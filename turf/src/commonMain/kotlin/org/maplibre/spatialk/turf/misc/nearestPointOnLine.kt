@file:JvmName("Miscellaneous")
@file:JvmMultifileClass

package org.maplibre.spatialk.turf.misc

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.MultiLineString
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.turf.measurement.bearing
import org.maplibre.spatialk.turf.measurement.destination
import org.maplibre.spatialk.turf.measurement.distance
import org.maplibre.spatialk.units.Length

/**
 * Result values from [nearestPointOnLine].
 *
 * @property point The point on the line nearest to the input position
 * @property distance Distance between the input position and [point]
 * @property location Distance along the line from the stat to the [point]
 * @property index Index of the segment of the line on which [point] lies.
 */
public data class NearestPointOnLineResult(
    val point: Position,
    val distance: Length,
    val location: Length,
    val index: Int,
)

/**
 * Finds the closest [Position] along a [LineString] to a given position
 *
 * @param line The [LineString] to find a position along
 * @param point The [Position] given to find the closest point along the [line]
 * @return The closest position along the line
 */
public fun nearestPointOnLine(line: LineString, point: Position): NearestPointOnLineResult {
    return nearestPointOnLine(listOf(line.coordinates), point)
}

/**
 * Finds the closest [Position] along a [MultiLineString] to a given position
 *
 * @param lines The [MultiLineString] to find a position along
 * @param point The [Position] given to find the closest point along the [lines]
 * @return The closest position along the lines
 */
public fun nearestPointOnLine(lines: MultiLineString, point: Position): NearestPointOnLineResult {
    return nearestPointOnLine(lines.coordinates, point)
}

internal fun nearestPointOnLine(
    lines: List<List<Position>>,
    point: Position,
): NearestPointOnLineResult {
    var closest =
        NearestPointOnLineResult(
            Position(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY),
            Length.POSITIVE_INFINITY,
            Length.POSITIVE_INFINITY,
            -1,
        )

    var length = Length.ZERO

    lines.forEach { coords ->
        for (i in 0 until coords.size - 1) {
            val start = coords[i]
            val startDistance = distance(point, coords[i])
            val stop = coords[i + 1]
            val stopDistance = distance(point, coords[i + 1])

            val sectionLength = distance(start, stop)

            val heightDistance = maxOf(startDistance, stopDistance)
            val direction = bearing(start, stop)
            val perpPoint1 = destination(point, heightDistance, direction + 90)
            val perpPoint2 = destination(point, heightDistance, direction - 90)

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

            if (intersect != null && distance(point, intersect) < closest.distance) {
                val intersectDistance = distance(point, intersect)
                closest =
                    closest.copy(
                        point = intersect,
                        distance = intersectDistance,
                        location = length + distance(start, intersect),
                        index = i,
                    )
            }

            length += sectionLength
        }
    }

    return closest
}
