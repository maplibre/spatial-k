@file:JvmSynthetic

package org.maplibre.spatialk.units.extensions

import kotlin.jvm.JvmSynthetic
import org.maplibre.spatialk.units.Area
import org.maplibre.spatialk.units.AreaUnit
import org.maplibre.spatialk.units.Geodesy.Degrees
import org.maplibre.spatialk.units.Geodesy.Minutes
import org.maplibre.spatialk.units.Geodesy.Radians
import org.maplibre.spatialk.units.Geodesy.Seconds
import org.maplibre.spatialk.units.Imperial.Acres
import org.maplibre.spatialk.units.Imperial.Cables
import org.maplibre.spatialk.units.Imperial.Chains
import org.maplibre.spatialk.units.Imperial.Fathoms
import org.maplibre.spatialk.units.Imperial.Feet
import org.maplibre.spatialk.units.Imperial.Furlongs
import org.maplibre.spatialk.units.Imperial.Inches
import org.maplibre.spatialk.units.Imperial.Leagues
import org.maplibre.spatialk.units.Imperial.Links
import org.maplibre.spatialk.units.Imperial.Miles
import org.maplibre.spatialk.units.Imperial.NauticalMiles
import org.maplibre.spatialk.units.Imperial.Rods
import org.maplibre.spatialk.units.Imperial.SquareFeet
import org.maplibre.spatialk.units.Imperial.SquareInches
import org.maplibre.spatialk.units.Imperial.SquareMiles
import org.maplibre.spatialk.units.Imperial.SquareRods
import org.maplibre.spatialk.units.Imperial.SquareYards
import org.maplibre.spatialk.units.Imperial.Yards
import org.maplibre.spatialk.units.International.Centimeters
import org.maplibre.spatialk.units.International.Kilometers
import org.maplibre.spatialk.units.International.Meters
import org.maplibre.spatialk.units.International.Millimeters
import org.maplibre.spatialk.units.International.SquareCentimeters
import org.maplibre.spatialk.units.International.SquareKilometers
import org.maplibre.spatialk.units.International.SquareMeters
import org.maplibre.spatialk.units.International.SquareMillimeters
import org.maplibre.spatialk.units.Length
import org.maplibre.spatialk.units.LengthUnit
import org.maplibre.spatialk.units.Metric.Ares
import org.maplibre.spatialk.units.Metric.Centiares
import org.maplibre.spatialk.units.Metric.Decares
import org.maplibre.spatialk.units.Metric.Deciares
import org.maplibre.spatialk.units.Metric.Hectares

internal fun Double.toRoundedString(decimalPlaces: Int): String {
    val str = toString().split('.', limit = 1)
    val integerPart = str[0]
    val decimalPart = str.getOrElse(1) { "0" }.take(decimalPlaces)
    return integerPart + decimalPart
}

public operator fun Double.times(other: Length): Length = other * this

public operator fun Double.times(other: Area): Area = other * this

public fun Double.toLength(unit: LengthUnit): Length = Length.of(this, unit)

public fun Double.toArea(unit: AreaUnit): Area = Area.of(this, unit)

// SI units - Length

public inline val Double.millimeters: Length
    get() = toLength(Millimeters)

public inline val Length.inMillimeters: Double
    get() = toDouble(Millimeters)

public inline val Double.centimeters: Length
    get() = toLength(Centimeters)

public inline val Length.inCentimeters: Double
    get() = toDouble(Centimeters)

public inline val Double.meters: Length
    get() = toLength(Meters)

public inline val Length.inMeters: Double
    get() = toDouble(Meters)

public inline val Double.kilometers: Length
    get() = toLength(Kilometers)

public inline val Length.inKilometers: Double
    get() = toDouble(Kilometers)

// SI units - Area

public inline val Double.squareMillimeters: Area
    get() = toArea(SquareMillimeters)

public inline val Area.inSquareMillimeters: Double
    get() = toDouble(SquareMillimeters)

public inline val Double.squareCentimeters: Area
    get() = toArea(SquareCentimeters)

public inline val Area.inSquareCentimeters: Double
    get() = toDouble(SquareCentimeters)

public inline val Double.squareMeters: Area
    get() = toArea(SquareMeters)

public inline val Area.inSquareMeters: Double
    get() = toDouble(SquareMeters)

public inline val Double.squareKilometers: Area
    get() = toArea(SquareKilometers)

public inline val Area.inSquareKilometers: Double
    get() = toDouble(SquareKilometers)

// Geodesy units - Length

public inline val Double.earthRadians: Length
    get() = toLength(Radians)

public inline val Length.inEarthRadians: Double
    get() = toDouble(Radians)

public inline val Double.earthDegrees: Length
    get() = toLength(Degrees)

public inline val Length.inEarthDegrees: Double
    get() = toDouble(Degrees)

public inline val Double.earthMinutes: Length
    get() = toLength(Minutes)

public inline val Length.inEarthMinutes: Double
    get() = toDouble(Minutes)

public inline val Double.earthSeconds: Length
    get() = toLength(Seconds)

public inline val Length.inEarthSeconds: Double
    get() = toDouble(Seconds)

// Metric units - Area

public inline val Double.centiares: Area
    get() = toArea(Centiares)

public inline val Area.inCentiares: Double
    get() = toDouble(Centiares)

public inline val Double.deciares: Area
    get() = toArea(Deciares)

public inline val Area.inDeciares: Double
    get() = toDouble(Deciares)

public inline val Double.ares: Area
    get() = toArea(Ares)

public inline val Area.inAres: Double
    get() = toDouble(Ares)

public inline val Double.decares: Area
    get() = toArea(Decares)

public inline val Area.inDecares: Double
    get() = toDouble(Decares)

public inline val Double.hectares: Area
    get() = toArea(Hectares)

public inline val Area.inHectares: Double
    get() = toDouble(Hectares)

// Imperial units - Length

public inline val Double.inches: Length
    get() = toLength(Inches)

public inline val Length.inInches: Double
    get() = toDouble(Inches)

public inline val Double.feet: Length
    get() = toLength(Feet)

public inline val Length.inFeet: Double
    get() = toDouble(Feet)

public inline val Double.yards: Length
    get() = toLength(Yards)

public inline val Length.inYards: Double
    get() = toDouble(Yards)

public inline val Double.miles: Length
    get() = toLength(Miles)

public inline val Length.inMiles: Double
    get() = toDouble(Miles)

public inline val Double.links: Length
    get() = toLength(Links)

public inline val Length.inLinks: Double
    get() = toDouble(Links)

public inline val Double.rods: Length
    get() = toLength(Rods)

public inline val Length.inRods: Double
    get() = toDouble(Rods)

public inline val Double.chains: Length
    get() = toLength(Chains)

public inline val Length.inChains: Double
    get() = toDouble(Chains)

public inline val Double.furlongs: Length
    get() = toLength(Furlongs)

public inline val Length.inFurlongs: Double
    get() = toDouble(Furlongs)

public inline val Double.leagues: Length
    get() = toLength(Leagues)

public inline val Length.inLeagues: Double
    get() = toDouble(Leagues)

public inline val Double.fathoms: Length
    get() = toLength(Fathoms)

public inline val Length.inFathoms: Double
    get() = toDouble(Fathoms)

public inline val Double.cables: Length
    get() = toLength(Cables)

public inline val Length.inCables: Double
    get() = toDouble(Cables)

public inline val Double.nauticalMiles: Length
    get() = toLength(NauticalMiles)

public inline val Length.inNauticalMiles: Double
    get() = toDouble(NauticalMiles)

// Imperial units - Area

public inline val Double.squareInches: Area
    get() = toArea(SquareInches)

public inline val Area.inSquareInches: Double
    get() = toDouble(SquareInches)

public inline val Double.squareFeet: Area
    get() = toArea(SquareFeet)

public inline val Area.inSquareFeet: Double
    get() = toDouble(SquareFeet)

public inline val Double.squareYards: Area
    get() = toArea(SquareYards)

public inline val Area.inSquareYards: Double
    get() = toDouble(SquareYards)

public inline val Double.squareMiles: Area
    get() = toArea(SquareMiles)

public inline val Area.inSquareMiles: Double
    get() = toDouble(SquareMiles)

public inline val Double.squareRods: Area
    get() = toArea(SquareRods)

public inline val Area.inSquareRods: Double
    get() = toDouble(SquareRods)

public inline val Double.acres: Area
    get() = toArea(Acres)

public inline val Area.inAcres: Double
    get() = toDouble(Acres)
