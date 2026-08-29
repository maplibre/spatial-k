package org.maplibre.spatialk.geojson.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.EmptyForeignMembers
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureId
import org.maplibre.spatialk.geojson.GeoJson
import org.maplibre.spatialk.geojson.Geometry

internal class FeatureSerializer<T : Geometry?, P : @Serializable Any?>(
    private val geometrySerializer: KSerializer<T>,
    private val propertiesSerializer: KSerializer<P>,
) : KSerializer<Feature<T, P>> {
    private val serialName: String = "Feature"
    private val typeSerializer = String.serializer()
    private val bboxSerializer = BoundingBox.serializer().nullable
    private val idSerializer = FeatureIdSerializer.nullable

    // special sentinel for nullable values
    private val uninitialized = Any()

    init {
        if (propertiesSerializer.descriptor.kind !is StructureKind)
            throw SerializationException(
                "Expected Feature.properties to serialize to a structure, got ${propertiesSerializer.descriptor.kind}"
            )
    }

    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor(serialName) {
            element("type", typeSerializer.descriptor)
            element("geometry", geometrySerializer.descriptor)
            element("properties", propertiesSerializer.descriptor, isOptional = !GeoJson.STRICT)
            element("id", idSerializer.descriptor, isOptional = true)
            element("bbox", bboxSerializer.descriptor, isOptional = true)
        }

    override fun serialize(encoder: Encoder, value: Feature<T, P>) {
        if (encoder is JsonEncoder) {
            encoder.encodeStreamingGeoJsonObject(value.foreignMembers) {
                put("type", typeSerializer, serialName)
                put("geometry", geometrySerializer, value.geometry)
                put("properties", propertiesSerializer, value.properties)
                value.id?.let { put("id", FeatureIdSerializer, it) }
                value.bbox?.let { put("bbox", BoundingBox.serializer(), it) }
            }
            return
        }

        encoder.encodeStructure(descriptor) {
            encodeSerializableElement(descriptor, 0, typeSerializer, serialName)
            encodeSerializableElement(descriptor, 1, geometrySerializer, value.geometry)
            encodeSerializableElement(descriptor, 2, propertiesSerializer, value.properties)
            encodeSerializableElement(descriptor, 3, idSerializer, value.id)
            encodeSerializableElement(descriptor, 4, bboxSerializer, value.bbox)
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun deserialize(decoder: Decoder): Feature<T, P> {
        if (decoder is JsonDecoder) {
            var geometry: Any? = uninitialized
            var properties: Any? = uninitialized
            var id: FeatureId? = null
            val members =
                decoder.readStreamingGeoJson(typeSerializer, bboxSerializer) { key ->
                    when (key) {
                        "geometry" -> {
                            geometry = decode(geometrySerializer)
                            true
                        }
                        "properties" -> {
                            properties = decode(propertiesSerializer)
                            true
                        }
                        "id" -> {
                            id = decode(idSerializer)
                            true
                        }
                        else -> false
                    }
                }
            members.requireType(serialName)
            if (geometry === uninitialized) throw MissingFieldException("geometry", serialName)
            if (properties === uninitialized && GeoJson.STRICT)
                throw MissingFieldException("properties", serialName)
            @Suppress("UNCHECKED_CAST")
            return Feature(
                geometry as T,
                if (properties === uninitialized) null as P else properties as P,
                id,
                members.bbox,
                members.foreignMembers(FeatureReservedKeys),
            )
        }

        return decoder.decodeStructure(descriptor) {
            var type: String? = null
            var bbox: BoundingBox? = null
            var geometry: Any? = uninitialized
            var properties: Any? = uninitialized
            var id: FeatureId? = null

            if (decodeSequentially()) {
                type = decodeSerializableElement(descriptor, 0, typeSerializer)
                geometry = decodeSerializableElement(descriptor, 1, geometrySerializer)
                properties = decodeSerializableElement(descriptor, 2, propertiesSerializer)
                id = decodeSerializableElement(descriptor, 3, idSerializer)
                bbox = decodeSerializableElement(descriptor, 4, bboxSerializer)
            } else {
                while (true) when (val index = decodeElementIndex(descriptor)) {
                    CompositeDecoder.DECODE_DONE -> break
                    0 -> type = decodeSerializableElement(descriptor, 0, typeSerializer)
                    1 -> geometry = decodeSerializableElement(descriptor, 1, geometrySerializer)
                    2 -> properties = decodeSerializableElement(descriptor, 2, propertiesSerializer)
                    3 -> id = decodeSerializableElement(descriptor, 3, idSerializer)
                    4 -> bbox = decodeSerializableElement(descriptor, 4, bboxSerializer)
                    else -> throw SerializationException("Unknown index $index")
                }
            }

            requireGeoJsonType(type, serialName)
            if (geometry == uninitialized) throw MissingFieldException("geometry", serialName)
            if (properties == uninitialized && GeoJson.STRICT)
                throw MissingFieldException("properties", serialName)

            @Suppress("UNCHECKED_CAST")
            Feature(
                geometry as T,
                if (properties == uninitialized) null as P else properties as P,
                id,
                bbox,
                EmptyForeignMembers,
            )
        }
    }
}
