package org.maplibre.spatialk.geojson.dsl

import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position

@GeoJsonDsl
public class PointBuilder {
    private var _coordinates: Position? = null
    public var coordinates: Position
        get() {
            requireNotNull(_coordinates) { "No coordinates provided for the Point" }
            return _coordinates!!
        }
        set(value) {
            _coordinates = value
        }

    public var bbox: BoundingBox? = null

    public fun build(): Point = Point(coordinates, bbox)
}
