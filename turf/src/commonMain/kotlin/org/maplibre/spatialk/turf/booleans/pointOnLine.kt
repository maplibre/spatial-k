@file:JvmName("Booleans")
@file:JvmMultifileClass

package org.maplibre.spatialk.turf.booleans

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.math.abs
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position

/**
 * Returns true if a point is on a line. Accepts an optional parameter to ignore the start and end
 * vertices of the linestring.
 *
 * @param ignoreEndVertices whether to ignore the start and end vertices
 * @param epsilon Fractional number to compare with the cross product result. Useful for dealing
 *   with floating points such as lng/lat points
 * @return `true` if a point is on a line
 */
public fun pointOnLine(
    point: Point,
    line: LineString,
    ignoreEndVertices: Boolean = false,
    epsilon: Double? = null,
): Boolean = pointOnLine(point.coordinates, line, ignoreEndVertices, epsilon)

/**
 * Returns true if a position is on a line. Accepts an optional parameter to ignore the start and
 * end vertices of the linestring.
 *
 * @param ignoreEndVertices whether to ignore the start and end vertices
 * @param epsilon Fractional number to compare with the cross product result. Useful for dealing
 *   with floating points such as lng/lat points
 * @return `true` if a position is on a line
 */
public fun pointOnLine(
    position: Position,
    line: LineString,
    ignoreEndVertices: Boolean = false,
    epsilon: Double? = null,
): Boolean {
    val lineCoordinates = line.coordinates
    return lineCoordinates.windowed(2).any { (start, end) ->
        val isStart = start == lineCoordinates.first()
        val isEnd = end == lineCoordinates.last()
        val ignoreBoundary =
            when {
                ignoreEndVertices && (isStart && isEnd) -> BoundaryExclusion.BOTH
                ignoreEndVertices && isStart -> BoundaryExclusion.START
                ignoreEndVertices && isEnd -> BoundaryExclusion.END
                else -> null
            }
        isPointOnLineSegment(start, end, position, ignoreBoundary, epsilon)
    }
}

private enum class BoundaryExclusion {
    START,
    END,
    BOTH,
}

@Suppress("CyclomaticComplexMethod", "ReturnCount")
private fun isPointOnLineSegment(
    lineSegmentStart: Position,
    lineSegmentEnd: Position,
    point: Position,
    excludeBoundary: BoundaryExclusion?,
    epsilon: Double?,
): Boolean {
    val x = point.longitude
    val y = point.latitude
    val x1 = lineSegmentStart.longitude
    val y1 = lineSegmentStart.latitude
    val x2 = lineSegmentEnd.longitude
    val y2 = lineSegmentEnd.latitude
    val dxc = x - x1
    val dyc = y - y1
    val dxl = x2 - x1
    val dyl = y2 - y1
    val cross = dxc * dyl - dyc * dxl

    if (epsilon != null) {
        if (abs(cross) > epsilon) {
            return false
        }
    } else if (cross != 0.0) {
        return false
    }

    // A degenerate segment (start == end) contains exactly its own coordinate; the bounding checks
    // below would otherwise exclude it because both dxl and dyl are 0.
    val isOnLine =
        if (dxl == 0.0 && dyl == 0.0) {
            x == x1 && y == y1
        } else if (abs(dxl) >= abs(dyl)) {
            dxl > 0.0 && x1 <= x && x <= x2 || dxl < 0.0 && x2 <= x && x <= x1
        } else {
            dyl > 0.0 && y1 <= y && y <= y2 || dyl < 0.0 && y2 <= y && y <= y1
        }

    return when (excludeBoundary) {
        BoundaryExclusion.START -> isOnLine && !(x == x1 && y == y1)
        BoundaryExclusion.END -> isOnLine && !(x == x2 && y == y2)
        BoundaryExclusion.BOTH -> isOnLine && !((x == x1 && y == y1) || (x == x2 && y == y2))
        null -> isOnLine
    }
}
