@file:JvmSynthetic

package org.maplibre.spatialk.geojson.dsl

import kotlin.jvm.JvmSynthetic
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.buildJsonObject
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Geometry

@GeoJsonDsl
public class FeatureCollectionDsl(
    private val features: MutableList<Feature<*, JsonObject>> = mutableListOf(),
    public var bbox: BoundingBox? = null,
) {
    public operator fun Feature<*, JsonObject>.unaryPlus() {
        features.add(this)
    }

    public fun create(): FeatureCollection<JsonObject> = FeatureCollection(features, bbox)

    public fun feature(
        geometry: Geometry? = null,
        id: String? = null,
        bbox: BoundingBox? = null,
        properties: (JsonObjectBuilder.() -> Unit)? = null,
    ) {
        +Feature(geometry, properties?.let { buildJsonObject { it() } }, id, bbox)
    }
}

@GeoJsonDsl
public inline fun featureCollection(
    block: FeatureCollectionDsl.() -> Unit
): FeatureCollection<JsonObject> = FeatureCollectionDsl().apply(block).create()
