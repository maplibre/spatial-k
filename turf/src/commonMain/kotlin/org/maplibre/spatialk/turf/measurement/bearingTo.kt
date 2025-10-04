@file:JvmName("Measurement")
@file:JvmMultifileClass

package org.maplibre.spatialk.turf.measurement

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmOverloads
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.turf.unitconversion.degreesToRadians
import org.maplibre.spatialk.turf.unitconversion.radiansToDegrees

/**
 * Takes two positions and finds the geographic bearing between them, i.e., the angle measured in
 * degrees from the north line (0 degrees)
 *
 * @param to ending point
 * @param final calculates the final bearing if true
 * @return bearing in decimal degrees, between -180 and 180 degrees (positive clockwise)
 */
@JvmOverloads
public fun Position.bearingTo(to: Position, final: Boolean = false): Double {
    if (final) return finalBearing(this, to)

    val lon1 = degreesToRadians(longitude)
    val lon2 = degreesToRadians(to.longitude)
    val lat1 = degreesToRadians(latitude)
    val lat2 = degreesToRadians(to.latitude)

    val a = sin(lon2 - lon1) * cos(lat2)
    val b = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(lon2 - lon1)

    return radiansToDegrees(atan2(a, b))
}

private fun finalBearing(start: Position, end: Position): Double =
    (end.bearingTo(start) + 180) % 360
