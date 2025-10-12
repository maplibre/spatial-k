@file:JvmName("Measurement")
@file:JvmMultifileClass

package org.maplibre.spatialk.turf.measurement

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmSynthetic
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.units.Bearing
import org.maplibre.spatialk.units.Bearing.Companion.North
import org.maplibre.spatialk.units.DMS.Degrees
import org.maplibre.spatialk.units.International.Meters
import org.maplibre.spatialk.units.Length
import org.maplibre.spatialk.units.LengthUnit
import org.maplibre.spatialk.units.RotationUnit
import org.maplibre.spatialk.units.extensions.*

/**
 * Takes a [Position] and calculates the location of a destination position given a distance
 * [Length] and bearing in degrees. This uses the Haversine formula to account for global curvature.
 *
 * @param distance distance from the origin point
 * @param bearing ranging from -180 to 180
 * @return destination position
 * @see <a href="https://en.wikipedia.org/wiki/Haversine_formula">Haversine formula</a>
 */
@JvmSynthetic
public fun Position.offset(distance: Length, bearing: Bearing): Position {
    val longitude1 = this.longitude.degrees
    val latitude1 = this.latitude.degrees
    val bearingFromN = (bearing - North)
    val radians = distance.inEarthRadians.radians

    val latitude2 =
        asin(sin(latitude1) * cos(radians) + cos(latitude1) * sin(radians) * cos(bearingFromN))
    val longitude2 =
        longitude1 +
            atan2(
                sin(bearingFromN) * sin(radians) * cos(latitude1),
                cos(radians) - sin(latitude1) * sin(latitude2),
            )

    return Position(longitude2.inDegrees, latitude2.inDegrees)
}

@PublishedApi
@Suppress("unused")
@JvmOverloads
internal fun offset(
    origin: Position,
    distance: Double,
    distanceUnit: LengthUnit = Meters,
    bearing: Double,
    bearingUnit: RotationUnit = Degrees,
): Position =
    origin.offset(distance.toLength(distanceUnit), North + bearing.toRotation(bearingUnit))
