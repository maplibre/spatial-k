@file:JvmName("Miscellaneous")
@file:JvmMultifileClass

package org.maplibre.spatialk.turf.misc

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.MultiLineString
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.turf.measurement.bearingTo
import org.maplibre.spatialk.turf.measurement.distance
import org.maplibre.spatialk.turf.measurement.offset
import org.maplibre.spatialk.units.Length
import org.maplibre.spatialk.units.LengthUnit
import org.maplibre.spatialk.units.extensions.degrees

/**
 * Finds the nearest point in the collection to the given position.
 *
 * @param point The position to find the nearest point to.
 * @return The [Point] in the collection that is closest to the given position.
 * @throws NoSuchElementException if the collection is empty.
 */
public fun Collection<Point>.nearestPointTo(point: Position): Point = minBy {
    distance(it.coordinates, point)
}

/**
 * Result values from [findNearestPointOnLine].
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
) {
    @PublishedApi
    @Suppress("unused")
    internal fun getDistance(unit: LengthUnit): Double = distance.toDouble(unit)

    @PublishedApi
    @Suppress("unused")
    internal fun getLocation(unit: LengthUnit): Double = location.toDouble(unit)
}

/**
 * Finds the closest [Position] along a [LineString] to a given position.
 *
 * @param point The [Position] given to find the closest point along the [LineString]
 * @return The closest position along the line
 */
public fun LineString.nearestPointTo(point: Position): NearestPointOnLineResult =
    findNearestPointOnLine(listOf(coordinates), point)

/**
 * Finds the closest [Position] along a [MultiLineString] to a given position.
 *
 * @param point The [Position] given to find the closest point along the [MultiLineString]
 * @return The closest position along the lines
 */
public fun MultiLineString.nearestPointTo(point: Position): NearestPointOnLineResult =
    findNearestPointOnLine(coordinates, point)

private fun findNearestPointOnLine(
    lines: List<List<Position>>,
    point: Position,
): NearestPointOnLineResult {
    var closest =
        NearestPointOnLineResult(
            Position(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY),
            Length.PositiveInfinity,
            Length.PositiveInfinity,
            -1,
        )

    var length = Length.Zero

    lines.forEach { coords ->
        for (i in 0 until coords.size - 1) {
            val start = coords[i]
            val startDistance = distance(point, coords[i])
            val stop = coords[i + 1]
            val stopDistance = distance(point, coords[i + 1])

            val sectionLength = distance(start, stop)

            val heightDistance = maxOf(startDistance, stopDistance)
            val direction = start.bearingTo(stop)
            val perpPoint1 = point.offset(heightDistance, direction + 90.degrees)
            val perpPoint2 = point.offset(heightDistance, direction - 90.degrees)

            val intersect =
                intersect(LineString(perpPoint1, perpPoint2), LineString(start, stop)).getOrNull(0)

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
