@file:JvmName("Booleans")
@file:JvmMultifileClass

package org.maplibre.spatialk.turf.booleans

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.MultiPolygon
import org.maplibre.spatialk.geojson.Polygon
import org.maplibre.spatialk.geojson.PolygonGeometry
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.turf.measurement.computeBbox

/**
 * Takes a [Position] and one or more [Polygon] and determines if the position resides inside the
 * polygons. The polygons can be convex or concave. The function accounts for holes. A position on
 * the boundary is considered to be inside.
 *
 * @return true if the [Position] is inside the [Polygon]. A position on the boundary is considered
 *   to be inside.
 */
public operator fun PolygonGeometry.contains(pos: Position): Boolean = this.contains(pos, false)

/**
 * Takes a [Position] and one or more [Polygon] and determines if the position resides inside the
 * polygons. The polygons can be convex or concave. The function accounts for holes.
 *
 * @param pos input point
 * @param ignoreBoundary True if the polygon boundary should not be considered to be inside the
 *   polygon.
 * @return `true` if the Position is inside the Polygon; `false` if the Position is not inside the
 *   Polygon
 */
public fun PolygonGeometry.contains(pos: Position, ignoreBoundary: Boolean): Boolean {
    val bbox = this.computeBbox()
    // normalize to multipolygon
    val polys =
        when (this) {
            is Polygon -> listOf(this.coordinates)
            is MultiPolygon -> this.coordinates
        }
    return pointInPolygon(pos, bbox, polys, ignoreBoundary)
}

private fun pointInPolygon(
    pos: Position,
    bbox: BoundingBox,
    polys: List<List<List<Position>>>,
    ignoreBoundary: Boolean,
): Boolean {
    // Quick elimination if the point is not inside the bbox
    if (!inBBox(pos, bbox)) {
        return false
    }
    for (i in polys.indices) {
        // check if it is in the outer ring first
        if (inRing(pos, polys[i][0], ignoreBoundary)) {
            var inHole = false
            var k = 1
            // check for the point in any of the holes
            while (k < polys[i].size && !inHole) {
                if (inRing(pos, polys[i][k], !ignoreBoundary)) {
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

private fun inBBox(pos: Position, boundingBox: BoundingBox): Boolean {
    return boundingBox.west <= pos.longitude &&
        boundingBox.south <= pos.latitude &&
        boundingBox.east >= pos.longitude &&
        boundingBox.north >= pos.latitude
}
