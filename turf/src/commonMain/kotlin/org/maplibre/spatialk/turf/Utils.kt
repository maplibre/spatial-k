@file:JvmName("TurfUtils")

package org.maplibre.spatialk.turf

import kotlin.jvm.JvmName

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
