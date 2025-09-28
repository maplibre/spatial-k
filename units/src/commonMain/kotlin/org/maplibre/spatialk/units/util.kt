package org.maplibre.spatialk.units

import org.maplibre.spatialk.units.AreaUnit.Imperial.*
import org.maplibre.spatialk.units.AreaUnit.International.*
import org.maplibre.spatialk.units.LengthUnit.Imperial.*
import org.maplibre.spatialk.units.LengthUnit.International.*

public typealias Length = Measurement<Dimension.Length>

public typealias Area = Measurement<Dimension.Area>

public operator fun <D : Dimension> Number.times(other: Measurement<D>): Measurement<D> =
    other * this

public operator fun Length.times(other: Length): Area =
    (this.inMeters * other.inMeters).squareMeters

public operator fun Area.div(other: Length): Length = (this.inSquareMeters / other.inMeters).meters

public fun Number.toLength(unit: LengthUnit): Length = Length(this.toDouble() * unit.magnitude)

public fun Number.toArea(unit: AreaUnit): Area = Area(this.toDouble() * unit.magnitude)

public inline val Number.meters: Length
    get() = toLength(Meters)

public inline val Number.kilometers: Length
    get() = toLength(Kilometers)

public inline val Number.feet: Length
    get() = toLength(Feet)

public inline val Number.miles: Length
    get() = toLength(Miles)

public inline val Number.squareMeters: Area
    get() = toArea(SquareMeters)

public inline val Number.squareKilometers: Area
    get() = toArea(SquareKilometers)

public inline val Number.squareFeet: Area
    get() = toArea(SquareFeet)

public inline val Number.squareMiles: Area
    get() = toArea(SquareMiles)

public inline val Number.acres: Area
    get() = toArea(Acres)

public inline val Length.inMeters: Double
    get() = toDouble(Meters)

public inline val Length.inKilometers: Double
    get() = toDouble(Kilometers)

public inline val Length.inFeet: Double
    get() = toDouble(Feet)

public inline val Length.inMiles: Double
    get() = toDouble(Miles)

public inline val Area.inSquareMeters: Double
    get() = toDouble(SquareMeters)

public inline val Area.inSquareKilometers: Double
    get() = toDouble(SquareKilometers)

public inline val Area.inSquareFeet: Double
    get() = toDouble(SquareFeet)

public inline val Area.inSquareMiles: Double
    get() = toDouble(SquareMiles)

public inline val Area.inAcres: Double
    get() = toDouble(Acres)

internal fun Double.toRoundedString(decimalPlaces: Int): String {
    val str = toString().split('.', limit = 1)
    val integerPart = str[0]
    val decimalPart = str.getOrElse(1) { "0" }.take(decimalPlaces)
    return integerPart + decimalPart
}
