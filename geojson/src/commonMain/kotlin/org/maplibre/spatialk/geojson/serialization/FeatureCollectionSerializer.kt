package org.maplibre.spatialk.geojson.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
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
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.EmptyForeignMembers
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Geometry

internal class FeatureCollectionSerializer<T : Geometry?, P : @Serializable Any?>(
    geometrySerializer: KSerializer<T>,
    propertiesSerializer: KSerializer<P>,
) : KSerializer<FeatureCollection<T, P>> {
    private val serialName: String = "FeatureCollection"
    private val typeSerializer = String.serializer()
    private val bboxSerializer = BoundingBox.serializer().nullable
    private val featuresSerializer =
        ListSerializer(Feature.serializer(geometrySerializer, propertiesSerializer))

    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor(serialName) {
            element("type", typeSerializer.descriptor)
            element("bbox", bboxSerializer.descriptor)
            element("features", featuresSerializer.descriptor)
        }

    override fun serialize(encoder: Encoder, value: FeatureCollection<T, P>) {
        if (encoder is JsonEncoder && value.foreignMembers.isNotEmpty()) {
            val specObject = buildJsonObject {
                put("type", serialName)
                value.bbox?.let {
                    put("bbox", encoder.json.encodeToJsonElement(BoundingBox.serializer(), it))
                }
                put(
                    "features",
                    encoder.json.encodeToJsonElement(featuresSerializer, value.features),
                )
            }
            encodeGeoJsonObject(encoder, specObject, value.foreignMembers)
            return
        }

        encoder.encodeStructure(descriptor) {
            encodeSerializableElement(descriptor, 0, typeSerializer, serialName)
            if (value.bbox != null || encoder !is JsonEncoder)
                encodeSerializableElement(descriptor, 1, bboxSerializer, value.bbox)
            encodeSerializableElement(descriptor, 2, featuresSerializer, value.features)
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun deserialize(decoder: Decoder): FeatureCollection<T, P> {
        if (decoder is JsonDecoder) {
            var type: String? = null
            var bbox: BoundingBox? = null
            var features: List<Feature<T, P>>? = null
            val extras = linkedMapOf<String, JsonElement>()
            decoder.forEachStreamingMember { key, valueIndex ->
                when (key) {
                    "type" -> type = decodeStreamingValue(valueIndex, typeSerializer)
                    "bbox" -> bbox = decodeStreamingValue(valueIndex, bboxSerializer)
                    "features" -> features = decodeStreamingValue(valueIndex, featuresSerializer)
                    else -> extras[key] = decodeStreamingValue(valueIndex, JsonElement.serializer())
                }
            }
            val decodedType = type ?: throw MissingFieldException("type", serialName)
            if (decodedType != serialName)
                throw SerializationException("Expected type $serialName but found $decodedType")
            val decodedFeatures = features ?: throw MissingFieldException("features", serialName)
            return FeatureCollection(
                decodedFeatures,
                bbox,
                foreignMembersFromExtras(extras, FeatureCollectionForbiddenKeys),
            )
        }

        return decoder.decodeStructure(descriptor) {
            var type: String? = null
            var bbox: BoundingBox? = null
            var features: List<Feature<T, P>>? = null

            @OptIn(ExperimentalSerializationApi::class)
            if (decodeSequentially()) {
                type = decodeSerializableElement(descriptor, 0, typeSerializer)
                bbox = decodeSerializableElement(descriptor, 1, bboxSerializer)
                features = decodeSerializableElement(descriptor, 2, featuresSerializer)
            } else {
                while (true) when (val index = decodeElementIndex(descriptor)) {
                    CompositeDecoder.DECODE_DONE -> break
                    0 -> type = decodeSerializableElement(descriptor, 0, typeSerializer)
                    1 -> bbox = decodeSerializableElement(descriptor, 1, bboxSerializer)
                    2 -> features = decodeSerializableElement(descriptor, 2, featuresSerializer)
                    else -> throw SerializationException("Unknown index $index")
                }
            }

            if (type != serialName)
                throw SerializationException("Expected type $serialName but found $type")
            if (features == null) throw SerializationException("Expected features to be present")

            FeatureCollection(features, bbox, EmptyForeignMembers)
        }
    }
}
