@file:JvmName("CoordinateMutation")
@file:JvmMultifileClass

package org.maplibre.spatialk.turf.coordinatemutation

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.math.pow

/**
 * Round [number] to [precision] decimal places.
 *
 * @param number number to round
 * @param precision number of decimal places
 * @return rounded number
 */
public fun round(number: Double, precision: Int = 0): Double {
    require(precision >= 0) { "precision must be non-negative" }
    val multiplier = 10.0.pow(precision)
    return kotlin.math.round(number * multiplier) / multiplier
}
