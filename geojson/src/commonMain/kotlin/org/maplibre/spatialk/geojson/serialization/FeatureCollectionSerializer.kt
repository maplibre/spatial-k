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
import kotlinx.serialization.json.JsonEncoder
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
        if (encoder is JsonEncoder) {
            encoder.encodeStreamingGeoJsonObject(value.foreignMembers) {
                put("type", typeSerializer, serialName)
                value.bbox?.let { put("bbox", BoundingBox.serializer(), it) }
                put("features", featuresSerializer, value.features)
            }
            return
        }

        encoder.encodeStructure(descriptor) {
            encodeSerializableElement(descriptor, 0, typeSerializer, serialName)
            encodeSerializableElement(descriptor, 1, bboxSerializer, value.bbox)
            encodeSerializableElement(descriptor, 2, featuresSerializer, value.features)
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun deserialize(decoder: Decoder): FeatureCollection<T, P> {
        if (decoder is JsonDecoder) {
            var features: List<Feature<T, P>>? = null
            val members =
                decoder.readStreamingGeoJson(typeSerializer, bboxSerializer) { key ->
                    if (key != "features") return@readStreamingGeoJson false
                    features = decode(featuresSerializer)
                    true
                }
            members.requireType(serialName)
            return FeatureCollection(
                features ?: throw MissingFieldException("features", serialName),
                members.bbox,
                members.foreignMembers(FeatureCollectionReservedKeys),
            )
        }

        return decoder.decodeStructure(descriptor) {
            var type: String? = null
            var bbox: BoundingBox? = null
            var features: List<Feature<T, P>>? = null

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

            requireGeoJsonType(type, serialName)
            if (features == null) throw MissingFieldException("features", serialName)

            FeatureCollection(features, bbox, EmptyForeignMembers)
        }
    }
}
