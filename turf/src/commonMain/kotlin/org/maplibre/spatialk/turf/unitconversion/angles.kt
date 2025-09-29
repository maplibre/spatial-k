package org.maplibre.spatialk.turf.unitconversion

import kotlin.math.PI

/** Converts an angle in radians to degrees */
public fun radiansToDegrees(radians: Double): Double = radians * 180.0 / PI

/** Converts an angle in degrees to radians */
public fun degreesToRadians(degrees: Double): Double = degrees * PI / 180.0
