@file:JvmName("TurfTransformation")

package org.maplibre.spatialk.turf

import kotlin.jvm.JvmName
import kotlin.math.pow
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Polygon
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.units.Length

/**
 * Takes a [LineString] and returns a curved version by applying a Bezier spline algorithm.
 *
 * The bezier spline implementation is a port of the implementation by
 * [Leszek Rybicki](http://leszek.rybicki.cc/) used in turfjs.
 *
 * @param line the input [LineString]
 * @param duration time in milliseconds between points in the output data
 * @param sharpness a measure of how curvy the path should be between splines
 * @return A [LineString] containing a curved line around the positions of the input line
 */
@ExperimentalTurfApi
public fun bezierSpline(
    line: LineString,
    duration: Int = 10_000,
    sharpness: Double = 0.85,
): LineString = LineString(bezierSpline(line.coordAll(), duration, sharpness))

/**
 * Takes a list of [Position] and returns a curved version by applying a Bezier spline algorithm.
 *
 * The bezier spline implementation is a port of the implementation by
 * [Leszek Rybicki](http://leszek.rybicki.cc/) used in turfjs.
 *
 * @param coords the input list of [Position].
 * @param duration time in milliseconds between points in the output data
 * @param sharpness a measure of how curvy the path should be between splines
 * @return A [List] containing [Position] of a curved line around the positions of the input line
 */
public fun bezierSpline(
    coords: List<Position>,
    duration: Int = 10_000,
    sharpness: Double = 0.85,
): List<Position> {
    // utility function to ensure a given altitude
    fun Position.altitude() = altitude ?: 0.0

    val controls = buildList {
        val centers =
            (0..<coords.lastIndex).map { i ->
                val p1 = coords[i]
                val p2 = coords[i + 1]
                Position(
                    longitude = (p1.longitude + p2.longitude) / 2,
                    latitude = (p1.latitude + p2.latitude) / 2,
                    altitude = (p1.altitude() + p2.altitude()) / 2,
                )
            }

        add(Pair(coords[0], coords[0]))
        for (i in 0..<centers.lastIndex) {
            val dx = coords[i + 1].longitude - (centers[i].longitude + centers[i + 1].longitude) / 2
            val dy = coords[i + 1].latitude - (centers[i].latitude + centers[i + 1].latitude) / 2
            val dz =
                coords[i + 1].altitude() - (centers[i].altitude() + centers[i + 1].altitude()) / 2
            add(
                Pair(
                    Position(
                        longitude =
                            (1.0 - sharpness) * coords[i + 1].longitude +
                                sharpness * (centers[i].longitude + dx),
                        latitude =
                            (1.0 - sharpness) * coords[i + 1].latitude +
                                sharpness * (centers[i].latitude + dy),
                        altitude =
                            (1.0 - sharpness) * coords[i + 1].altitude() +
                                sharpness * (centers[i].altitude() + dz),
                    ),
                    Position(
                        longitude =
                            (1.0 - sharpness) * coords[i + 1].longitude +
                                sharpness * (centers[i + 1].longitude + dx),
                        latitude =
                            (1.0 - sharpness) * coords[i + 1].latitude +
                                sharpness * (centers[i + 1].latitude + dy),
                        altitude =
                            (1.0 - sharpness) * coords[i + 1].altitude() +
                                sharpness * (centers[i + 1].altitude() + dz),
                    ),
                )
            )
        }
        add(Pair(coords[coords.lastIndex], coords[coords.lastIndex]))
    }

    fun bezier(t: Double, p1: Position, c1: Position, c2: Position, p2: Position): Position {
        val t2 = t * t
        val t3 = t2 * t
        val b = listOf(t3, 3 * t2 * (1 - t), 3 * t * (1 - t) * (1 - t), (1 - t) * (1 - t) * (1 - t))
        return Position(
            longitude =
                p2.longitude * b[0] +
                    c2.longitude * b[1] +
                    c1.longitude * b[2] +
                    p1.longitude * b[3],
            latitude =
                p2.latitude * b[0] + c2.latitude * b[1] + c1.latitude * b[2] + p1.latitude * b[3],
            altitude =
                p2.altitude() * b[0] +
                    c2.altitude() * b[1] +
                    c1.altitude() * b[2] +
                    p1.altitude() * b[3],
        )
    }

    fun pos(time: Int): Position {
        var t = time.coerceAtLeast(0)
        if (t > duration) {
            t = duration - 1
        }

        val t2 = t.toDouble() / duration
        if (t2 >= 1) {
            return coords[coords.lastIndex]
        }

        val n = (coords.lastIndex * t2).toInt()
        val t1 = coords.lastIndex * t2 - n
        return bezier(t1, coords[n], controls[n].second, controls[n + 1].first, coords[n + 1])
    }

    val positions =
        (0..<duration step 10).plus(duration).mapNotNull { i ->
            if ((i / 100) % 2 != 0) {
                return@mapNotNull null
            }

            val pos = pos(i)
            Position(pos.longitude, pos.latitude)
        }

    return positions
}

/**
 * Takes a [Point] and calculates the circle polygon given a radius in degrees, radians, miles, or
 * kilometers; and steps for precision.
 *
 * @param center center point of circle
 * @param radius radius of the circle
 * @param steps the number of steps must be at least four. Default is 64
 */
@ExperimentalTurfApi
public fun circle(center: Point, radius: Length, steps: Int = 64): Polygon {
    require(steps >= 4) { "circle needs to have four or more coordinates." }
    require(radius.isPositive) { "radius must be a positive value" }
    val coordinates =
        (0..steps).map { step ->
            destination(center.coordinates, radius, (step * -360) / steps.toDouble())
        }
    val ring = coordinates.plus(coordinates.first())
    return Polygon(coordinates = listOf(ring), bbox = computeBbox(ring))
}

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
