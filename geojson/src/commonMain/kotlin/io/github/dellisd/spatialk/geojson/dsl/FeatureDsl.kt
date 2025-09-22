@file:JvmName("-FeatureDslKt")
@file:Suppress("MatchingDeclarationName")

package io.github.dellisd.spatialk.geojson.dsl

import io.github.dellisd.spatialk.geojson.BoundingBox
import io.github.dellisd.spatialk.geojson.Feature
import io.github.dellisd.spatialk.geojson.Geometry
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlin.jvm.JvmName

@GeoJsonDsl
public class PropertiesBuilder {
    private val properties = mutableMapOf<String, JsonElement>()

    public fun put(key: String, value: String?) {
        properties[key] = JsonPrimitive(value)
    }

    public fun put(key: String, value: Number?) {
        properties[key] = JsonPrimitive(value)
    }

    public fun put(key: String, value: Boolean?) {
        properties[key] = JsonPrimitive(value)
    }

    public fun put(key: String, value: JsonElement) {
        properties[key] = value
    }

    public fun build(): Map<String, JsonElement> = properties
}

@GeoJsonDsl
public inline fun <reified T : Geometry> feature(
    geometry: T? = null,
    id: String? = null,
    bbox: BoundingBox? = null,
    properties: PropertiesBuilder.() -> Unit = {}
): Feature<T> = Feature(geometry, PropertiesBuilder().apply(properties).build(), id, bbox)
