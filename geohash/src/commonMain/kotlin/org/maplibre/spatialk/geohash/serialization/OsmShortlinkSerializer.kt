package org.maplibre.spatialk.geohash.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.maplibre.spatialk.geohash.OsmShortlink

/** Serializes an [OsmShortlink] as its canonical shortlink code. */
internal object OsmShortlinkSerializer : KSerializer<OsmShortlink> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor(
            "org.maplibre.spatialk.geohash.OsmShortlink",
            PrimitiveKind.STRING,
        )

    override fun serialize(encoder: Encoder, value: OsmShortlink) {
        encoder.encodeString(value.text)
    }

    override fun deserialize(decoder: Decoder): OsmShortlink {
        val text = decoder.decodeString()
        return try {
            OsmShortlink.parse(text)
        } catch (cause: IllegalArgumentException) {
            throw SerializationException("Invalid OpenStreetMap shortlink '$text'", cause)
        }
    }
}
