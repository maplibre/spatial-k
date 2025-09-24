package org.maplibre.spatialk.geojson.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder

internal interface JsonSerializer<T> : KSerializer<T> {
    override fun deserialize(decoder: Decoder): T {
        val input = decoder as? JsonDecoder
            ?: throw SerializationException("This class can only be loaded from JSON")
        return deserialize(input)
    }

    override fun serialize(encoder: Encoder, value: T) {
        val output = encoder as? JsonEncoder
            ?: throw SerializationException("This class can only be saved as JSON")
        serialize(output, value)
    }

    fun deserialize(input: JsonDecoder): T
    fun serialize(output: JsonEncoder, value: T)
}
