@file:JvmName("TurfMiscellaneous")

package org.maplibre.spatialk.turf

import kotlin.jvm.JvmName
import kotlin.math.max
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.MultiLineString
import org.maplibre.spatialk.geojson.Position

/**
 * Returns intersecting points between two [LineString]s.
 *
 * Currently only supports primitive LineStrings containing exactly two points each!
 *
 * @return A list containing any intersections between [line1] and [line2]
 * @throws NotImplementedError if either LineString does not contain exactly two points
 */
@ExperimentalTurfApi
public fun lineIntersect(line1: LineString, line2: LineString): List<Position> {
    if (line1.coordinates.size == 2 && line2.coordinates.size == 2) {
        val intersect = intersects(line1, line2)
        return if (intersect != null) listOf(intersect) else emptyList()
    } else {
        throw NotImplementedError("Complex GeoJSON intersections are currently unsupported")
    }
}

/**
 * Find a point that intersects LineStrings with two coordinates each
 *
 * @param line1 A [LineString] (must contain exactly 2 coordinates)
 * @param line2 A [LineString] (must contain exactly 2 coordinates)
 * @return The position of the intersection, or null if the two lines do not intersect.
 */
@ExperimentalTurfApi
internal fun intersects(line1: LineString, line2: LineString): Position? {
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

/**
 * Takes a [LineString], a start and a stop [Position] and returns a subsection of the line between
 * those points. The start and stop points do not need to fall exactly on the line.
 *
 * @param start Start position
 * @param stop Stop position
 * @param line The [LineString] to slice
 * @return The sliced subsection of the line
 */
@ExperimentalTurfApi
public fun lineSlice(start: Position, stop: Position, line: LineString): LineString {
    val startVertex = nearestPointOnLine(line, start)
    val stopVertex = nearestPointOnLine(line, stop)

    val (startPos, endPos) =
        if (startVertex.index <= stopVertex.index) startVertex to stopVertex
        else stopVertex to startVertex

    val positions = mutableListOf(startPos.point)
    for (i in startPos.index + 1 until endPos.index + 1) {
        positions.add(line.coordinates[i])
    }
    positions.add(endPos.point)

    return LineString(positions)
}

/**
 * Result values from [nearestPointOnLine].
 *
 * @property point The point on the line nearest to the input position
 * @property distance Distance between the input position and [point]
 * @property location Distance along the line from the stat to the [point]
 * @property index Index of the segment of the line on which [point] lies.
 */
@ExperimentalTurfApi
public data class NearestPointOnLineResult(
    val point: Position,
    val distance: Double,
    val location: Double,
    val index: Int,
)

/**
 * Finds the closest [Position] along a [LineString] to a given position
 *
 * @param line The [LineString] to find a position along
 * @param point The [Position] given to find the closest point along the [line]
 * @return The closest position along the line
 */
@ExperimentalTurfApi
public fun nearestPointOnLine(
    line: LineString,
    point: Position,
    units: Units = Units.Kilometers,
): NearestPointOnLineResult {
    return nearestPointOnLine(listOf(line.coordinates), point, units)
}

/**
 * Finds the closest [Position] along a [MultiLineString] to a given position
 *
 * @param lines The [MultiLineString] to find a position along
 * @param point The [Position] given to find the closest point along the [lines]
 * @return The closest position along the lines
 */
@ExperimentalTurfApi
public fun nearestPointOnLine(
    lines: MultiLineString,
    point: Position,
    units: Units = Units.Kilometers,
): NearestPointOnLineResult {
    return nearestPointOnLine(lines.coordinates, point, units)
}

@ExperimentalTurfApi
internal fun nearestPointOnLine(
    lines: List<List<Position>>,
    point: Position,
    units: Units = Units.Kilometers,
): NearestPointOnLineResult {
    var closest =
        NearestPointOnLineResult(
            Position(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY),
            Double.POSITIVE_INFINITY,
            Double.POSITIVE_INFINITY,
            -1,
        )

    var length = 0.0

    lines.forEach { coords ->
        for (i in 0 until coords.size - 1) {
            val start = coords[i]
            val startDistance = distance(point, coords[i], units = units)
            val stop = coords[i + 1]
            val stopDistance = distance(point, coords[i + 1], units = units)

            val sectionLength = distance(start, stop, units = units)

            val heightDistance = max(startDistance, stopDistance)
            val direction = bearing(start, stop)
            val perpPoint1 = destination(point, heightDistance, direction + 90, units = units)
            val perpPoint2 = destination(point, heightDistance, direction - 90, units = units)

            val intersect =
                lineIntersect(LineString(perpPoint1, perpPoint2), LineString(start, stop))
                    .getOrNull(0)

            if (startDistance < closest.distance) {
                closest =
                    closest.copy(
                        point = start,
                        location = length,
                        distance = startDistance,
                        index = i,
                    )
            }

            if (stopDistance < closest.distance) {
                closest =
                    closest.copy(
                        point = stop,
                        location = length + sectionLength,
                        distance = stopDistance,
                        index = i + 1,
                    )
            }

            if (intersect != null && distance(point, intersect, units = units) < closest.distance) {
                val intersectDistance = distance(point, intersect, units = units)
                closest =
                    closest.copy(
                        point = intersect,
                        distance = intersectDistance,
                        location = length + distance(start, intersect, units = units),
                        index = i,
                    )
            }

            length += sectionLength
        }
    }

    return closest
}
