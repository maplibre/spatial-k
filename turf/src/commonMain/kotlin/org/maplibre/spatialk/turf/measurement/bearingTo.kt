@file:JvmName("Measurement")
@file:JvmMultifileClass

package org.maplibre.spatialk.turf.measurement

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmOverloads
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.units.Bearing
import org.maplibre.spatialk.units.Bearing.Companion.North
import org.maplibre.spatialk.units.DMS.Degrees
import org.maplibre.spatialk.units.RotationUnit
import org.maplibre.spatialk.units.extensions.*

/**
 * Takes two positions and finds the geographic bearing between them, i.e., the angle measured in
 * degrees from the north line.
 *
 * @param to ending point
 * @param final calculates the final bearing if true
 * @return bearing in decimal degrees, between -180 and 180 degrees (positive clockwise)
 */
@JvmOverloads
@JvmName("bearingToAsBearing")
public fun Position.bearingTo(to: Position, final: Boolean = false): Bearing {
    if (final) return to.bearingTo(this) + 180.degrees

    val lon1 = this.longitude.degrees
    val lon2 = to.longitude.degrees
    val lat1 = this.latitude.degrees
    val lat2 = to.latitude.degrees

    val a = sin(lon2 - lon1) * cos(lat2)
    val b = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(lon2 - lon1)

    return North + atan2(a, b)
}

@PublishedApi
@Suppress("unused")
@JvmOverloads
internal fun bearingTo(
    from: Position,
    to: Position,
    final: Boolean = false,
    unit: RotationUnit = Degrees,
): Double = from.bearingTo(to, final).smallestRotationTo(North).toDouble(unit)
