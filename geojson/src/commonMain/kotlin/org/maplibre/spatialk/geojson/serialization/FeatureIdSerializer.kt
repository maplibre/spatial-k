package org.maplibre.spatialk.geojson.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonPrimitive
import org.maplibre.spatialk.geojson.FeatureId

internal object FeatureIdSerializer : KSerializer<FeatureId> {
    private val jsonDelegate = JsonPrimitive.serializer()

    override val descriptor: SerialDescriptor = jsonDelegate.descriptor

    override fun serialize(encoder: Encoder, value: FeatureId) =
        if (encoder is JsonEncoder) jsonDelegate.serialize(encoder, value)
        else encoder.encodeString(Json.encodeToString(value))

    override fun deserialize(decoder: Decoder): FeatureId =
        if (decoder is JsonDecoder) jsonDelegate.deserialize(decoder)
        else Json.decodeFromString(jsonDelegate, decoder.decodeString())
}
