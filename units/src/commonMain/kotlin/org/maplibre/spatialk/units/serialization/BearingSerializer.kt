package org.maplibre.spatialk.units.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.maplibre.spatialk.units.Bearing
import org.maplibre.spatialk.units.Bearing.Companion.North
import org.maplibre.spatialk.units.extensions.degrees
import org.maplibre.spatialk.units.extensions.inDegrees

/** Serializes a [Bearing] as its normalized clockwise angle in degrees from north. */
internal object BearingSerializer : KSerializer<Bearing> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("org.maplibre.spatialk.units.Bearing", PrimitiveKind.DOUBLE)

    override fun serialize(encoder: Encoder, value: Bearing) {
        encoder.encodeDouble((value - North).inDegrees)
    }

    override fun deserialize(decoder: Decoder): Bearing {
        val angle = decoder.decodeDouble()
        return try {
            Bearing.of(angle.degrees)
        } catch (cause: IllegalArgumentException) {
            throw SerializationException("Invalid bearing '$angle'", cause)
        }
    }
}
