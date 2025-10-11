package org.maplibre.spatialk.geojson.dsl

import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Geometry

@GeoJsonDsl
public class FeatureCollectionBuilder<T : Geometry?> {
    public var bbox: BoundingBox? = null
    private val features: MutableList<Feature<T>> = mutableListOf()

    public fun add(feature: Feature<T>) {
        features.add(feature)
    }

    public fun build(): FeatureCollection<T> = FeatureCollection(features, bbox)
}
