@file:JvmName("Measurement")
@file:JvmMultifileClass

package org.maplibre.spatialk.turf.measurement

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmSynthetic
import kotlin.math.*
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.units.International.Meters
import org.maplibre.spatialk.units.Length
import org.maplibre.spatialk.units.LengthUnit
import org.maplibre.spatialk.units.extensions.degrees
import org.maplibre.spatialk.units.extensions.earthRadians
import org.maplibre.spatialk.units.extensions.inRadians
import org.maplibre.spatialk.units.extensions.times

/**
 * Calculates the distance between two positions. This uses the
 * [Haversine formula](https://en.wikipedia.org/wiki/Haversine_formula) to account for global
 * curvature.
 *
 * @param from origin point
 * @param to destination point
 * @return distance between the two points
 */
@JvmSynthetic
@JvmName("distanceAsLength")
public fun distance(from: Position, to: Position): Length {
    val dLat = (to.latitude - from.latitude).degrees.inRadians
    val dLon = (to.longitude - from.longitude).degrees.inRadians
    val lat1 = from.latitude.degrees.inRadians
    val lat2 = to.latitude.degrees.inRadians

    val a = sin(dLat / 2).pow(2) + sin(dLon / 2).pow(2) * cos(lat1) * cos(lat2)
    return 2.0 * atan2(sqrt(a), sqrt(1 - a)).earthRadians
}

/**
 * Calculates the distance between two points. This uses the
 * [Haversine formula](https://en.wikipedia.org/wiki/Haversine_formula) to account for global
 * curvature.
 *
 * @param from origin point
 * @param to destination point
 * @return distance between the two points
 */
@JvmSynthetic
@JvmName("distanceAsLength")
public fun distance(from: Point, to: Point): Length = distance(from.coordinates, to.coordinates)

@PublishedApi
@Suppress("unused")
@JvmOverloads
internal fun distance(from: Position, to: Position, unit: LengthUnit = Meters): Double =
    distance(from, to).toDouble(unit)

@PublishedApi
@Suppress("unused")
@JvmOverloads
internal fun distance(from: Point, to: Point, unit: LengthUnit = Meters): Double =
    distance(from.coordinates, to.coordinates, unit)

/**
 * Calculates the distance between a given point and the nearest point on a line.
 *
 * @param from point to calculate from
 * @param to line to calculate to
 */
@JvmSynthetic
@JvmName("distanceAsLength")
public fun distance(from: Position, to: LineString): Length {
    var distance = Length.MaxValue

    to.coordinates
        .drop(1)
        .mapIndexed { idx, position -> to.coordinates[idx] to position }
        .forEach { (prev, cur) ->
            val d = distanceToSegment(from, prev, cur)
            if (d < distance) distance = d
        }

    return distance
}

/**
 * Calculates the distance between a given point and the nearest point on a line.
 *
 * @param from point to calculate from
 * @param to line to calculate to
 */
@JvmSynthetic
@JvmName("distanceAsLength")
public fun distance(from: Point, to: LineString): Length = distance(from.coordinates, to)

/**
 * Calculates the distance between a given line and the nearest point on the line to a position.
 *
 * This is a convenience function equivalent to calling `distance(to, from)`.
 *
 * @param from line to calculate from
 * @param to point to calculate to
 * @return distance between the line and the point
 */
@JvmSynthetic
@JvmName("distanceAsLength")
public fun distance(from: LineString, to: Position): Length = distance(to, from)

/**
 * Calculates the distance between a given line and the nearest point on the line to a point.
 *
 * This is a convenience function equivalent to calling `distance(to, from)`.
 *
 * @param from line to calculate from
 * @param to point to calculate to
 * @return distance between the line and the point
 */
@JvmSynthetic
@JvmName("distanceAsLength")
public fun distance(from: LineString, to: Point): Length = distance(to.coordinates, from)

@PublishedApi
@Suppress("unused")
@JvmOverloads
internal fun distance(from: Position, to: LineString, unit: LengthUnit = Meters): Double =
    distance(from, to).toDouble(unit)

@PublishedApi
@Suppress("unused")
@JvmOverloads
internal fun distance(from: Point, to: LineString, unit: LengthUnit = Meters): Double =
    distance(from.coordinates, to, unit)

@PublishedApi
@Suppress("unused")
@JvmOverloads
internal fun distance(from: LineString, to: Position, unit: LengthUnit = Meters): Double =
    distance(to, from).toDouble(unit)

@PublishedApi
@Suppress("unused")
@JvmOverloads
internal fun distance(from: LineString, to: Point, unit: LengthUnit = Meters): Double =
    distance(to.coordinates, from, unit)

private fun distanceToSegment(point: Position, start: Position, end: Position): Length {
    fun dot(u: Position, v: Position): Double {
        return u.longitude * v.longitude + u.latitude * v.latitude
    }

    val segmentVector = Position(end.longitude - start.longitude, end.latitude - start.latitude)
    val pointVector = Position(point.longitude - start.longitude, point.latitude - start.latitude)

    val projectionLengthSquared = dot(pointVector, segmentVector)
    if (projectionLengthSquared <= 0) {
        return rhumbDistance(point, start)
    }
    val segmentLengthSquared = dot(segmentVector, segmentVector)
    if (segmentLengthSquared <= projectionLengthSquared) {
        return rhumbDistance(point, end)
    }

    val projectionRatio = projectionLengthSquared / segmentLengthSquared
    val projectedPoint =
        Position(
            start.longitude + projectionRatio * segmentVector.longitude,
            start.latitude + projectionRatio * segmentVector.latitude,
        )

    return rhumbDistance(point, projectedPoint)
}
