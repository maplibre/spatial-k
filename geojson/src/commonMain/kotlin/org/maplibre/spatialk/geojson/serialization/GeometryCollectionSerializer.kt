package org.maplibre.spatialk.geojson.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
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
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.Geometry
import org.maplibre.spatialk.geojson.GeometryCollection

internal object GeometryCollectionSerializer : KSerializer<GeometryCollection> {
    private val serialName: String = "GeometryCollection"
    private val typeSerializer = String.serializer()
    private val bboxSerializer = BoundingBox.Companion.serializer().nullable
    private val geometriesSerializer = ListSerializer(Geometry.serializer())

    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor(serialName) {
            element("type", typeSerializer.descriptor)
            element("bbox", bboxSerializer.descriptor)
            element("geometries", geometriesSerializer.descriptor)
        }

    override fun serialize(encoder: Encoder, value: GeometryCollection) {
        encoder.encodeStructure(descriptor) {
            encodeSerializableElement(descriptor, 0, typeSerializer, serialName)
            if (value.bbox != null)
                encodeSerializableElement(descriptor, 1, bboxSerializer, value.bbox)
            encodeSerializableElement(descriptor, 2, geometriesSerializer, value.geometries)
        }
    }

    override fun deserialize(decoder: Decoder): GeometryCollection {
        return decoder.decodeStructure(descriptor) {
            var type: String? = null
            var bbox: BoundingBox? = null
            var geometries: List<Geometry>? = null

            @OptIn(ExperimentalSerializationApi::class)
            if (decodeSequentially()) {
                type = decodeSerializableElement(descriptor, 0, typeSerializer)
                bbox = decodeSerializableElement(descriptor, 1, bboxSerializer)
                geometries = decodeSerializableElement(descriptor, 2, geometriesSerializer)
            } else {
                while (true) when (val index = decodeElementIndex(descriptor)) {
                    CompositeDecoder.DECODE_DONE -> break
                    0 -> type = decodeSerializableElement(descriptor, 0, typeSerializer)
                    1 -> bbox = decodeSerializableElement(descriptor, 1, bboxSerializer)
                    2 -> geometries = decodeSerializableElement(descriptor, 2, geometriesSerializer)
                    else -> throw SerializationException("Unknown index $index")
                }
            }

            if (type != serialName)
                throw SerializationException("Expected type $serialName but found $type")
            if (geometries == null)
                throw SerializationException("Expected geometries to be present")

            GeometryCollection(geometries, bbox)
        }
    }
}
