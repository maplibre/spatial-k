@file:JvmName("Transformation")
@file:JvmMultifileClass

package org.maplibre.spatialk.turf.transformation

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import org.maplibre.spatialk.geojson.Geometry
import org.maplibre.spatialk.geojson.GeometryCollection
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.MultiLineString
import org.maplibre.spatialk.geojson.MultiPoint
import org.maplibre.spatialk.geojson.MultiPolygon
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Polygon
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.turf.coordinatemutation.flattenCoordinates
import org.maplibre.spatialk.turf.measurement.center
import org.maplibre.spatialk.turf.measurement.centroid
import org.maplibre.spatialk.turf.measurement.computeBbox
import org.maplibre.spatialk.turf.measurement.rhumbBearingTo
import org.maplibre.spatialk.turf.measurement.rhumbDistance
import org.maplibre.spatialk.turf.measurement.rhumbOffset

/**
 * Scale a [Geometry] from a given point by a factor of scaling (ex: factor=2 would make the
 * geometry 200% larger).
 *
 * When a [Position] carries an altitude, the altitude is scaled by the same factor (matching
 * Turf.js `transformScale`, which multiplies the z coordinate by the factor). GeoKJSON's `scale`
 * drops altitude; spatial-k preserves it.
 *
 * @param factor of scaling, positive values greater than 0. Numbers between 0 and 1 will shrink the
 *   geojson, numbers greater than 1 will expand it, a factor of 1 will not change the geojson.
 * @param origin point from which the scaling will occur
 * @return scaled [Geometry]
 * @throws IllegalArgumentException if the scaling factor is not positive
 */
@Throws(IllegalArgumentException::class)
@Suppress("UNCHECKED_CAST")
public fun <T : Geometry> T.scale(factor: Double, origin: ScaleOrigin = ScaleOrigin.Centroid): T {
    require(factor > 0.0) { "invalid scaling factor. Must be a positive value" }
    if (factor == 1.0) return this
    val originPosition = defineOrigin(origin)

    fun scaledPosition(pos: Position): Position {
        val originalDistance = rhumbDistance(originPosition, pos)
        val bearing = originPosition.rhumbBearingTo(pos)
        val newDistance = originalDistance * factor
        val scaled = originPosition.rhumbOffset(newDistance, bearing)
        // Preserve and scale altitude, matching Turf.js transformScale:
        // if (coord.length === 3) coord[2] *= factor
        return pos.altitude?.let { Position(scaled.longitude, scaled.latitude, it * factor) }
            ?: scaled
    }

    val newGeometry: Geometry =
        when (this) {
            is Point -> Point(scaledPosition(this.coordinates))
            is MultiPoint -> MultiPoint(this.coordinates.map(::scaledPosition))
            is LineString -> LineString(this.coordinates.map(::scaledPosition))
            is MultiLineString ->
                MultiLineString(this.coordinates.map { line -> line.map(::scaledPosition) })
            is Polygon -> Polygon(this.coordinates.map { line -> line.map(::scaledPosition) })
            is MultiPolygon ->
                MultiPolygon(
                    this.coordinates.map { polygon ->
                        polygon.map { line -> line.map(::scaledPosition) }
                    }
                )
            is GeometryCollection<*> ->
                GeometryCollection(
                    this.geometries.map { geometry ->
                        geometry.scale(factor, ScaleOrigin.Coordinates(originPosition))
                    }
                )
        }
    return newGeometry as T
}

private fun <T : Geometry> T.defineOrigin(origin: ScaleOrigin): Position {
    val boundingBox = bbox ?: computeBbox(flattenCoordinates())
    val (east, north) = boundingBox.northeast
    val (west, south) = boundingBox.southwest

    return when (origin) {
        ScaleOrigin.SouthWest -> Position(west, south)
        ScaleOrigin.SouthEast -> Position(east, south)
        ScaleOrigin.NorthWest -> Position(west, north)
        ScaleOrigin.NorthEast -> Position(east, north)
        ScaleOrigin.Center -> boundingBox.center().coordinates
        ScaleOrigin.Centroid -> centroid().coordinates
        is ScaleOrigin.Coordinates -> origin.position
    }
}

/** Option to define an origin, at which point of an origin geometry the scaling will occur. */
public sealed class ScaleOrigin {
    /** Use the southwestern corner of the geometry's bounding box as the origin. */
    public data object SouthWest : ScaleOrigin()

    /** Use the southeastern corner of the geometry's bounding box as the origin. */
    public data object SouthEast : ScaleOrigin()

    /** Use the northwestern corner of the geometry's bounding box as the origin. */
    public data object NorthWest : ScaleOrigin()

    /** Use the northeastern corner of the geometry's bounding box as the origin. */
    public data object NorthEast : ScaleOrigin()

    /** Use the center of the geometry's bounding box as the origin. */
    public data object Center : ScaleOrigin()

    /** Use the geometry's centroid as the origin. */
    public data object Centroid : ScaleOrigin()

    /**
     * Use an explicit position as the origin.
     *
     * @property position the point from which the scaling will occur.
     */
    public data class Coordinates(public val position: Position) : ScaleOrigin()
}
