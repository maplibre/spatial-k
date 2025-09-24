package org.maplibre.spatialk.geojson.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.listSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.maplibre.spatialk.geojson.Position

/**
 * [KSerializer] implementation for implementations of the [Position] interface. Serializes a
 * Position down to an array of numbers as specified by GeoJSON. A position maps to `[longitude,
 * latitude, altitude]`.
 *
 * A position's [altitude][Position.altitude] is only included in the array if it is not null.
 */
public object PositionSerializer : KSerializer<Position> {
    @OptIn(ExperimentalSerializationApi::class)
    override val descriptor: SerialDescriptor = listSerialDescriptor(Double.serializer().descriptor)

    override fun deserialize(decoder: Decoder): Position {
        val list = ListSerializer(Double.serializer()).deserialize(decoder)

        if (list.size < 2) {
            throw SerializationException(
                "Position data requires at least 2 doubles (longitude, latitude). Found: ${list.size}"
            )
        }

        return Position(list[0], list[1], if (list.size > 2) list[2] else null)
    }

    override fun serialize(encoder: Encoder, value: Position) {
        ListSerializer(Double.serializer())
            .serialize(
                encoder,
                mutableListOf(value.longitude, value.latitude).let { list ->
                    value.altitude?.let { list.plus(it) } ?: list
                },
            )
    }
}
