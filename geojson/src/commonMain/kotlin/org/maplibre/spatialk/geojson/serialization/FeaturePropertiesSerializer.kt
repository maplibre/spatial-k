package org.maplibre.spatialk.geojson.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject

internal object FeaturePropertiesSerializer : KSerializer<JsonObject> {
    private val delegate = JsonObject.serializer()

    override val descriptor: SerialDescriptor = delegate.descriptor

    override fun deserialize(decoder: Decoder): JsonObject {
        return when (decoder) {
            is JsonDecoder -> delegate.deserialize(decoder)
            else -> Json.decodeFromString(delegate, decoder.decodeString())
        }
    }

    override fun serialize(encoder: Encoder, value: JsonObject): Unit =
        when (encoder) {
            is JsonEncoder -> delegate.serialize(encoder, value)
            else -> encoder.encodeString(Json.encodeToString(delegate, value))
        }
}
