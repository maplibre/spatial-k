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
import org.maplibre.spatialk.units.Bearing
import org.maplibre.spatialk.units.Bearing.Companion.North
import org.maplibre.spatialk.units.extensions.degrees
import org.maplibre.spatialk.units.extensions.inRadians
import org.maplibre.spatialk.units.extensions.radians

/**
 * Takes two positions and finds the geographic bearing between them at the start or end of a great
 * circle arc.
 *
 * @param pos ending point
 * @param final if true, calculate the starting bearing, else the ending bearing
 */
@JvmOverloads
public fun Position.bearingTo(pos: Position, final: Boolean = false): Bearing {
    if (final) return pos.bearingTo(this) + 180.degrees

    val lon1 = longitude.degrees.inRadians
    val lon2 = pos.longitude.degrees.inRadians
    val lat1 = latitude.degrees.inRadians
    val lat2 = pos.latitude.degrees.inRadians

    val a = sin(lon2 - lon1) * cos(lat2)
    val b = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(lon2 - lon1)

    return North + atan2(a, b).radians
}
