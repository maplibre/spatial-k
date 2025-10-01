package org.maplibre.spatialk.geojson.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.DoubleArraySerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.maplibre.spatialk.geojson.BoundingBox

/** Serializes a [BoundingBox] by invoking [DoubleArraySerializer] on the coordinates. */
public object BoundingBoxSerializer : KSerializer<BoundingBox> {

    private val delegate = DoubleArraySerializer()

    override val descriptor: SerialDescriptor = delegate.descriptor

    override fun deserialize(decoder: Decoder): BoundingBox {
        val array = delegate.deserialize(decoder)

        if (array.size < 4 || array.size % 2 == 1)
            throw SerializationException(
                "BoundingBox data requires array of even size >= 4. Found array of size: ${array.size}"
            )

        return BoundingBox(array)
    }

    override fun serialize(encoder: Encoder, value: BoundingBox): Unit =
        delegate.serialize(encoder, value.coordinates)
}
