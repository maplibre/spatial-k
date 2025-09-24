package org.maplibre.spatialk.geojson.serialization

import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.serialization.BoundingBoxSerializer.toJsonArray
import org.maplibre.spatialk.geojson.serialization.GeometrySerializer.toJsonObject
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put

public object FeatureSerializer : JsonSerializer<Feature> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Feature")

    override fun deserialize(input: JsonDecoder): Feature = Feature.fromJson(input.decodeJsonElement().jsonObject)

    override fun serialize(output: JsonEncoder, value: Feature) {
        output.encodeJsonElement(value.toJsonObject())
    }

    internal fun Feature.toJsonObject() = buildJsonObject {
        put("type", "Feature")
        bbox?.let { put("bbox", it.toJsonArray()) }
        geometry?.let { put("geometry", it.toJsonObject()) }
        id?.let { put("id", it) }

        put("properties", JsonObject(properties))
    }
}
