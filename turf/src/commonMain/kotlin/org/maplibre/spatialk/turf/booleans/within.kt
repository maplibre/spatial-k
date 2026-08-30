@file:Suppress("TooManyFunctions")
@file:JvmName("Booleans")
@file:JvmMultifileClass

package org.maplibre.spatialk.turf.booleans

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.Geometry
import org.maplibre.spatialk.geojson.GeometryCollection
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.MultiLineString
import org.maplibre.spatialk.geojson.MultiPoint
import org.maplibre.spatialk.geojson.MultiPolygon
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Polygon
import org.maplibre.spatialk.geojson.PolygonGeometry
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.turf.measurement.computeBbox

/**
 * [within] returns true if [geometry1] is completely within [geometry2]. The interiors of both
 * geometries must intersect and, the interior and boundary of the primary (geometry a) must not
 * intersect the exterior of the secondary (geometry b). [within] returns the exact opposite result
 * of [contains].
 *
 * @throws IllegalStateException when the geometries are not comparable to each other.
 *
 * Note: containment is approximate, matching GeoKJSON and Turf.js.
 *
 * - polygon-in-polygon: only the outer ring of [geometry1] is checked against [geometry2]; a
 *   polygon whose edges bulge outside while all its vertices are inside is not detected.
 * - line-in-line: only the vertices of [geometry1] are checked against [geometry2], and no shared
 *   interior point is required; a line whose vertices all coincide with [geometry2]'s boundary
 *   while its segments leave it is reported as within. (Turf.js additionally probes segment
 *   midpoints for an interior overlap.)
 * - line-in-polygon: only the vertices (and their segment midpoints) are checked, without Turf's
 *   line-splitting; a line that crosses a concave notch of the polygon without a vertex or midpoint
 *   landing outside is not detected.
 */
@Suppress("CyclomaticComplexMethod")
@Throws(IllegalStateException::class)
public fun within(geometry1: Geometry, geometry2: Geometry): Boolean =
    when (geometry1) {
        is Point ->
            when (geometry2) {
                is MultiPoint -> isPointInMultiPoint(geometry1, geometry2)
                is LineString -> pointOnLine(geometry1, geometry2, ignoreEndVertices = true)
                is Polygon -> geometry2.contains(geometry1.coordinates, ignoreBoundary = true)
                is MultiPolygon -> geometry2.contains(geometry1.coordinates, ignoreBoundary = true)
                is GeometryCollection<*>,
                is MultiLineString,
                is Point -> error("geometry2 ${geometry2::class.simpleName} is not supported")
            }

        is MultiPoint ->
            when (geometry2) {
                is MultiPoint -> isMultiPointInMultiPoint(geometry1, geometry2)
                is LineString -> isMultiPointOnLine(geometry1, geometry2)
                is Polygon -> isMultiPointInPoly(geometry1, geometry2)
                is MultiPolygon -> isMultiPointInMultiPoly(geometry1, geometry2)
                is MultiLineString,
                is GeometryCollection<*>,
                is Point -> error("geometry2 ${geometry2::class.simpleName} is not supported")
            }

        is LineString ->
            when (geometry2) {
                is LineString -> isLineOnLine(geometry1, geometry2)
                is Polygon -> isLineInPoly(geometry1, geometry2)
                is MultiPolygon -> isLineInPoly(geometry1, geometry2)
                is GeometryCollection<*>,
                is MultiLineString,
                is MultiPoint,
                is Point -> error("geometry2 ${geometry2::class.simpleName} is not supported")
            }

        is Polygon ->
            when (geometry2) {
                is Polygon -> isPolyInPoly(geometry1, geometry2)
                is MultiPolygon -> isPolyInMultiPoly(geometry1, geometry2)
                is GeometryCollection<*>,
                is LineString,
                is MultiLineString,
                is MultiPoint,
                is Point -> error("geometry2 ${geometry2::class.simpleName} is not supported")
            }

        is GeometryCollection<*>,
        is MultiLineString,
        is MultiPolygon -> error("geometry1 ${geometry1::class.simpleName} is not supported")
    }

private fun isPointInMultiPoint(point: Point, multiPoint: MultiPoint) =
    multiPoint.coordinates.any { position -> position == point.coordinates }

private fun isMultiPointInMultiPoint(multiPoint1: MultiPoint, multiPoint2: MultiPoint) =
    multiPoint2.coordinates.containsAll(multiPoint1.coordinates)

private fun isMultiPointOnLine(multiPoint: MultiPoint, lineString: LineString): Boolean =
    multiPoint.all { pointOnLine(it, lineString) } &&
        multiPoint.any { point -> pointOnLine(point, lineString, true) }

private fun isMultiPointInPoly(multiPoint: MultiPoint, polygon: Polygon): Boolean =
    multiPoint.coordinates.all { polygon.contains(it) } &&
        multiPoint.coordinates.any { polygon.contains(it, ignoreBoundary = true) }

private fun isMultiPointInMultiPoly(multiPoint: MultiPoint, polygon: MultiPolygon): Boolean =
    multiPoint.coordinates.all { polygon.contains(it) } &&
        multiPoint.coordinates.any { polygon.contains(it, ignoreBoundary = true) }

private fun isLineOnLine(lineString1: LineString, lineString2: LineString) =
    lineString1.coordinates.all { position -> pointOnLine(position, lineString2) }

private fun isLineInPoly(linestring: LineString, polygon: PolygonGeometry): Boolean {
    val polyBbox = polygon.bbox ?: polygon.computeBbox()
    val lineBbox = linestring.bbox ?: linestring.computeBbox()

    if (!bboxContains(polyBbox, lineBbox)) {
        return false
    }

    return linestring.coordinates.all { polygon.contains(it) } &&
        (linestring.coordinates.any { polygon.contains(it, ignoreBoundary = true) } ||
            linestring.coordinates.zipWithNext().any { (start, end) ->
                // The segment is a straight line in coordinate space, so probe its planar midpoint
                // rather than the geodesic midpoint, which can wander outside a thin polygon on
                // long or high-latitude segments.
                polygon.contains(
                    Position(
                        longitude = (start.longitude + end.longitude) / 2,
                        latitude = (start.latitude + end.latitude) / 2,
                    ),
                    ignoreBoundary = true,
                )
            })
}

/** Is Polygon1 in Polygon2. Only takes into account outer rings. */
private fun isPolyInPoly(geometry1: Polygon, geometry2: Polygon): Boolean {
    val poly1Bbox = geometry1.bbox ?: geometry1.computeBbox()
    val poly2Bbox = geometry2.bbox ?: geometry2.computeBbox()

    if (!bboxContains(poly2Bbox, poly1Bbox)) {
        return false
    }

    return geometry1.coordinates.firstOrNull()?.all { position ->
        geometry2.contains(position)
    } ?: false
}

private fun isPolyInMultiPoly(geometry1: Polygon, geometry2: MultiPolygon): Boolean {
    val poly1Bbox = geometry1.bbox ?: geometry1.computeBbox()
    val poly2Bbox = geometry2.bbox ?: geometry2.computeBbox()

    if (!bboxContains(poly2Bbox, poly1Bbox)) {
        return false
    }

    return geometry1.coordinates.firstOrNull()?.all { position ->
        geometry2.contains(position)
    } ?: false
}

/** @return true if [bbox1] fully contains [bbox2] */
@Suppress("ReturnCount")
private fun bboxContains(bbox1: BoundingBox, bbox2: BoundingBox): Boolean {
    if (bbox1.west > bbox2.west) return false
    if (bbox1.east < bbox2.east) return false
    if (bbox1.south > bbox2.south) return false
    if (bbox1.north < bbox2.north) return false
    return true
}
