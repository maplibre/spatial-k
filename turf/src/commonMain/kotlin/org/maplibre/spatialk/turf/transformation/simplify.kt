package org.maplibre.spatialk.turf.transformation

import kotlin.math.pow
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.Position

private fun getSqDist(p1: Position, p2: Position): Double {
    val dx = p1.longitude - p2.longitude
    val dy = p1.latitude - p2.latitude
    return dx * dx + dy * dy
}

private fun getSqSegDist(p: Position, p1: Position, p2: Position): Double {
    var x = p1.longitude
    var y = p1.latitude
    var dx = p2.longitude - x
    var dy = p2.latitude - y

    if (dx != 0.0 || dy != 0.0) {
        val t = ((p.longitude - x) * dx + (p.latitude - y) * dy) / (dx * dx + dy * dy)

        if (t > 1) {
            x = p2.longitude
            y = p2.latitude
        } else if (t > 0) {
            x += dx * t
            y += dy * t
        }
    }

    dx = p.longitude - x
    dy = p.latitude - y

    return dx * dx + dy * dy
}

private fun simplifyRadialDist(points: List<Position>, sqTolerance: Double): List<Position> {
    if (points.isEmpty()) return emptyList()

    var prevPoint = points[0]
    val newPoints = mutableListOf(prevPoint)
    var point: Position? = null

    for (i in 1 until points.size) {
        point = points[i]

        if (getSqDist(point, prevPoint) > sqTolerance) {
            newPoints.add(point)
            prevPoint = point
        }
    }
    point?.let { if (prevPoint != it) newPoints.add(it) }

    return newPoints
}

private fun simplifyDPStep(
    points: List<Position>,
    first: Int,
    last: Int,
    sqTolerance: Double,
    simplified: MutableList<Position>,
) {
    var maxSqDist = sqTolerance
    var index: Int? = null

    for (i in first + 1 until last) {
        val sqDist = getSqSegDist(points[i], points[first], points[last])

        if (sqDist > maxSqDist) {
            index = i
            maxSqDist = sqDist
        }
    }

    index?.let {
        if (maxSqDist > sqTolerance) {
            if (it - first > 1) simplifyDPStep(points, first, it, sqTolerance, simplified)
            simplified.add(points[it])
            if (last - it > 1) simplifyDPStep(points, it, last, sqTolerance, simplified)
        }
    }
}

private fun simplifyDouglasPeucker(points: List<Position>, sqTolerance: Double): List<Position> {
    if (points.isEmpty()) return emptyList()
    val last = points.size - 1

    val simplified = mutableListOf(points[0])
    simplifyDPStep(points, 0, last, sqTolerance, simplified)
    simplified.add(points[last])

    return simplified
}

/**
 * Reduces the number of points in a LineString while preserving its general shape.
 *
 * @param lineString The LineString to simplify.
 * @param tolerance The tolerance for simplification (in the units of the coordinates). A higher
 *   tolerance results in more simplification (fewer points). If `null`, a default tolerance of
 *   `1.0` is used.
 * @param highestQuality If `true`, the radial distance simplification step is skipped, potentially
 *   resulting in a higher quality simplification at the cost of performance.
 * @return A new, simplified LineString.
 */
public fun simplify(
    lineString: LineString,
    tolerance: Double? = null,
    highestQuality: Boolean = false,
): LineString {
    if (lineString.coordinates.size <= 2) return lineString

    val sqTolerance = tolerance?.pow(2) ?: 1.0

    val simplifiedPoints =
        if (highestQuality) lineString.coordinates
        else simplifyRadialDist(lineString.coordinates, sqTolerance)

    return LineString(simplifyDouglasPeucker(simplifiedPoints, sqTolerance))
}
