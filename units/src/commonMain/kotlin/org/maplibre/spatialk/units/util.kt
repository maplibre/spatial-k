package org.maplibre.spatialk.units

import org.maplibre.spatialk.units.AreaUnit.Imperial.*
import org.maplibre.spatialk.units.AreaUnit.International.*
import org.maplibre.spatialk.units.AreaUnit.Metric.*
import org.maplibre.spatialk.units.LengthUnit.Imperial.*
import org.maplibre.spatialk.units.LengthUnit.International.*

public operator fun Number.times(other: Length): Length = other * this

public operator fun Number.times(other: Area): Area = other * this

public fun Number.toLength(unit: LengthUnit): Length = Length.of(this, unit)

public fun Number.toArea(unit: AreaUnit): Area = Area.of(this, unit)

public inline val Number.meters: Length
    get() = toLength(Meters)

public inline val Number.kilometers: Length
    get() = toLength(Kilometers)

public inline val Number.feet: Length
    get() = toLength(Feet)

public inline val Number.yards: Length
    get() = toLength(Yards)

public inline val Number.miles: Length
    get() = toLength(Miles)

public inline val Number.squareMeters: Area
    get() = toArea(SquareMeters)

public inline val Number.squareKilometers: Area
    get() = toArea(SquareKilometers)

public inline val Number.hectares: Area
    get() = toArea(Hectares)

public inline val Number.squareFeet: Area
    get() = toArea(SquareFeet)

public inline val Number.squareMiles: Area
    get() = toArea(SquareMiles)

public inline val Number.acres: Area
    get() = toArea(Acres)

internal fun Double.toRoundedString(decimalPlaces: Int): String {
    val str = toString().split('.', limit = 1)
    val integerPart = str[0]
    val decimalPart = str.getOrElse(1) { "0" }.take(decimalPlaces)
    return integerPart + decimalPart
}
