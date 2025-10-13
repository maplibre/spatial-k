package org.maplibre.spatialk.geojson.dsl

import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position

/**
 * Builder for constructing [LineString] objects using a DSL.
 *
 * @property bbox An optional [BoundingBox] for this [LineString].
 * @see LineString
 * @see buildLineString
 */
@GeoJsonDsl
public class LineStringBuilder {
    public var bbox: BoundingBox? = null
    private val points: MutableList<Position> = mutableListOf()

    /**
     * Adds a [Position] to this [LineString].
     *
     * @param longitude The longitude coordinate.
     * @param latitude The latitude coordinate.
     * @param altitude The optional altitude coordinate.
     */
    public fun add(longitude: Double, latitude: Double, altitude: Double? = null) {
        points.add(Position(longitude, latitude, altitude))
    }

    /**
     * Adds a [Position] to this [LineString].
     *
     * @param position The [Position] to add.
     */
    public fun add(position: Position) {
        points.add(position)
    }

    /**
     * Adds a [Point] to this [LineString].
     *
     * @param point The [Point] whose coordinates will be added.
     */
    public fun add(point: Point) {
        points.add(point.coordinates)
    }

    /**
     * Builds the [LineString] from the configured values.
     *
     * @return The constructed [LineString].
     */
    public fun build(): LineString = LineString(points, bbox)
}
