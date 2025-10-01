@file:JvmName("Booleans")
@file:JvmMultifileClass

package org.maplibre.spatialk.turf.booleans

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmOverloads
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.MultiPolygon
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Polygon
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.turf.measurement.bbox

/**
 * Takes a [Point] and a [Polygon] and determines if the point resides inside the polygon. The
 * polygon can be convex or concave. The function accounts for holes.
 *
 * @param point input point
 * @param polygon input polygon
 * @param ignoreBoundary True if the polygon boundary should be ignored when determining if the
 *   point is inside the polygon otherwise false.
 * @return `true` if the Position is inside the Polygon; `false` if the Position is not inside the
 *   Polygon
 */
@JvmOverloads
public fun pointInPolygon(
    point: Point,
    polygon: Polygon,
    ignoreBoundary: Boolean = false,
): Boolean {
    val bbox = bbox(polygon)
    // normalize to multipolygon
    val polys = listOf(polygon.coordinates)
    return pointInPolygon(point.coordinates, bbox, polys, ignoreBoundary)
}

/**
 * Takes a [Point] and a [MultiPolygon] and determines if the point resides inside the polygon. The
 * polygon can be convex or concave. The function accounts for holes.
 *
 * @param point input point
 * @param polygon input multipolygon
 * @param ignoreBoundary True if the polygon boundary should be ignored when determining if the
 *   point is inside the polygon otherwise false.
 * @return `true` if the Position is inside the Polygon; `false` if the Position is not inside the
 *   Polygon
 */
@JvmOverloads
public fun pointInPolygon(
    point: Point,
    polygon: MultiPolygon,
    ignoreBoundary: Boolean = false,
): Boolean {
    val bbox = bbox(polygon)
    val polys = polygon.coordinates
    return pointInPolygon(point.coordinates, bbox, polys, ignoreBoundary)
}

private fun pointInPolygon(
    point: Position,
    bbox: BoundingBox,
    polys: List<List<List<Position>>>,
    ignoreBoundary: Boolean,
): Boolean {
    // Quick elimination if the point is not inside the bbox
    if (!inBBox(point, bbox)) {
        return false
    }
    for (i in polys.indices) {
        // check if it is in the outer ring first
        if (inRing(point, polys[i][0], ignoreBoundary)) {
            var inHole = false
            var k = 1
            // check for the point in any of the holes
            while (k < polys[i].size && !inHole) {
                if (inRing(point, polys[i][k], !ignoreBoundary)) {
                    inHole = true
                }
                k++
            }
            if (!inHole) {
                return true
            }
        }
    }
    return false
}

private fun inRing(point: Position, ring: List<Position>, ignoreBoundary: Boolean): Boolean {
    val (ptLon, ptLat) = point
    var isInside = false
    val openRing =
        if (
            ring[0].longitude == ring.last().longitude && ring[0].latitude == ring.last().latitude
        ) {
            ring.slice(0 until ring.size - 1)
        } else ring
    var i = 0
    var j = openRing.size - 1
    while (i < openRing.size) {
        val xi = openRing[i].longitude
        val yi = openRing[i].latitude
        val xj = openRing[j].longitude
        val yj = openRing[j].latitude
        val onBoundary =
            ptLat * (xi - xj) + yi * (xj - ptLon) + yj * (ptLon - xi) == 0.0 &&
                (xi - ptLon) * (xj - ptLon) <= 0 &&
                (yi - ptLat) * (yj - ptLat) <= 0
        if (onBoundary) {
            return !ignoreBoundary
        }
        val intersect =
            yi > ptLat != yj > ptLat && ptLon < ((xj - xi) * (ptLat - yi)) / (yj - yi) + xi
        if (intersect) {
            isInside = !isInside
        }

        j = i++
    }
    return isInside
}

private fun inBBox(point: Position, boundingBox: BoundingBox): Boolean {
    return boundingBox.west <= point.longitude &&
        boundingBox.south <= point.latitude &&
        boundingBox.east >= point.longitude &&
        boundingBox.north >= point.latitude
}

public operator fun Polygon.contains(point: Point): Boolean = pointInPolygon(point, this)
