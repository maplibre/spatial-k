package org.maplibre.spatialk.geojson.dsl

import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.MultiPoint
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position

/**
 * Builder for constructing [MultiPoint] objects using a DSL.
 *
 * @property bbox An optional [BoundingBox] for this [MultiPoint].
 * @see MultiPoint
 * @see buildMultiPoint
 * @see addPoint
 */
@GeoJsonDsl
public class MultiPointBuilder {
    public var bbox: BoundingBox? = null
    private val points: MutableList<Position> = mutableListOf()

    /**
     * Adds a [Point] to this [MultiPoint].
     *
     * @param longitude The longitude coordinate.
     * @param latitude The latitude coordinate.
     * @param altitude The optional altitude coordinate.
     */
    public fun add(longitude: Double, latitude: Double, altitude: Double? = null) {
        points.add(Position(longitude, latitude, altitude))
    }

    /**
     * Adds a [Point] to this [MultiPoint].
     *
     * @param position The [Position] to add.
     */
    public fun add(position: Position) {
        points.add(position)
    }

    /**
     * Adds a [Point] to this [MultiPoint].
     *
     * @param point The [Point] whose coordinates will be added.
     */
    public fun add(point: Point) {
        points.add(point.coordinates)
    }

    /**
     * Builds the [MultiPoint] from the configured values.
     *
     * @return The constructed [MultiPoint].
     */
    public fun build(): MultiPoint = MultiPoint(points, bbox)
}
