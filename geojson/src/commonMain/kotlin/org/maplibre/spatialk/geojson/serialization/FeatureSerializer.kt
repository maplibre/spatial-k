package org.maplibre.spatialk.geojson.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.json.JsonObject
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.Geometry

@OptIn(ExperimentalSerializationApi::class)
internal class FeatureSerializer<T : Geometry?>(private val geometrySerializer: KSerializer<T>) :
    KSerializer<Feature<T>> {

    private val isGeometryNullable = geometrySerializer.descriptor.isNullable
    private val propertiesSerializer = JsonObject.serializer().nullable
    private val idSerializer = String.serializer().nullable
    private val bboxSerializer = BoundingBox.serializer().nullable

    init {
        println(geometrySerializer.descriptor)
        println("isGeometryNullable: $isGeometryNullable")
    }

    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("Feature") {
            element("geometry", geometrySerializer.descriptor)
            element<JsonObject?>("properties")
            element<String?>("id")
            element<BoundingBox?>("bbox")
        }

    override fun serialize(encoder: Encoder, value: Feature<T>) =
        encoder.encodeStructure(descriptor) {
            // https://datatracker.ietf.org/doc/html/rfc7946#section-3.2
            // Always encode geometry and properties
            encodeNullableSerializableElement(descriptor, 0, geometrySerializer, value.geometry)
            encodeNullableSerializableElement(descriptor, 1, propertiesSerializer, value.properties)

            // Only encode id and bbox if not null
            if (value.id != null)
                encodeNullableSerializableElement(descriptor, 2, idSerializer, value.id)
            if (value.bbox != null)
                encodeNullableSerializableElement(descriptor, 3, bboxSerializer, value.bbox)
        }

    override fun deserialize(decoder: Decoder): Feature<T> =
        decoder.decodeStructure(descriptor) {
            var geometry: T? = null
            var properties: JsonObject? = null
            var id: String? = null
            var bbox: BoundingBox? = null

            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 ->
                        geometry =
                            decodeNullableSerializableElement(descriptor, 0, geometrySerializer)
                    1 ->
                        properties =
                            decodeNullableSerializableElement(descriptor, 1, propertiesSerializer)
                    2 -> id = decodeNullableSerializableElement(descriptor, 2, idSerializer)
                    3 -> bbox = decodeNullableSerializableElement(descriptor, 3, bboxSerializer)
                    CompositeDecoder.DECODE_DONE -> break
                    else -> throw SerializationException("Unknown index $index")
                }
            }

            @Suppress("UNCHECKED_CAST")
            if (geometry == null && !isGeometryNullable)
                throw SerializationException("Expected Feature to have a non-null geometry")
            else geometry as T

            Feature(geometry = geometry, properties = properties, id = id, bbox = bbox)
        }
}
