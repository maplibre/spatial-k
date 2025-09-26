package org.maplibre.spatialk.geojson.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

internal object Surrogate {
    fun <S, T> deserialize(serializer: KSerializer<S>, decoder: Decoder, mapper: (S) -> T): T {
        try {
            val surrogate = serializer.deserialize(decoder)
            return mapper(surrogate)
        } catch (e: IllegalArgumentException) {
            throw SerializationException(e.message)
        }
    }

    fun <T> serialize(serializer: KSerializer<T>, encoder: Encoder, value: () -> T) {
        try {
            serializer.serialize(encoder, value())
        } catch (e: IllegalArgumentException) {
            throw SerializationException(e.message)
        }
    }
}
