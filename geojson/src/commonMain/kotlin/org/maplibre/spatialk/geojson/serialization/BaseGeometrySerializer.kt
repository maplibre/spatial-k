package org.maplibre.spatialk.geojson.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.EmptyForeignMembers
import org.maplibre.spatialk.geojson.Geometry

internal abstract class BaseGeometrySerializer<G : Geometry, C>(
    private val serialName: String,
    private val coordinatesSerializer: KSerializer<C>,
) : KSerializer<G> {
    private val typeSerializer = String.serializer()
    private val bboxSerializer = BoundingBox.serializer().nullable

    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor(serialName) {
            element("type", typeSerializer.descriptor)
            element("bbox", bboxSerializer.descriptor)
            element("coordinates", coordinatesSerializer.descriptor)
        }

    override fun serialize(encoder: Encoder, value: G) {
        if (encoder is JsonEncoder && value.foreignMembers.isNotEmpty()) {
            encoder.encodeStreamingGeoJsonObject(value.foreignMembers) {
                put("type", typeSerializer, serialName)
                value.bbox?.let { put("bbox", BoundingBox.serializer(), it) }
                put("coordinates", coordinatesSerializer, getCoordinates(value))
            }
            return
        }

        encoder.encodeStructure(descriptor) {
            encodeSerializableElement(descriptor, 0, typeSerializer, serialName)
            if (value.bbox != null || encoder !is JsonEncoder)
                encodeSerializableElement(descriptor, 1, bboxSerializer, value.bbox)
            encodeSerializableElement(descriptor, 2, coordinatesSerializer, getCoordinates(value))
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun deserialize(decoder: Decoder): G {
        if (decoder is JsonDecoder) {
            var type: String? = null
            var bbox: BoundingBox? = null
            var coordinates: C? = null
            val extras = linkedMapOf<String, JsonElement>()
            decoder.forEachStreamingMember { key, valueIndex ->
                when (key) {
                    "type" -> type = decodeStreamingValue(valueIndex, typeSerializer)
                    "bbox" -> bbox = decodeStreamingValue(valueIndex, bboxSerializer)
                    "coordinates" ->
                        coordinates = decodeStreamingValue(valueIndex, coordinatesSerializer)
                    else -> extras[key] = decodeStreamingValue(valueIndex, JsonElement.serializer())
                }
            }
            val decodedType = type ?: throw MissingFieldException("type", serialName)
            if (decodedType != serialName)
                throw SerializationException("Expected type $serialName but found $decodedType")
            val decodedCoordinates =
                coordinates ?: throw MissingFieldException("coordinates", serialName)
            return construct(
                decodedCoordinates,
                bbox,
                foreignMembersFromExtras(extras, GeometryCoordinateForbiddenKeys),
            )
        }

        return decoder.decodeStructure(descriptor) {
            var type: String? = null
            var bbox: BoundingBox? = null
            var coordinates: C? = null

            if (decodeSequentially()) {
                type = decodeSerializableElement(descriptor, 0, typeSerializer)
                bbox = decodeSerializableElement(descriptor, 1, bboxSerializer)
                coordinates = decodeSerializableElement(descriptor, 2, coordinatesSerializer)
            } else {
                while (true) when (val index = decodeElementIndex(descriptor)) {
                    CompositeDecoder.DECODE_DONE -> break
                    0 -> type = decodeSerializableElement(descriptor, 0, typeSerializer)
                    1 -> bbox = decodeSerializableElement(descriptor, 1, bboxSerializer)
                    2 ->
                        coordinates =
                            decodeSerializableElement(descriptor, 2, coordinatesSerializer)
                    else -> throw SerializationException("Unknown index $index")
                }
            }

            if (type == null) throw MissingFieldException("type", serialName)
            if (coordinates == null) throw MissingFieldException("coordinates", serialName)

            if (type != serialName)
                throw SerializationException("Expected type $serialName but found $type")

            construct(coordinates, bbox, EmptyForeignMembers)
        }
    }

    protected abstract fun getCoordinates(value: G): C

    protected abstract fun construct(
        coordinates: C,
        bbox: BoundingBox?,
        foreignMembers: JsonObject,
    ): G
}
