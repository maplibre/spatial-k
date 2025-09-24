@file:JvmName("-FeatureDslKt")
@file:Suppress("MatchingDeclarationName")

package org.maplibre.spatialk.geojson.dsl

import kotlin.jvm.JvmName
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.Geometry

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
public inline fun feature(
    geometry: Geometry? = null,
    id: String? = null,
    bbox: BoundingBox? = null,
    properties: PropertiesBuilder.() -> Unit = {},
): Feature = Feature(geometry, PropertiesBuilder().apply(properties).build(), id, bbox)
