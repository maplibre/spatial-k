package org.maplibre.spatialk.geojson.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonObject
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.Geometry

// internal class FeatureSerializer<T : Geometry>(geometrySerializer: KSerializer<T>) :
//    KSerializer<Feature<T>> {
//    private val delegate = Feature.serializer(typeSerial0 = geometrySerializer.nullable)
//
//    override val descriptor: SerialDescriptor = delegate.descriptor
//
//    @Suppress("UNCHECKED_CAST")
//    override fun deserialize(decoder: Decoder): Feature<T> {
//        try {
//            return delegate.deserialize(decoder) as Feature<T>
//        } catch (e: ClassCastException) {
//            throw SerializationException(e.message)
//        }
//    }
//
//    override fun serialize(encoder: Encoder, value: Feature<T>): Unit =
//        delegate.serialize(encoder, value)
// }

internal class FeatureSerializer<T : Geometry>(geometrySerializer: KSerializer<T>) :
    KSerializer<Feature<T?>> {

    val geometrySerializer = Geometry.serializer().nullable

    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("Feature") {
            element("geometry", geometrySerializer.descriptor)
            element("properties", JsonObject.serializer().descriptor, isOptional = true)
            element("id", PrimitiveSerialDescriptor("id", PrimitiveKind.STRING), isOptional = true)
            element("bbox", BoundingBox.serializer().descriptor, isOptional = true)
        }

    override fun serialize(encoder: Encoder, value: Feature<T?>) {
        val composite = encoder.beginStructure(descriptor)
        composite.encodeSerializableElement(descriptor, 0, geometrySerializer, value.geometry)
        if (value.properties != null) {
            composite.encodeSerializableElement(
                descriptor,
                1,
                JsonObject.serializer(),
                value.properties,
            )
        }
        if (value.id != null) {
            composite.encodeStringElement(descriptor, 2, value.id)
        }
        if (value.bbox != null) {
            composite.encodeSerializableElement(descriptor, 3, BoundingBox.serializer(), value.bbox)
        }
        composite.endStructure(descriptor)
    }

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(decoder: Decoder): Feature<T?> {
        val dec = decoder.beginStructure(descriptor)
        var geometry: Geometry? = null
        var properties: JsonObject? = null
        var id: String? = null
        var bbox: BoundingBox? = null

        while (true) {
            when (val index = dec.decodeElementIndex(descriptor)) {
                0 -> geometry = dec.decodeSerializableElement(descriptor, 0, geometrySerializer)
                1 ->
                    properties =
                        dec.decodeSerializableElement(descriptor, 1, JsonObject.serializer())
                2 -> id = dec.decodeStringElement(descriptor, 2)
                3 -> bbox = dec.decodeSerializableElement(descriptor, 3, BoundingBox.serializer())
                CompositeDecoder.DECODE_DONE -> break
                else -> throw SerializationException("Unknown index $index in Feature")
            }
        }

        dec.endStructure(descriptor)

        try {
            geometry as T?
        } catch (e: ClassCastException) {
            throw SerializationException("Unexpected geometry: ${e.message}")
        }

        return Feature(geometry = geometry, properties = properties, id = id, bbox = bbox)
    }
}
