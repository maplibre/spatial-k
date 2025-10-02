@file:JvmName("Measurement")
@file:JvmMultifileClass

package org.maplibre.spatialk.turf.measurement

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.math.*
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.turf.unitconversion.degreesToRadians
import org.maplibre.spatialk.units.Length
import org.maplibre.spatialk.units.extensions.earthRadians
import org.maplibre.spatialk.units.extensions.times

/**
 * Calculates the distance between two positions. This uses the Haversine formula to account for
 * global curvature.
 *
 * @param from origin point
 * @param to destination point
 * @return distance between the two points
 * @see <a href="https://en.wikipedia.org/wiki/Haversine_formula">Haversine formula</a>
 */
public fun distance(from: Position, to: Position): Length {
    val dLat = degreesToRadians(to.latitude - from.latitude)
    val dLon = degreesToRadians(to.longitude - from.longitude)
    val lat1 = degreesToRadians(from.latitude)
    val lat2 = degreesToRadians(to.latitude)

    val a = sin(dLat / 2).pow(2) + sin(dLon / 2).pow(2) * cos(lat1) * cos(lat2)
    return 2.0 * atan2(sqrt(a), sqrt(1 - a)).earthRadians
}
