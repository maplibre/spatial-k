@file:JvmName("Measurement")
@file:JvmMultifileClass

package org.maplibre.spatialk.turf.measurement

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.sqrt
import kotlin.math.tan
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.units.Length
import org.maplibre.spatialk.units.LengthUnit.Geodesy.Radians
import org.maplibre.spatialk.units.toLength

/** Calculates the distance along a rhumb line between two points. */
public fun rhumbDistance(origin: Position, destination: Position): Length {
    // compensate the crossing of the 180th meridian
    val destination =
        Position(
            destination.longitude +
                when {
                    destination.longitude - origin.longitude > 180 -> -360
                    origin.longitude - destination.longitude > 180 -> 360
                    else -> 0
                },
            destination.latitude,
        )

    val phi1 = origin.latitude * PI / 180
    val phi2 = destination.latitude * PI / 180
    val deltaPhi = phi2 - phi1
    var deltaLambda = abs(destination.longitude - origin.longitude) * PI / 180

    if (deltaLambda > PI) {
        deltaLambda -= 2 * PI
    }

    val deltaPsi = ln(tan(phi2 / 2 + PI / 4) / tan(phi1 / 2 + PI / 4))
    val q = if (abs(deltaPsi) > 10e-12) deltaPhi / deltaPsi else cos(phi1)

    val delta = sqrt(deltaPhi * deltaPhi + q * q * deltaLambda * deltaLambda)
    val dist = delta.toLength(Radians)

    return dist
}
