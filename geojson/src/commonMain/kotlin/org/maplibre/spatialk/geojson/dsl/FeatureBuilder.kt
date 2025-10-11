package org.maplibre.spatialk.geojson.dsl

import kotlinx.serialization.json.JsonObject
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.Geometry

@GeoJsonDsl
public class FeatureBuilder<T : Geometry?>(private val geometry: T) {

    public var properties: JsonObject? = null
    public var id: String? = null
    public var bbox: BoundingBox? = null

    public fun build(): Feature<T> {
        return Feature(geometry, properties, id, bbox)
    }
}
