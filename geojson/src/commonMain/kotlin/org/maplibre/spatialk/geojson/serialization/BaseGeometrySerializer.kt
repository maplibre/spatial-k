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
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.put
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
            val specObject = buildJsonObject {
                put("type", serialName)
                value.bbox?.let {
                    put("bbox", encoder.json.encodeToJsonElement(BoundingBox.serializer(), it))
                }
                put(
                    "coordinates",
                    encoder.json.encodeToJsonElement(coordinatesSerializer, getCoordinates(value)),
                )
            }
            encodeGeoJsonObject(encoder, specObject, value.foreignMembers)
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
            val obj = decodeGeoJsonObject(decoder)
            val type =
                decoder.json.decodeFromJsonElement(
                    typeSerializer,
                    obj.requireMember("type", serialName),
                )
            if (type != serialName)
                throw SerializationException("Expected type $serialName but found $type")
            val coordinates =
                decoder.json.decodeFromJsonElement(
                    coordinatesSerializer,
                    obj.requireMember("coordinates", serialName),
                )
            val bbox =
                obj["bbox"]?.let {
                    decoder.json.decodeFromJsonElement(BoundingBox.serializer(), it)
                }
            return construct(coordinates, bbox, obj.without(GeometryCoordinateReservedKeys))
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
