package io.github.dellisd.spatialk.geojson.serialization

import io.github.dellisd.spatialk.geojson.Feature
import io.github.dellisd.spatialk.geojson.Geometry
import io.github.dellisd.spatialk.geojson.serialization.BoundingBoxSerializer.toJsonArray
import io.github.dellisd.spatialk.geojson.serialization.GeometrySerializer.toJsonObject
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put

internal class FeatureSerializer<T : Geometry> : JsonSerializer<Feature<T>> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Feature")

    override fun deserialize(input: JsonDecoder): Feature<T> = Feature.fromJson(input.decodeJsonElement().jsonObject)

    override fun serialize(output: JsonEncoder, value: Feature<T>) {
        output.encodeJsonElement(value.toJsonObject())
    }

}

internal fun <T : Geometry> Feature<T>.toJsonObject() = buildJsonObject {
    put("type", "Feature")
    bbox?.let { put("bbox", it.toJsonArray()) }
    geometry?.let { put("geometry", it.toJsonObject()) }
    id?.let { put("id", it) }

    put("properties", JsonObject(properties))
}
