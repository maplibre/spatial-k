package org.maplibre.spatialk.geojson.dsl

import kotlinx.serialization.Serializable
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.Geometry

@GeoJsonDsl
public class FeatureBuilder<T : Geometry?, P : @Serializable Any?>(
    public var geometry: T,
    public var properties: P,
) {
    public var id: String? = null
    public var bbox: BoundingBox? = null

    public fun build(): Feature<T, P> {
        return Feature(geometry, properties, id, bbox)
    }
}
