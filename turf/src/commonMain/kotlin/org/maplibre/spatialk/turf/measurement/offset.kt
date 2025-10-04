@file:JvmName("Measurement")
@file:JvmMultifileClass

package org.maplibre.spatialk.turf.measurement

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmSynthetic
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.turf.unitconversion.degreesToRadians
import org.maplibre.spatialk.turf.unitconversion.radiansToDegrees
import org.maplibre.spatialk.units.International.Meters
import org.maplibre.spatialk.units.Length
import org.maplibre.spatialk.units.LengthUnit
import org.maplibre.spatialk.units.extensions.inEarthRadians
import org.maplibre.spatialk.units.extensions.toLength

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
public fun Position.offset(distance: Length, bearing: Double): Position {
    val longitude1 = degreesToRadians(longitude)
    val latitude1 = degreesToRadians(latitude)
    val bearingRad = degreesToRadians(bearing)
    val radians = distance.inEarthRadians

    val latitude2 =
        asin(sin(latitude1) * cos(radians) + cos(latitude1) * sin(radians) * cos(bearingRad))
    val longitude2 =
        longitude1 +
            atan2(
                sin(bearingRad) * sin(radians) * cos(latitude1),
                cos(radians) - sin(latitude1) * sin(latitude2),
            )

    return Position(radiansToDegrees(longitude2), radiansToDegrees(latitude2))
}

@PublishedApi
@Suppress("unused")
@JvmOverloads
internal fun offset(
    origin: Position,
    distance: Double,
    unit: LengthUnit = Meters,
    bearing: Int,
): Position = origin.offset(distance.toLength(unit), bearing.toDouble())
