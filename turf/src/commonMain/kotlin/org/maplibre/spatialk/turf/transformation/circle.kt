@file:JvmName("Transformation")
@file:JvmMultifileClass

package org.maplibre.spatialk.turf.transformation

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmSynthetic
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Polygon
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.turf.measurement.computeBbox
import org.maplibre.spatialk.turf.measurement.offset
import org.maplibre.spatialk.units.Bearing
import org.maplibre.spatialk.units.International.Meters
import org.maplibre.spatialk.units.Length
import org.maplibre.spatialk.units.LengthUnit
import org.maplibre.spatialk.units.extensions.degrees
import org.maplibre.spatialk.units.extensions.toLength

/**
 * Takes a [Position] and calculates the circle polygon given a radius [Length] and steps for
 * precision.
 *
 * @param center center point of circle
 * @param radius radius of the circle
 * @param steps the number of steps must be at least four. Default is 64
 */
@JvmSynthetic
public fun circle(center: Position, radius: Length, steps: Int = 64): Polygon {
    require(steps >= 4) { "circle needs to have four or more coordinates." }
    require(radius.isPositive) { "radius must be a positive value" }

    val ring = buildList {
        (0..steps).map { step ->
            val fraction = step.toDouble() / steps
            add(center.offset(radius, Bearing.North - (360.degrees * fraction)))
        }
        add(center.offset(radius, Bearing.North))
    }

    return Polygon(coordinates = listOf(ring), bbox = computeBbox(ring))
}

/**
 * Takes a [Point] and calculates the circle polygon given a radius [Length] and steps for
 * precision.
 *
 * @param center center point of circle
 * @param radius radius of the circle
 * @param steps the number of steps must be at least four. Default is 64
 */
@JvmSynthetic
public fun circle(center: Point, radius: Length, steps: Int = 64): Polygon =
    circle(center.coordinates, radius, steps)

@PublishedApi
@JvmOverloads
@Suppress("unused")
internal fun circle(
    center: Position,
    radius: Double,
    unit: LengthUnit = Meters,
    steps: Int = 64,
): Polygon = circle(center, radius.toLength(unit), steps)

@PublishedApi
@JvmOverloads
@Suppress("unused")
internal fun circle(
    center: Point,
    radius: Double,
    unit: LengthUnit = Meters,
    steps: Int = 64,
): Polygon = circle(center.coordinates, radius, unit, steps)
