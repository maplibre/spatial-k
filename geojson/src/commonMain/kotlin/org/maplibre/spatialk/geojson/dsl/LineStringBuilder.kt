package org.maplibre.spatialk.geojson.dsl

import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position

@GeoJsonDsl
public class LineStringBuilder {
    public var bbox: BoundingBox? = null
    private val points: MutableList<Position> = mutableListOf()

    public fun add(longitude: Double, latitude: Double, altitude: Double? = null) {
        points.add(Position(longitude, latitude, altitude))
    }

    public fun add(position: Position) {
        points.add(position)
    }

    public fun add(point: Point) {
        points.add(point.coordinates)
    }

    public fun build(): LineString = LineString(points, bbox)
}
