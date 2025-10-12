package org.maplibre.spatialk.geojson.dsl

import kotlinx.serialization.Serializable
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Geometry

@GeoJsonDsl
public class FeatureCollectionBuilder<T : Geometry?, P : @Serializable Any?> {
    public var bbox: BoundingBox? = null
    private val features: MutableList<Feature<T, P>> = mutableListOf()

    public fun add(feature: Feature<T, P>) {
        features.add(feature)
    }

    public fun build(): FeatureCollection<T, P> = FeatureCollection(features, bbox)
}
