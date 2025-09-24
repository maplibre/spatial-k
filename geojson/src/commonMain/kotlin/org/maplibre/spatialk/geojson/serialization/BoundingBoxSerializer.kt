package org.maplibre.spatialk.geojson.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import org.maplibre.spatialk.geojson.BoundingBox

public object BoundingBoxSerializer : KSerializer<BoundingBox> {
    private const val ARRAY_SIZE_2D = 4
    private const val ARRAY_SIZE_3D = 6

    override val descriptor: SerialDescriptor
        get() = buildSerialDescriptor("BoundingBox", StructureKind.LIST)

    @Suppress("MagicNumber")
    override fun deserialize(decoder: Decoder): BoundingBox {
        val input =
            decoder as? JsonDecoder
                ?: throw SerializationException("This class can only be loaded from JSON")
        val array = input.decodeJsonElement().jsonArray

        return when (array.size) {
            ARRAY_SIZE_2D -> {
                BoundingBox(
                    array[0].jsonPrimitive.double,
                    array[1].jsonPrimitive.double,
                    array[2].jsonPrimitive.double,
                    array[3].jsonPrimitive.double,
                )
            }
            ARRAY_SIZE_3D -> {
                BoundingBox(
                    array[0].jsonPrimitive.double,
                    array[1].jsonPrimitive.double,
                    array[2].jsonPrimitive.double,
                    array[3].jsonPrimitive.double,
                    array[4].jsonPrimitive.double,
                    array[5].jsonPrimitive.double,
                )
            }
            else -> {
                throw SerializationException(
                    "Expected array of size 4 or 6. Got array of size ${array.size}"
                )
            }
        }
    }

    override fun serialize(encoder: Encoder, value: BoundingBox) {
        encoder as? JsonEncoder
            ?: throw SerializationException("This class can only be saved as JSON")

        encoder.encodeJsonElement(value.toJsonArray())
    }

    internal fun BoundingBox.toJsonArray(): JsonArray = buildJsonArray {
        coordinates.forEach(this::add)
    }
}
