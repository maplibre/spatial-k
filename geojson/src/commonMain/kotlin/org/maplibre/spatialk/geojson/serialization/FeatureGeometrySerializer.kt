package org.maplibre.spatialk.geojson.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.maplibre.spatialk.geojson.Geometry

internal class FeatureGeometrySerializer<T : Geometry?> : KSerializer<T> {
    private val delegate = Geometry.serializer().nullable

    override val descriptor: SerialDescriptor = delegate.descriptor

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(decoder: Decoder): T {
        val value = delegate.deserialize(decoder)

        try {
            return value as T
        } catch (e: ClassCastException) {
            throw SerializationException("Unexpected geometry type: ${e.message}")
        }
    }

    override fun serialize(encoder: Encoder, value: T): Unit = delegate.serialize(encoder, value)
}
