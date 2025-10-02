@file:JvmSynthetic

package org.maplibre.spatialk.units

import kotlin.jvm.JvmSynthetic

public operator fun Number.times(other: Length): Length = other * this

public operator fun Number.times(other: Area): Area = other * this

public fun Number.toLength(unit: LengthUnit): Length = Length.of(this, unit)

public fun Number.toArea(unit: AreaUnit): Area = Area.of(this, unit)

internal fun Double.toRoundedString(decimalPlaces: Int): String {
    val str = toString().split('.', limit = 1)
    val integerPart = str[0]
    val decimalPart = str.getOrElse(1) { "0" }.take(decimalPlaces)
    return integerPart + decimalPart
}
