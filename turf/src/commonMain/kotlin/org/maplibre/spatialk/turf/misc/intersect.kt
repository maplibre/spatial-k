@file:JvmName("Miscellaneous")
@file:JvmMultifileClass

package org.maplibre.spatialk.turf.misc

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.Position

/**
 * Returns intersecting points between two [LineString]s.
 *
 * Currently only supports primitive LineStrings containing exactly two points each!
 *
 * @return A list containing any intersections between [line1] and [line2]
 * @throws NotImplementedError if either LineString does not contain exactly two points
 */
public fun intersect(line1: LineString, line2: LineString): List<Position> {
    if (line1.coordinates.size == 2 && line2.coordinates.size == 2) {
        val intersect = intersects(line1, line2)
        return if (intersect != null) listOf(intersect) else emptyList()
    } else {
        throw NotImplementedError("Complex GeoJSON intersections are currently unsupported")
    }
}

/**
 * Find a point that intersects LineStrings with two coordinates each.
 *
 * @param line1 A [LineString] (must contain exactly 2 coordinates)
 * @param line2 A [LineString] (must contain exactly 2 coordinates)
 * @return The position of the intersection, or null if the two lines do not intersect.
 */
private fun intersects(line1: LineString, line2: LineString): Position? {
    require(line1.coordinates.size == 2) { "line1 must contain exactly 2 coordinates" }
    require(line2.coordinates.size == 2) { "line2 must contain exactly 2 coordinates" }

    val x1 = line1.coordinates[0].longitude
    val y1 = line1.coordinates[0].latitude
    val x2 = line1.coordinates[1].longitude
    val y2 = line1.coordinates[1].latitude
    val x3 = line2.coordinates[0].longitude
    val y3 = line2.coordinates[0].latitude
    val x4 = line2.coordinates[1].longitude
    val y4 = line2.coordinates[1].latitude

    val denom = ((y4 - y3) * (x2 - x1)) - ((x4 - x3) * (y2 - y1))
    val numeA = ((x4 - x3) * (y1 - y3)) - ((y4 - y3) * (x1 - x3))
    val numeB = ((x2 - x1) * (y1 - y3)) - ((y2 - y1) * (x1 - x3))

    if (denom == 0.0 || numeA == 0.0 && numeB == 0.0) {
        return null
    }

    val uA = numeA / denom
    val uB = numeB / denom

    if (uA in 0.0..1.0 && uB in 0.0..1.0) {
        val x = x1 + (uA * (x2 - x1))
        val y = y1 + (uA * (y2 - y1))
        return Position(x, y)
    }

    return null
}
