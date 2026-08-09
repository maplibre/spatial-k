@file:JvmName("Measurement")
@file:JvmMultifileClass

package org.maplibre.spatialk.turf.measurement

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmSynthetic
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.sin
import kotlin.math.tan
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.units.Bearing
import org.maplibre.spatialk.units.Bearing.Companion.North
import org.maplibre.spatialk.units.DMS.Degrees
import org.maplibre.spatialk.units.International.Meters
import org.maplibre.spatialk.units.Length
import org.maplibre.spatialk.units.LengthUnit
import org.maplibre.spatialk.units.RotationUnit
import org.maplibre.spatialk.units.extensions.degrees
import org.maplibre.spatialk.units.extensions.inEarthRadians
import org.maplibre.spatialk.units.extensions.inRadians
import org.maplibre.spatialk.units.extensions.toLength
import org.maplibre.spatialk.units.extensions.toRotation

/**
 * Returns the destination [Position] having travelled the given [distance] along a Rhumb line from
 * this position with the given [bearing].
 *
 * @param distance distance from the starting point
 * @param bearing variant bearing angle ranging from -180 to 180 degrees from north
 * @return the destination position
 */
@JvmSynthetic
public fun Position.rhumbOffset(distance: Length, bearing: Bearing): Position {
    val destination = calculateRhumbDestination(this, distance, bearing)
    return Position(compensateAntiMeridianLongitude(this, destination), destination.latitude)
}

@PublishedApi
@Suppress("unused")
@JvmOverloads
internal fun rhumbOffset(
    origin: Position,
    distance: Double,
    distanceUnit: LengthUnit = Meters,
    bearing: Double,
    bearingUnit: RotationUnit = Degrees,
): Position =
    origin.rhumbOffset(distance.toLength(distanceUnit), North + bearing.toRotation(bearingUnit))

@Suppress("MagicNumber")
private fun calculateRhumbDestination(
    origin: Position,
    distance: Length,
    bearing: Bearing,
): Position {
    val delta = distance.inEarthRadians // angular distance in radians
    val lambda1 = origin.longitude.degrees.inRadians // to radians, but without normalize to pi
    val phi1 = origin.latitude.degrees.inRadians
    val theta = (bearing - North).inRadians

    val deltaPhi = delta * cos(theta)

    // check for some daft bugger going past the pole, normalise latitude if so
    val phi2 =
        (phi1 + deltaPhi).let {
            if (abs(it) > PI / 2) {
                if (it > 0) PI - it else -PI - it
            } else {
                it
            }
        }

    val deltaPsi = ln(tan(phi2 / 2 + PI / 4) / tan(phi1 / 2 + PI / 4))

    // E-W course becomes ill-conditioned with 0/0
    val q = if (abs(deltaPsi) > 10e-12) deltaPhi / deltaPsi else cos(phi1)

    val deltaLambda = (delta * sin(theta)) / q

    val lambda2 = lambda1 + deltaLambda

    return Position(
        longitude = (((lambda2 * 180 / PI) + 540) % 360) - 180,
        latitude = phi2 * 180 / PI,
    )
}

private fun compensateAntiMeridianLongitude(from: Position, to: Position): Double {
    val longitude =
        when {
            to.longitude - from.longitude > 180 -> to.longitude - 360
            from.longitude - to.longitude > 180 -> to.longitude + 360
            else -> to.longitude
        }
    return longitude
}
