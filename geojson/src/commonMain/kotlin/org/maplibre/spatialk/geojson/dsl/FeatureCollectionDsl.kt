@file:JvmSynthetic

package org.maplibre.spatialk.geojson.dsl

import kotlin.jvm.JvmSynthetic
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.buildJsonObject
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Geometry

@GeoJsonDsl
public class FeatureCollectionDsl<T : Geometry?>(
    private val features: MutableList<Feature<T>> = mutableListOf(),
    public var bbox: BoundingBox? = null,
) {
    public operator fun Feature<T>.unaryPlus() {
        features.add(this)
    }

    public fun create(): FeatureCollection<T> = FeatureCollection(features, bbox)

    public fun feature(
        geometry: T,
        id: String? = null,
        bbox: BoundingBox? = null,
        properties: (JsonObjectBuilder.() -> Unit)? = null,
    ) {
        +Feature(geometry, properties?.let { buildJsonObject { it() } }, id, bbox)
    }
}

@GeoJsonDsl
public inline fun <T : Geometry?> featureCollection(
    block: FeatureCollectionDsl<T>.() -> Unit
): FeatureCollection<T> = FeatureCollectionDsl<T>().apply(block).create()
