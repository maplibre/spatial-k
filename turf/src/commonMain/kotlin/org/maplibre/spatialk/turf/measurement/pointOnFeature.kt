@file:JvmName("Measurement")
@file:JvmMultifileClass

package org.maplibre.spatialk.turf.measurement

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.math.sqrt
import org.maplibre.spatialk.geojson.Geometry
import org.maplibre.spatialk.geojson.GeometryCollection
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.MultiLineString
import org.maplibre.spatialk.geojson.MultiPoint
import org.maplibre.spatialk.geojson.MultiPolygon
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Polygon
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.turf.booleans.contains
import org.maplibre.spatialk.turf.coordinatemutation.flattenCoordinates

/**
 * Takes a [Geometry] and returns a [Point] guaranteed to be on the surface of the feature.
 *
 * * Given a [Polygon], the point will be in the area of the polygon
 * * Given a [LineString], the point will be along the string
 * * Given a [Point], the point will the same as the input
 *
 * @return a point on the surface of the input
 */
public fun Geometry.pointOnFeature(): Point {
    val center = (bbox ?: computeBbox()).center()
    return if (isPointOnSurface(this, center)) {
        center
    } else {
        nearestPoint(center.coordinates, this.flattenCoordinates().map(::Point))
    }
}

private fun isPointOnSurface(geometry: Geometry, centroid: Point): Boolean =
    when (geometry) {
        is Point ->
            geometry.coordinates.longitude == centroid.coordinates.longitude &&
                geometry.coordinates.latitude == centroid.coordinates.latitude

        is MultiPoint ->
            geometry.coordinates.any {
                it.longitude == centroid.coordinates.longitude &&
                    it.latitude == centroid.coordinates.latitude
            }

        is LineString ->
            geometry.coordinates.zipWithNext().any { (p1, p2) ->
                pointOnSegment(centroid.coordinates, p1, p2)
            }

        is MultiLineString ->
            geometry.coordinates.any { line ->
                line.zipWithNext().any { (p1, p2) ->
                    pointOnSegment(centroid.coordinates, p1, p2)
                }
            }

        is Polygon -> geometry.contains(centroid.coordinates)
        is MultiPolygon -> geometry.contains(centroid.coordinates)
        is GeometryCollection<*> -> geometry.any { isPointOnSurface(it, centroid) }
    }

private fun pointOnSegment(point: Position, start: Position, end: Position): Boolean {
    val (x, y) = point
    val (x1, y1) = start
    val (x2, y2) = end
    val ab = sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1))
    val ap = sqrt((x - x1) * (x - x1) + (y - y1) * (y - y1))
    val pb = sqrt((x2 - x) * (x2 - x) + (y2 - y) * (y2 - y))
    // Exact float equality is intentional: this mirrors GeoKJSON and Turf.js, and the fixtures pass
    // with it. Do not "fix" it to an epsilon comparison.
    return ab == ap + pb
}

private fun nearestPoint(target: Position, points: List<Point>): Point =
    points[nearestPointIndex(target, points.map { it.coordinates })]
