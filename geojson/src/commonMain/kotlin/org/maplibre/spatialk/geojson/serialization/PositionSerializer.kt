package org.maplibre.spatialk.geojson.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.DoubleArraySerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.maplibre.spatialk.geojson.Position

/** Serializes a [Position] by invoking [DoubleArraySerializer] on the coordinates. */
internal object PositionSerializer : KSerializer<Position> {
    private val delegate = DoubleArraySerializer()

    override val descriptor: SerialDescriptor = delegate.descriptor

    override fun deserialize(decoder: Decoder): Position {
        val array = delegate.deserialize(decoder)

        if (array.size < 2)
            throw SerializationException(
                "Position data requires at least two elements [longitude, latitude]. Found array of size: ${array.size}"
            )

        return Position(array)
    }

    override fun serialize(encoder: Encoder, value: Position): Unit =
        delegate.serialize(encoder, value.coordinates)
}
