package org.maplibre.spatialk.geojson.serialization

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.EmptyForeignMembers

internal val StreamingGeoJsonMapDescriptor: SerialDescriptor =
    MapSerializer(String.serializer(), JsonElement.serializer()).descriptor

internal val GeometryCoordinateReservedKeys: Set<String> =
    setOf("type", "bbox", "coordinates", "geometry", "properties", "features", "geometries")

internal val GeometryCollectionReservedKeys: Set<String> =
    setOf("type", "bbox", "geometries", "geometry", "properties", "features", "coordinates")

internal val FeatureReservedKeys: Set<String> =
    setOf(
        "type",
        "bbox",
        "geometry",
        "properties",
        "id",
        "coordinates",
        "geometries",
        "features",
    )

internal val FeatureCollectionReservedKeys: Set<String> =
    setOf("type", "bbox", "features", "coordinates", "geometries", "geometry", "properties")

internal fun validateForeignMembers(foreignMembers: JsonObject, reserved: Set<String>) {
    val conflict = reservedConflict(foreignMembers.keys, reserved)
    require(conflict == null) {
        "Foreign member \"$conflict\" conflicts with a reserved GeoJSON member name"
    }
}

internal fun foreignMembersFromExtras(
    extras: Map<String, JsonElement>,
    reserved: Set<String>,
): JsonObject {
    val conflict = reservedConflict(extras.keys, reserved)
    if (conflict != null) {
        throw SerializationException(
            "Foreign member \"$conflict\" conflicts with a reserved GeoJSON member name"
        )
    }
    return if (extras.isEmpty()) EmptyForeignMembers else JsonObject(extras.toMap())
}

private fun reservedConflict(keys: Set<String>, reserved: Set<String>): String? = keys.firstOrNull {
    it in reserved
}

@OptIn(ExperimentalSerializationApi::class)
internal fun requireGeoJsonType(type: String?, serialName: String) {
    val decoded = type ?: throw MissingFieldException("type", serialName)
    if (decoded != serialName)
        throw SerializationException("Expected type $serialName but found $decoded")
}

internal class StreamingGeoJsonWriter(private val encoder: CompositeEncoder) {
    private var index = 0

    fun <T> put(key: String, serializer: SerializationStrategy<T>, value: T) {
        encoder.encodeStringElement(StreamingGeoJsonMapDescriptor, index++, key)
        encoder.encodeSerializableElement(StreamingGeoJsonMapDescriptor, index++, serializer, value)
    }
}

/**
 * Writes known members then [extras] as a JSON object without first building a [JsonObject] tree.
 */
internal inline fun JsonEncoder.encodeStreamingGeoJsonObject(
    extras: JsonObject,
    crossinline writeKnown: StreamingGeoJsonWriter.() -> Unit,
) {
    encodeStructure(StreamingGeoJsonMapDescriptor) {
        StreamingGeoJsonWriter(this).run {
            writeKnown()
            extras.forEach { (key, value) -> put(key, JsonElement.serializer(), value) }
        }
    }
}

internal class StreamingGeoJsonValue(
    private val decoder: CompositeDecoder,
    private val index: Int,
) {
    fun <T> decode(serializer: DeserializationStrategy<T>): T =
        decoder.decodeSerializableElement(StreamingGeoJsonMapDescriptor, index, serializer)
}

internal class StreamingGeoJsonMembers {
    var type: String? = null
    var bbox: BoundingBox? = null
    val extras: MutableMap<String, JsonElement> = linkedMapOf()

    fun requireType(serialName: String) {
        requireGeoJsonType(type, serialName)
    }

    fun foreignMembers(reserved: Set<String>): JsonObject =
        foreignMembersFromExtras(extras, reserved)
}

/**
 * Walks a JSON object as a map so large members such as `features` can be decoded without first
 * materializing the whole document as a [JsonObject] tree. [handle] returns true when [key] was a
 * known member.
 */
@OptIn(ExperimentalSerializationApi::class)
internal fun JsonDecoder.readStreamingGeoJson(
    typeSerializer: DeserializationStrategy<String>,
    bboxSerializer: DeserializationStrategy<BoundingBox?>,
    handle: StreamingGeoJsonValue.(key: String) -> Boolean,
): StreamingGeoJsonMembers {
    val members = StreamingGeoJsonMembers()
    decodeStructure(StreamingGeoJsonMapDescriptor) {
        fun consume(key: String, valueIndex: Int) {
            val value = StreamingGeoJsonValue(this, valueIndex)
            when (key) {
                "type" -> members.type = value.decode(typeSerializer)
                "bbox" -> members.bbox = value.decode(bboxSerializer)
                else ->
                    if (!value.handle(key)) {
                        members.extras[key] = value.decode(JsonElement.serializer())
                    }
            }
        }

        if (decodeSequentially()) {
            val size = decodeCollectionSize(StreamingGeoJsonMapDescriptor)
            for (index in 0 until size * 2 step 2) {
                consume(decodeStringElement(StreamingGeoJsonMapDescriptor, index), index + 1)
            }
        } else {
            while (true) {
                val keyIndex = decodeElementIndex(StreamingGeoJsonMapDescriptor)
                if (keyIndex == CompositeDecoder.DECODE_DONE) break
                val key = decodeStringElement(StreamingGeoJsonMapDescriptor, keyIndex)
                val valueIndex = decodeElementIndex(StreamingGeoJsonMapDescriptor)
                if (valueIndex == CompositeDecoder.DECODE_DONE) {
                    throw SerializationException("Expected a value for \"$key\"")
                }
                consume(key, valueIndex)
            }
        }
    }
    return members
}
