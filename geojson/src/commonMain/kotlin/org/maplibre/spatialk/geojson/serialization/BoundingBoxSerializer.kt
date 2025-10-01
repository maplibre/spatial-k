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
import org.maplibre.spatialk.geojson.BoundingBox

public object BoundingBoxSerializer : KSerializer<BoundingBox> {

    @OptIn(ExperimentalSerializationApi::class)
    override val descriptor: SerialDescriptor = listSerialDescriptor(Double.serializer().descriptor)

    override fun deserialize(decoder: Decoder): BoundingBox {
        val list = ListSerializer(Double.serializer()).deserialize(decoder)

        if (list.size < 4 || list.size % 2 == 1) {
            throw SerializationException(
                "Expected array of even size >= 4. Got array of size ${list.size}"
            )
        }

        return BoundingBox(list.toDoubleArray())
    }

    override fun serialize(encoder: Encoder, value: BoundingBox) {
        ListSerializer(Double.serializer()).serialize(encoder, value.coordinates.toList())
    }
}
