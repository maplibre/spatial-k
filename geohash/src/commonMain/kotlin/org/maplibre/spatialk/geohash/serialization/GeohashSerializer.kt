package org.maplibre.spatialk.geohash.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.maplibre.spatialk.geohash.Geohash

/** Serializes a [Geohash] as its canonical lowercase address. */
internal object GeohashSerializer : KSerializer<Geohash> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("org.maplibre.spatialk.geohash.Geohash", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Geohash) {
        encoder.encodeString(value.text)
    }

    override fun deserialize(decoder: Decoder): Geohash {
        val text = decoder.decodeString()
        return try {
            Geohash.parse(text)
        } catch (cause: IllegalArgumentException) {
            throw SerializationException("Invalid geohash '$text'", cause)
        }
    }
}
