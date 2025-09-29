package org.maplibre.spatialk.turf.unitconversion

import kotlin.math.PI
import org.maplibre.spatialk.turf.ExperimentalTurfApi

/** Converts an angle in radians to degrees */
public fun radiansToDegrees(radians: Double): Double = radians * 180.0 / PI

/** Converts an angle in degrees to radians */
public fun degreesToRadians(degrees: Double): Double = degrees * PI / 180.0

/**
 * Converts any bearing angle from the north line direction (positive clockwise) and returns an
 * angle between 0-360 degrees (positive clockwise), 0 being the north line
 *
 * @param bearing angle, between -180 and +180 degrees
 * @return angle between 0 and 360 degrees
 */
@ExperimentalTurfApi
public fun bearingToAzimuth(bearing: Double): Double {
    var angle = bearing % 360
    if (angle < 0) {
        angle += 360
    }
    return angle
}

/**
 * Converts any azimuth angle from the north line direction (positive clockwise) and returns an
 * angle between -180 and +180 degrees (positive clockwise), 0 being the north line
 *
 * @param azimuth between 0 and 360 degrees
 * @return bearing angle, between -180 and +180 degrees
 */
@ExperimentalTurfApi
public fun azimuthToBearing(azimuth: Double): Double {
    // Ignore full revolutions (multiples of 360)
    val angle = azimuth % 360
    return when {
        angle > 180 -> angle - 360
        angle < -180 -> angle + 360
        else -> angle
    }
}
