@file:OptIn(ExperimentalTurfApi::class)
@file:Suppress("LongMethod", "MagicNumber", "NestedBlockDepth")

package io.github.dellisd.spatialk.turf

import io.github.dellisd.spatialk.geojson.BoundingBox
import io.github.dellisd.spatialk.geojson.MultiPolygon
import io.github.dellisd.spatialk.geojson.Point
import io.github.dellisd.spatialk.geojson.Polygon
import io.github.dellisd.spatialk.geojson.Position
import kotlin.jvm.JvmOverloads

/**
 * Takes a [Point] and a [Polygon] and determines if the point
 * resides inside the polygon. The polygon can be convex or concave. The function accounts for holes.
 *
 * @param point input point
 * @param polygon input polygon
 * @param ignoreBoundary True if polygon boundary should be ignored when determining if
 * the point is inside the polygon otherwise false.
 * @return `true` if the Position is inside the Polygon; `false` if the Position is not inside the Polygon
 */
@ExperimentalTurfApi
@JvmOverloads
public fun booleanPointInPolygon(point: Point, polygon: Polygon, ignoreBoundary: Boolean = false): Boolean {
    val bbox = bbox(polygon)
    // normalize to multipolygon
    val polys = listOf(polygon.coordinates)
    return booleanPointInPolygon(point.coordinates, bbox, polys, ignoreBoundary)
}

/**
 * Takes a [Point] and a [MultiPolygon] and determines if the point
 * resides inside the polygon. The polygon can be convex or concave. The function accounts for holes.
 *
 * @param point input point
 * @param polygon input multipolygon
 * @param ignoreBoundary True if polygon boundary should be ignored when determining if
 * the point is inside the polygon otherwise false.
 * @return `true` if the Position is inside the Polygon; `false` if the Position is not inside the Polygon
 */
@OptIn(ExperimentalTurfApi::class)
@JvmOverloads
public fun booleanPointInPolygon(point: Point, polygon: MultiPolygon, ignoreBoundary: Boolean = false): Boolean {
    val bbox = bbox(polygon)
    val polys = polygon.coordinates
    return booleanPointInPolygon(point.coordinates, bbox, polys, ignoreBoundary)
}

@Suppress("ReturnCount")
private fun booleanPointInPolygon(
    point: Position,
    bbox: BoundingBox,
    polys: List<List<List<Position>>>,
    ignoreBoundary: Boolean
): Boolean {
    // Quick elimination if point is not inside bbox
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
    val pt = point.coordinates
    var isInside = false
    @Suppress("NAME_SHADOWING") val ring = if (
        ring[0].coordinates[0] == ring.last().coordinates[0] &&
        ring[0].coordinates[1] == ring.last().coordinates[1]
    ) {
        ring.slice(0 until ring.size - 1)
    } else {
        ring
    }
    var i = 0
    var j = ring.size - 1
    while (i < ring.size) {
        val xi = ring[i].coordinates[0]
        val yi = ring[i].coordinates[1]
        val xj = ring[j].coordinates[0]
        val yj = ring[j].coordinates[1]
        val onBoundary =
            pt[1] * (xi - xj) + yi * (xj - pt[0]) + yj * (pt[0] - xi) == 0.0 &&
                    (xi - pt[0]) * (xj - pt[0]) <= 0 &&
                    (yi - pt[1]) * (yj - pt[1]) <= 0
        if (onBoundary) {
            return !ignoreBoundary
        }
        val intersect =
            yi > pt[1] != yj > pt[1] &&
                    pt[0] < ((xj - xi) * (pt[1] - yi)) / (yj - yi) + xi
        if (intersect) {
            isInside = !isInside
        }

        j = i++
    }
    return isInside
}

private fun inBBox(point: Position, boundingBox: BoundingBox): Boolean {
    val pt = point.coordinates
    val bbox = boundingBox.coordinates
    return bbox[0] <= pt[0] && bbox[1] <= pt[1] && bbox[2] >= pt[0] && bbox[3] >= pt[1]
}
