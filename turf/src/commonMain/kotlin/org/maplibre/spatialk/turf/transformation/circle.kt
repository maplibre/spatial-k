@file:JvmName("Transformation")
@file:JvmMultifileClass

package org.maplibre.spatialk.turf.transformation

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Polygon
import org.maplibre.spatialk.turf.measurement.computeBbox
import org.maplibre.spatialk.turf.measurement.destination
import org.maplibre.spatialk.units.Length

/**
 * Takes a [Point] and calculates the circle polygon given a radius in degrees, radians, miles, or
 * kilometers; and steps for precision.
 *
 * @param center center point of circle
 * @param radius radius of the circle
 * @param steps the number of steps must be at least four. Default is 64
 */
public fun circle(center: Point, radius: Length, steps: Int = 64): Polygon {
    require(steps >= 4) { "circle needs to have four or more coordinates." }
    require(radius.isPositive) { "radius must be a positive value" }

    val ring = buildList {
        (0..steps).map { step ->
            add(destination(center.coordinates, radius, (step * -360) / steps.toDouble()))
        }
        add(destination(center.coordinates, radius, 0.0))
    }

    return Polygon(coordinates = listOf(ring), bbox = computeBbox(ring))
}
