package io.github.dellisd.spatialk.geojson.serialization

import io.github.dellisd.spatialk.geojson.FeatureCollection
import io.github.dellisd.spatialk.geojson.serialization.BoundingBoxSerializer.toJsonArray
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put

public object FeatureCollectionSerializer : JsonSerializer<FeatureCollection> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("FeatureCollection")

    override fun deserialize(input: JsonDecoder): FeatureCollection {
        return FeatureCollection.fromJson(input.decodeJsonElement().jsonObject)
    }

    override fun serialize(output: JsonEncoder, value: FeatureCollection) {
        val data = buildJsonObject {
            put("type", "FeatureCollection")
            value.bbox?.let { put("bbox", it.toJsonArray()) }
            put(
                "features",
                buildJsonArray {
                    value.features.forEach { feature -> add(feature.toJsonObject()) }
                }
            )
        }
        output.encodeJsonElement(data)
    }
}
