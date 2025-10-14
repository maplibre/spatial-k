@file:JvmName("Miscellaneous")
@file:JvmMultifileClass

package org.maplibre.spatialk.turf.misc

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlinx.serialization.Serializable
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.LineStringGeometry
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
 * Result properties from [nearestPointTo] of a `Collection<Point>`.
 *
 * @property distance Distance between the input position and the point
 * @property index Index of the point in the collection
 */
@Serializable
public data class NearestPointProps internal constructor(val distance: Length, val index: Int) {
    @PublishedApi
    @Suppress("unused")
    internal fun getDistance(unit: LengthUnit): Double = distance.toDouble(unit)
}

/**
 * Finds the nearest point in the collection to the given position.
 *
 * @param point The position to find the nearest point to.
 * @return The [Point] in the collection that is closest to the given position.
 * @throws NoSuchElementException if the collection is empty.
 */
public fun Collection<Point>.nearestPointTo(point: Point): Feature<Point, NearestPointProps> {
    if (this.isEmpty()) throw NoSuchElementException("No points available.")

    var resultPos = Position(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)
    var resultDistance = Length.PositiveInfinity
    var resultIndex = -1

    this.forEachIndexed { i, candidate ->
        val distance = distance(candidate.coordinates, point.coordinates)
        if (distance < resultDistance) {
            resultPos = candidate.coordinates
            resultDistance = distance
            resultIndex = i
        }
    }

    return Feature(
        geometry = Point(resultPos),
        properties = NearestPointProps(resultDistance, resultIndex),
    )
}

/**
 * Result properties from [nearestPointTo] from a `LineString`.
 *
 * @property distance Distance between the input position and the point
 * @property location Distance along the line from the stat to the the point
 * @property index Index of the segment of the line on which the point lies.
 */
@Serializable
public data class NearestPointOnLineProps
internal constructor(val distance: Length, val location: Length, val index: Int) {
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
public fun LineStringGeometry.nearestPointTo(
    point: Position
): Feature<Point, NearestPointOnLineProps> {
    val lines =
        when (this) {
            is LineString -> listOf(coordinates)
            is MultiLineString -> coordinates
        }
    return findNearestPointOnLine(lines, point)
}

private fun findNearestPointOnLine(
    lines: List<List<Position>>,
    point: Position,
): Feature<Point, NearestPointOnLineProps> {
    var resultPos = Position(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)
    var resultDistance = Length.PositiveInfinity
    var resultLocation = Length.PositiveInfinity
    var resultIndex = -1

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
                intersect(LineString(perpPoint1, perpPoint2), LineString(start, stop))
                    ?.get(0)
                    ?.coordinates

            if (startDistance < resultDistance) {
                resultPos = start
                resultLocation = length
                resultDistance = startDistance
                resultIndex = i
            }

            if (stopDistance < resultDistance) {
                resultPos = stop
                resultLocation = length + sectionLength
                resultDistance = stopDistance
                resultIndex = i + 1
            }

            if (intersect != null && distance(point, intersect) < resultDistance) {
                val intersectDistance = distance(point, intersect)
                resultPos = intersect
                resultDistance = intersectDistance
                resultLocation = length + distance(start, intersect)
                resultIndex = i
            }

            length += sectionLength
        }
    }

    return Feature(
        Point(resultPos),
        NearestPointOnLineProps(resultDistance, resultLocation, resultIndex),
    )
}
