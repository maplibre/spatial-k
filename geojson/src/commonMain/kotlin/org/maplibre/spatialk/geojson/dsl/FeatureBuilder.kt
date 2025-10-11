package org.maplibre.spatialk.geojson.dsl

import kotlinx.serialization.json.JsonObject
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.Geometry

@GeoJsonDsl
public class FeatureBuilder<T : Geometry?> {

    private var _geometry: Any? = uninitialized

    public var geometry: T
        get() {
            require(_geometry !== uninitialized) { "No geometry provided for the Feature" }
            @Suppress("UNCHECKED_CAST")
            return _geometry as T
        }
        set(value) {
            _geometry = value
        }

    public var properties: JsonObject? = null
    public var id: String? = null
    public var bbox: BoundingBox? = null

    public fun build(): Feature<T> {
        return Feature(geometry, properties, id, bbox)
    }

    private companion object {
        private val uninitialized = Any()
    }
}
