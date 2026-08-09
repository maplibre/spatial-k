@file:Suppress("CyclomaticComplexMethod")
@file:JvmName("Booleans")
@file:JvmMultifileClass

package org.maplibre.spatialk.turf.booleans

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

/**
 * @return true if none of the points common to both geometries intersect the interiors of both
 *   geometries.
 * @throws IllegalStateException when [geometry1] or [geometry2] is a [GeometryCollection]
 *
 * Note: the implementation is approximate in places, matching GeoKJSON and Turf.js.
 *
 * - Point vs Polygon checks every ring (including holes), so a point on a hole boundary touches.
 * - When a polygon is `geometry1` (e.g. `polygonTouchesOther`, `multiPolygonTouchesOther`), only
 *   `ring.first()` (the outer ring) is inspected — a component touching only an inner ring or a
 *   non-first polygon of a [MultiPolygon] can be missed.
 * - MultiPolygon branches inspect only `geometry1.first()` (the first polygon) for most `geometry2`
 *   types, so a touching component that is not the first polygon can be missed — inherited verbatim
 *   from GeoKJSON and Turf.js. The MultiLineString branch checks every polygon.
 */
@Throws(IllegalStateException::class)
public fun touches(geometry1: Geometry, geometry2: Geometry): Boolean {
    if (geometry1.flattenCoordinates().isEmpty() || geometry2.flattenCoordinates().isEmpty()) {
        return false
    }

    return when (geometry1) {
        is Point -> pointTouchesOther(geometry2, geometry1)
        is MultiPoint -> multiPointTouchesOther(geometry2, geometry1)
        is LineString -> lineTouchesOther(geometry1, geometry2)
        is MultiLineString -> multiLineTouchesOther(geometry1, geometry2)
        is Polygon -> polygonTouchesOther(geometry2, geometry1)
        is MultiPolygon -> multiPolygonTouchesOther(geometry2, geometry1)
        else -> error("type of geometry1 is not supported")
    }
}

private fun pointTouchesOther(geometry2: Geometry, geometry1: Point) =
    when (geometry2) {
        is LineString -> isPointOnLineEnd(geometry1, geometry2)
        is MultiLineString -> geometry2.any { line -> touches(geometry1, line) }
        is MultiPoint -> geometry2.coordinates.any { geometry1.coordinates == it }
        is MultiPolygon -> geometry2.any { touches(geometry1, it) }
        is Point -> geometry1.coordinates == geometry2.coordinates
        is Polygon -> geometry2.coordinates.map(::LineString).any { pointOnLine(geometry1, it) }
        else -> error("${geometry2::class.simpleName} as type of geometry2 is not supported ")
    }

private fun multiPointTouchesOther(geometry2: Geometry, geometry1: MultiPoint) =
    when (geometry2) {
        is LineString ->
            geometry1.any { point -> isPointOnLineEnd(point, geometry2) } &&
                geometry1.none { point -> pointOnLine(point, geometry2, true) }

        is MultiLineString -> geometry2.any { line -> touches(geometry1, line) }

        is Point,
        is MultiPoint ->
            geometry1
                .flattenCoordinates()
                .intersect(geometry2.flattenCoordinates().toSet())
                .isNotEmpty()

        is Polygon ->
            geometry1.any { point ->
                pointOnLine(point, geometry2.coordinates.map(::LineString).first())
            } && geometry1.none { point -> geometry2.contains(point.coordinates, true) }

        is MultiPolygon -> geometry2.any { touches(geometry1, it) }
        else -> error("${geometry2::class.simpleName} as type of geometry2 is not supported ")
    }

private fun lineTouchesOther(geometry1: LineString, geometry2: Geometry) =
    when (geometry2) {
        is LineString ->
            (isPointOnLineEnd(geometry1.coordinates.first(), geometry2) ||
                isPointOnLineEnd(geometry1.coordinates.last(), geometry2)) &&
                geometry1.coordinates.none { pointOnLine(it, geometry2, ignoreEndVertices = true) }

        is MultiLineString ->
            geometry2.any { line ->
                (isPointOnLineEnd(geometry1.coordinates.first(), line) ||
                    isPointOnLineEnd(geometry1.coordinates.last(), line)) &&
                    geometry1.coordinates.none { position -> pointOnLine(position, line, true) }
            } &&
                geometry2.none { line ->
                    // A component overlapping the line's interior makes touches false, even if
                    // another
                    // component only touches an endpoint. Overlap exists when either endpoint of
                    // one
                    // geometry is strictly interior to the other (a fully contained component is
                    // detected via its own endpoints, which are interior to the line).
                    geometry1.coordinates.any { position ->
                        position != line.coordinates.first() &&
                            position != line.coordinates.last() &&
                            pointOnLine(position, line)
                    } ||
                        line.coordinates.any { position ->
                            position != geometry1.coordinates.first() &&
                                position != geometry1.coordinates.last() &&
                                pointOnLine(position, geometry1)
                        }
                }

        is MultiPolygon ->
            geometry1.coordinates.any { position ->
                geometry2.any { pointOnLine(position, it.coordinates.map(::LineString).first()) } &&
                    geometry2.none { it.contains(position, true) }
            }

        is Point -> isPointOnLineEnd(geometry2.coordinates, geometry1)
        is MultiPoint ->
            geometry2.coordinates.any { position -> isPointOnLineEnd(position, geometry1) } &&
                geometry2.coordinates.none { position -> pointOnLine(position, geometry1, true) }

        is Polygon ->
            geometry1.coordinates.any { position ->
                pointOnLine(position, geometry2.coordinates.map(::LineString).first())
            } && geometry1.coordinates.none { position -> geometry2.contains(position, true) }

        else -> error("${geometry2::class.simpleName} as type of geometry2 is not supported ")
    }

private fun multiLineTouchesOther(geometry1: MultiLineString, geometry2: Geometry) =
    when (geometry2) {
        is LineString ->
            geometry1.any { line ->
                isPointOnLineEnd(line.coordinates.first(), geometry2) ||
                    isPointOnLineEnd(line.coordinates.last(), geometry2)
            } &&
                geometry2.coordinates.none { point ->
                    geometry1.any { line -> pointOnLine(point, line, true) }
                }

        is MultiLineString -> geometry1.any { line -> touches(line, geometry2) }

        is MultiPoint ->
            geometry1.any { line ->
                geometry2.coordinates.any { point -> isPointOnLineEnd(point, line) }
            } &&
                geometry2.coordinates.none { point ->
                    geometry1.any { line -> pointOnLine(point, line, true) }
                }

        is MultiPolygon -> geometry2.any { polygon -> touches(geometry1, polygon) }

        is Point -> geometry1.any { line -> isPointOnLineEnd(geometry2, line) }
        is Polygon ->
            geometry1.any { line ->
                line.coordinates.any { point ->
                    pointOnLine(point, geometry2.coordinates.map(::LineString).first())
                }
            } &&
                geometry1.none { line ->
                    line.coordinates.any { point -> geometry2.contains(point, true) }
                }

        else -> error("${geometry2::class.simpleName} as type of geometry2 is not supported ")
    }

private fun polygonTouchesOther(geometry2: Geometry, geometry1: Polygon) =
    when (geometry2) {
        is LineString ->
            geometry2.coordinates.any { point ->
                pointOnLine(point, geometry1.coordinates.map(::LineString).first())
            } && geometry2.coordinates.none { point -> geometry1.contains(point, true) }

        is MultiLineString ->
            geometry2
                .flatMap { it.coordinates }
                .let { points ->
                    points.any {
                        pointOnLine(it, geometry1.coordinates.map(::LineString).first())
                    } && points.none { geometry1.contains(it, true) }
                }

        is MultiPolygon ->
            geometry1.coordinates
                .map(::LineString)
                .flatMap { it.coordinates }
                .let { points ->
                    geometry2
                        .map { it.coordinates.map(::LineString).first() }
                        .any { line -> points.any { point -> pointOnLine(point, line) } } &&
                        geometry2.none { polygon ->
                            points.any { point -> polygon.contains(point, true) }
                        }
                }

        is Point -> geometry1.coordinates.map(::LineString).any { pointOnLine(geometry2, it) }
        is MultiPoint ->
            geometry2.any { point ->
                pointOnLine(point, geometry1.coordinates.map(::LineString).first())
            } && geometry2.none { point -> geometry1.contains(point.coordinates, true) }

        is Polygon ->
            geometry1.coordinates.map(::LineString).first().coordinates.let { points ->
                points.any { point ->
                    pointOnLine(point, geometry2.coordinates.map(::LineString).first())
                } && points.none { point -> geometry2.contains(point, true) }
            }

        else -> error("type of geometry2 is not supported")
    }

private fun multiPolygonTouchesOther(geometry2: Geometry, geometry1: MultiPolygon) =
    when (geometry2) {
        is LineString ->
            geometry1.first().coordinates.map(::LineString).any { line ->
                geometry2.coordinates.any { point -> pointOnLine(point, line) } &&
                    geometry2.coordinates.none { point -> geometry1.first().contains(point, true) }
            }

        is MultiLineString ->
            geometry2.any { line ->
                line.coordinates.any { point ->
                    geometry1.any { polygon ->
                        pointOnLine(point, polygon.coordinates.map(::LineString).first())
                    }
                }
            } &&
                geometry2.none { line ->
                    line.coordinates.any { point ->
                        geometry1.any { polygon -> polygon.contains(point, true) }
                    }
                }

        is MultiPolygon ->
            geometry2.let { polygons ->
                geometry1
                    .first()
                    .coordinates
                    .map(::LineString)
                    .flatMap { it.coordinates }
                    .any { point ->
                        polygons
                            .flatMap { it.coordinates.map(::LineString) }
                            .any { line -> pointOnLine(point, line) } &&
                            polygons.none { polygon -> polygon.contains(point, true) }
                    }
            }

        is Point ->
            geometry1.first().coordinates.map(::LineString).any { pointOnLine(geometry2, it) }
        is MultiPoint ->
            geometry1.first().coordinates.map(::LineString).any { line ->
                geometry2.any { point -> pointOnLine(point, line) } &&
                    geometry2.none { point -> geometry1.first().contains(point.coordinates, true) }
            }

        is Polygon ->
            geometry1
                .flatMap { it.coordinates.map(::LineString) }
                .flatMap { it.coordinates }
                .let { points ->
                    points.any {
                        pointOnLine(it, geometry2.coordinates.map(::LineString).first())
                    } && points.none { geometry2.contains(it, true) }
                }

        else -> error("${geometry2::class.simpleName} as type of geometry2 is not supported ")
    }

private fun isPointOnLineEnd(point: Point, lineString: LineString): Boolean =
    isPointOnLineEnd(point.coordinates, lineString)

// Compare in two dimensions only: the other touches predicates (pointOnLine, polygon contains)
// ignore altitude, so endpoint contact must not depend on the optional z coordinate.
private fun isPointOnLineEnd(position: Position, lineString: LineString): Boolean {
    val first = lineString.coordinates.first()
    val last = lineString.coordinates.last()
    return (position.longitude == first.longitude && position.latitude == first.latitude) ||
        (position.longitude == last.longitude && position.latitude == last.latitude)
}
