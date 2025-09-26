package org.maplibre.spatialk.geojson.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.listSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.maplibre.spatialk.geojson.BoundingBox

public object BoundingBoxSerializer : KSerializer<BoundingBox> {
    private const val ARRAY_SIZE_2D = 4
    private const val ARRAY_SIZE_3D = 6

    override val descriptor: SerialDescriptor = listSerialDescriptor(Double.serializer().descriptor)

    override fun deserialize(decoder: Decoder): BoundingBox {
        val list = ListSerializer(Double.serializer()).deserialize(decoder)

        if (list.size != ARRAY_SIZE_2D && list.size != ARRAY_SIZE_3D) {
            throw SerializationException(
                "Expected array of size 4 or 6. Got array of size ${list.size}"
            )
        }

        return BoundingBox(list.toDoubleArray())
    }

    override fun serialize(encoder: Encoder, value: BoundingBox) {
        ListSerializer(Double.serializer()).serialize(encoder, value.coordinates.toList())
    }
}
