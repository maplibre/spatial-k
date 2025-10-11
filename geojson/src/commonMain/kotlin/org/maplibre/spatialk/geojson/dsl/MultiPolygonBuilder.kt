package org.maplibre.spatialk.geojson.dsl

import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.MultiPolygon
import org.maplibre.spatialk.geojson.Polygon
import org.maplibre.spatialk.geojson.Position

@GeoJsonDsl
public class MultiPolygonBuilder {
    public var bbox: BoundingBox? = null
    private val coordinates: MutableList<List<List<Position>>> = mutableListOf()

    public fun add(polygon: Polygon) {
        coordinates.add(polygon.coordinates)
    }

    public fun build(): MultiPolygon = MultiPolygon(coordinates, bbox)
}
