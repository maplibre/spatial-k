@file:JvmSynthetic

package org.maplibre.spatialk.units.extensions

import kotlin.experimental.ExperimentalTypeInference
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic
import kotlin.math.pow
import kotlin.math.roundToLong
import org.maplibre.spatialk.units.*
import org.maplibre.spatialk.units.DMS.ArcMinutes
import org.maplibre.spatialk.units.DMS.ArcSeconds
import org.maplibre.spatialk.units.DMS.Degrees
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
import org.maplibre.spatialk.units.International.Radians
import org.maplibre.spatialk.units.International.SquareCentimeters
import org.maplibre.spatialk.units.International.SquareKilometers
import org.maplibre.spatialk.units.International.SquareMeters
import org.maplibre.spatialk.units.International.SquareMillimeters
import org.maplibre.spatialk.units.Metric.Ares
import org.maplibre.spatialk.units.Metric.Centiares
import org.maplibre.spatialk.units.Metric.Decares
import org.maplibre.spatialk.units.Metric.Deciares
import org.maplibre.spatialk.units.Metric.Gradians
import org.maplibre.spatialk.units.Metric.Hectares

internal fun Double.toRoundedString(decimalPlaces: Int): String {
    val mult = 10.0.pow(decimalPlaces)
    val rounded = ((this * mult).roundToLong() / mult).toString()
    val intPart = rounded.substringBefore('.')
    if (decimalPlaces == 0) return intPart
    val decimalPart = rounded.substringAfter('.', missingDelimiterValue = "").take(decimalPlaces)
    return "$intPart.${decimalPart.padEnd(decimalPlaces, '0')}"
}

/** Multiplies a scalar by a [Length]. */
public operator fun Double.times(other: Length): Length = other * this

/** Multiplies a scalar by an [Area]. */
public operator fun Double.times(other: Area): Area = other * this

/** Multiplies a scalar by a [Rotation]. */
public operator fun Double.times(other: Rotation): Rotation = other * this

/** Converts this scalar to a [Length] in the specified [unit]. */
public fun Double.toLength(unit: LengthUnit): Length = Length.of(this, unit)

/** Converts this scalar to an [Area] in the specified [unit]. */
public fun Double.toArea(unit: AreaUnit): Area = Area.of(this, unit)

/** Converts this scalar to a [Rotation] in the specified [unit]. */
public fun Double.toRotation(unit: RotationUnit): Rotation = Rotation.of(this, unit)

/** Calculates the sum of [Length] values produced by [selector] for each element. */
@OptIn(ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
@JvmName("sumOfLength")
public inline fun <T> Iterable<T>.sumOf(selector: (T) -> Length): Length =
    fold(Length.Zero) { acc, t -> acc + selector(t) }

/** Calculates the sum of all [Length] values in this collection. */
@JvmName("sumLength")
public fun Iterable<Length>.sum(): Length = fold(Length.Zero) { acc, t -> acc + t }

/** Calculates the sum of [Area] values produced by [selector] for each element. */
@OptIn(ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
@JvmName("sumOfArea")
public inline fun <T> Iterable<T>.sumOf(selector: (T) -> Area): Area =
    fold(Area.Zero) { acc, t -> acc + selector(t) }

/** Calculates the sum of all [Area] values in this collection. */
@JvmName("sumArea") public fun Iterable<Area>.sum(): Area = fold(Area.Zero) { acc, t -> acc + t }

/** Calculates the sum of [Rotation] values produced by [selector] for each element. */
@OptIn(ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
@JvmName("sumOfRotation")
public inline fun <T> Iterable<T>.sumOf(selector: (T) -> Rotation): Rotation =
    fold(Rotation.Zero) { acc, t -> acc + selector(t) }

/** Calculates the sum of all [Rotation] values in this collection. */
@JvmName("sumRotation")
public fun Iterable<Rotation>.sum(): Rotation = fold(Rotation.Zero) { acc, t -> acc + t }

// SI units - Length

/** Creates a [Length] from a scalar value in millimeters. */
public inline val Double.millimeters: Length
    get() = toLength(Millimeters)

/** Creates a [Length] from a scalar value in millimeters. */
public inline val Int.millimeters: Length
    get() = toDouble().toLength(Millimeters)

/** Returns this [Length] as a scalar value in millimeters. */
public inline val Length.inMillimeters: Double
    get() = toDouble(Millimeters)

/** Creates a [Length] from a scalar value in centimeters. */
public inline val Double.centimeters: Length
    get() = toLength(Centimeters)

/** Creates a [Length] from a scalar value in centimeters. */
public inline val Int.centimeters: Length
    get() = toDouble().toLength(Centimeters)

/** Returns this [Length] as a scalar value in centimeters. */
public inline val Length.inCentimeters: Double
    get() = toDouble(Centimeters)

/** Creates a [Length] from a scalar value in meters. */
public inline val Double.meters: Length
    get() = toLength(Meters)

/** Creates a [Length] from a scalar value in meters. */
public inline val Int.meters: Length
    get() = toDouble().toLength(Meters)

/** Returns this [Length] as a scalar value in meters. */
public inline val Length.inMeters: Double
    get() = toDouble(Meters)

/** Creates a [Length] from a scalar value in kilometers. */
public inline val Double.kilometers: Length
    get() = toLength(Kilometers)

/** Creates a [Length] from a scalar value in kilometers. */
public inline val Int.kilometers: Length
    get() = toDouble().toLength(Kilometers)

/** Returns this [Length] as a scalar value in kilometers. */
public inline val Length.inKilometers: Double
    get() = toDouble(Kilometers)

// SI units - Area

/** Creates an [Area] from a scalar value in square millimeters. */
public inline val Double.squareMillimeters: Area
    get() = toArea(SquareMillimeters)

/** Creates an [Area] from a scalar value in square millimeters. */
public inline val Int.squareMillimeters: Area
    get() = toDouble().toArea(SquareMillimeters)

/** Returns this [Area] as a scalar value in square millimeters. */
public inline val Area.inSquareMillimeters: Double
    get() = toDouble(SquareMillimeters)

/** Creates an [Area] from a scalar value in square centimeters. */
public inline val Double.squareCentimeters: Area
    get() = toArea(SquareCentimeters)

/** Creates an [Area] from a scalar value in square centimeters. */
public inline val Int.squareCentimeters: Area
    get() = toDouble().toArea(SquareCentimeters)

/** Returns this [Area] as a scalar value in square centimeters. */
public inline val Area.inSquareCentimeters: Double
    get() = toDouble(SquareCentimeters)

/** Creates an [Area] from a scalar value in square meters. */
public inline val Double.squareMeters: Area
    get() = toArea(SquareMeters)

/** Creates an [Area] from a scalar value in square meters. */
public inline val Int.squareMeters: Area
    get() = toDouble().toArea(SquareMeters)

/** Returns this [Area] as a scalar value in square meters. */
public inline val Area.inSquareMeters: Double
    get() = toDouble(SquareMeters)

/** Creates an [Area] from a scalar value in square kilometers. */
public inline val Double.squareKilometers: Area
    get() = toArea(SquareKilometers)

/** Creates an [Area] from a scalar value in square kilometers. */
public inline val Int.squareKilometers: Area
    get() = toDouble().toArea(SquareKilometers)

/** Returns this [Area] as a scalar value in square kilometers. */
public inline val Area.inSquareKilometers: Double
    get() = toDouble(SquareKilometers)

// Rotation units

/** Creates a [Rotation] from a scalar value in radians. */
public inline val Double.radians: Rotation
    get() = toRotation(Radians)

/** Creates a [Rotation] from a scalar value in radians. */
public inline val Int.radians: Rotation
    get() = toDouble().toRotation(Radians)

/** Returns this [Rotation] as a scalar value in radians. */
public inline val Rotation.inRadians: Double
    get() = toDouble(Radians)

/** Creates a [Rotation] from a scalar value in gradians. */
public inline val Double.gradians: Rotation
    get() = toRotation(Gradians)

/** Creates a [Rotation] from a scalar value in gradians. */
public inline val Int.gradians: Rotation
    get() = toDouble().toRotation(Gradians)

/** Returns this [Rotation] as a scalar value in gradians. */
public inline val Rotation.inGradians: Double
    get() = toDouble(Gradians)

/** Creates a [Rotation] from a scalar value in degrees. */
public inline val Double.degrees: Rotation
    get() = toRotation(Degrees)

/** Creates a [Rotation] from a scalar value in degrees. */
public inline val Int.degrees: Rotation
    get() = toDouble().toRotation(Degrees)

/** Returns this [Rotation] as a scalar value in degrees. */
public inline val Rotation.inDegrees: Double
    get() = toDouble(Degrees)

/** Creates a [Rotation] from a scalar value in arc minutes. */
public inline val Double.arcMinutes: Rotation
    get() = toRotation(ArcMinutes)

/** Creates a [Rotation] from a scalar value in arc minutes. */
public inline val Int.arcMinutes: Rotation
    get() = toDouble().toRotation(ArcMinutes)

/** Returns this [Rotation] as a scalar value in arc minutes. */
public inline val Rotation.inArcMinutes: Double
    get() = toDouble(ArcMinutes)

/** Creates a [Rotation] from a scalar value in arc seconds. */
public inline val Double.arcSeconds: Rotation
    get() = toRotation(ArcSeconds)

/** Creates a [Rotation] from a scalar value in arc seconds. */
public inline val Int.arcSeconds: Rotation
    get() = toDouble().toRotation(ArcSeconds)

/** Returns this [Rotation] as a scalar value in arc seconds. */
public inline val Rotation.inArcSeconds: Double
    get() = toDouble(ArcSeconds)

// Geodesy units - Length

/** Creates a [Length] from a scalar value in earth radians. */
public inline val Double.earthRadians: Length
    get() = toLength(Earth.Radians)

/** Creates a [Length] from a scalar value in earth radians. */
public inline val Int.earthRadians: Length
    get() = toDouble().toLength(Earth.Radians)

/** Returns this [Length] as a scalar value in earth radians. */
public inline val Length.inEarthRadians: Double
    get() = toDouble(Earth.Radians)

/** Creates a [Length] from a scalar value in earth degrees. */
public inline val Double.earthDegrees: Length
    get() = toLength(Earth.Degrees)

/** Creates a [Length] from a scalar value in earth degrees. */
public inline val Int.earthDegrees: Length
    get() = toDouble().toLength(Earth.Degrees)

/** Returns this [Length] as a scalar value in earth degrees. */
public inline val Length.inEarthDegrees: Double
    get() = toDouble(Earth.Degrees)

/** Creates a [Length] from a scalar value in earth minutes. */
public inline val Double.earthMinutes: Length
    get() = toLength(Earth.ArcMinutes)

/** Creates a [Length] from a scalar value in earth minutes. */
public inline val Int.earthMinutes: Length
    get() = toDouble().toLength(Earth.ArcMinutes)

/** Returns this [Length] as a scalar value in earth minutes. */
public inline val Length.inEarthMinutes: Double
    get() = toDouble(Earth.ArcMinutes)

/** Creates a [Length] from a scalar value in earth seconds. */
public inline val Double.earthSeconds: Length
    get() = toLength(Earth.ArcSeconds)

/** Creates a [Length] from a scalar value in earth seconds. */
public inline val Int.earthSeconds: Length
    get() = toDouble().toLength(Earth.ArcSeconds)

/** Returns this [Length] as a scalar value in earth seconds. */
public inline val Length.inEarthSeconds: Double
    get() = toDouble(Earth.ArcSeconds)

// Metric units - Area

/** Creates an [Area] from a scalar value in centiares. */
public inline val Double.centiares: Area
    get() = toArea(Centiares)

/** Creates an [Area] from a scalar value in centiares. */
public inline val Int.centiares: Area
    get() = toDouble().toArea(Centiares)

/** Returns this [Area] as a scalar value in centiares. */
public inline val Area.inCentiares: Double
    get() = toDouble(Centiares)

/** Creates an [Area] from a scalar value in deciares. */
public inline val Double.deciares: Area
    get() = toArea(Deciares)

/** Creates an [Area] from a scalar value in deciares. */
public inline val Int.deciares: Area
    get() = toDouble().toArea(Deciares)

/** Returns this [Area] as a scalar value in deciares. */
public inline val Area.inDeciares: Double
    get() = toDouble(Deciares)

/** Creates an [Area] from a scalar value in ares. */
public inline val Double.ares: Area
    get() = toArea(Ares)

/** Creates an [Area] from a scalar value in ares. */
public inline val Int.ares: Area
    get() = toDouble().toArea(Ares)

/** Returns this [Area] as a scalar value in ares. */
public inline val Area.inAres: Double
    get() = toDouble(Ares)

/** Creates an [Area] from a scalar value in decares. */
public inline val Double.decares: Area
    get() = toArea(Decares)

/** Creates an [Area] from a scalar value in decares. */
public inline val Int.decares: Area
    get() = toDouble().toArea(Decares)

/** Returns this [Area] as a scalar value in decares. */
public inline val Area.inDecares: Double
    get() = toDouble(Decares)

/** Creates an [Area] from a scalar value in hectares. */
public inline val Double.hectares: Area
    get() = toArea(Hectares)

/** Creates an [Area] from a scalar value in hectares. */
public inline val Int.hectares: Area
    get() = toDouble().toArea(Hectares)

/** Returns this [Area] as a scalar value in hectares. */
public inline val Area.inHectares: Double
    get() = toDouble(Hectares)

// Imperial units - Length

/** Creates a [Length] from a scalar value in inches. */
public inline val Double.inches: Length
    get() = toLength(Inches)

/** Creates a [Length] from a scalar value in inches. */
public inline val Int.inches: Length
    get() = toDouble().toLength(Inches)

/** Returns this [Length] as a scalar value in inches. */
public inline val Length.inInches: Double
    get() = toDouble(Inches)

/** Creates a [Length] from a scalar value in feet. */
public inline val Double.feet: Length
    get() = toLength(Feet)

/** Creates a [Length] from a scalar value in feet. */
public inline val Int.feet: Length
    get() = toDouble().toLength(Feet)

/** Returns this [Length] as a scalar value in feet. */
public inline val Length.inFeet: Double
    get() = toDouble(Feet)

/** Creates a [Length] from a scalar value in yards. */
public inline val Double.yards: Length
    get() = toLength(Yards)

/** Creates a [Length] from a scalar value in yards. */
public inline val Int.yards: Length
    get() = toDouble().toLength(Yards)

/** Returns this [Length] as a scalar value in yards. */
public inline val Length.inYards: Double
    get() = toDouble(Yards)

/** Creates a [Length] from a scalar value in miles. */
public inline val Double.miles: Length
    get() = toLength(Miles)

/** Creates a [Length] from a scalar value in miles. */
public inline val Int.miles: Length
    get() = toDouble().toLength(Miles)

/** Returns this [Length] as a scalar value in miles. */
public inline val Length.inMiles: Double
    get() = toDouble(Miles)

/** Creates a [Length] from a scalar value in links. */
public inline val Double.links: Length
    get() = toLength(Links)

/** Creates a [Length] from a scalar value in links. */
public inline val Int.links: Length
    get() = toDouble().toLength(Links)

/** Returns this [Length] as a scalar value in links. */
public inline val Length.inLinks: Double
    get() = toDouble(Links)

/** Creates a [Length] from a scalar value in rods. */
public inline val Double.rods: Length
    get() = toLength(Rods)

/** Creates a [Length] from a scalar value in rods. */
public inline val Int.rods: Length
    get() = toDouble().toLength(Rods)

/** Returns this [Length] as a scalar value in rods. */
public inline val Length.inRods: Double
    get() = toDouble(Rods)

/** Creates a [Length] from a scalar value in chains. */
public inline val Double.chains: Length
    get() = toLength(Chains)

/** Creates a [Length] from a scalar value in chains. */
public inline val Int.chains: Length
    get() = toDouble().toLength(Chains)

/** Returns this [Length] as a scalar value in chains. */
public inline val Length.inChains: Double
    get() = toDouble(Chains)

/** Creates a [Length] from a scalar value in furlongs. */
public inline val Double.furlongs: Length
    get() = toLength(Furlongs)

/** Creates a [Length] from a scalar value in furlongs. */
public inline val Int.furlongs: Length
    get() = toDouble().toLength(Furlongs)

/** Returns this [Length] as a scalar value in furlongs. */
public inline val Length.inFurlongs: Double
    get() = toDouble(Furlongs)

/** Creates a [Length] from a scalar value in leagues. */
public inline val Double.leagues: Length
    get() = toLength(Leagues)

/** Creates a [Length] from a scalar value in leagues. */
public inline val Int.leagues: Length
    get() = toDouble().toLength(Leagues)

/** Returns this [Length] as a scalar value in leagues. */
public inline val Length.inLeagues: Double
    get() = toDouble(Leagues)

/** Creates a [Length] from a scalar value in fathoms. */
public inline val Double.fathoms: Length
    get() = toLength(Fathoms)

/** Creates a [Length] from a scalar value in fathoms. */
public inline val Int.fathoms: Length
    get() = toDouble().toLength(Fathoms)

/** Returns this [Length] as a scalar value in fathoms. */
public inline val Length.inFathoms: Double
    get() = toDouble(Fathoms)

/** Creates a [Length] from a scalar value in cables. */
public inline val Double.cables: Length
    get() = toLength(Cables)

/** Creates a [Length] from a scalar value in cables. */
public inline val Int.cables: Length
    get() = toDouble().toLength(Cables)

/** Returns this [Length] as a scalar value in cables. */
public inline val Length.inCables: Double
    get() = toDouble(Cables)

/** Creates a [Length] from a scalar value in nautical miles. */
public inline val Double.nauticalMiles: Length
    get() = toLength(NauticalMiles)

/** Creates a [Length] from a scalar value in nautical miles. */
public inline val Int.nauticalMiles: Length
    get() = toDouble().toLength(NauticalMiles)

/** Returns this [Length] as a scalar value in nautical miles. */
public inline val Length.inNauticalMiles: Double
    get() = toDouble(NauticalMiles)

// Imperial units - Area

/** Creates an [Area] from a scalar value in square inches. */
public inline val Double.squareInches: Area
    get() = toArea(SquareInches)

/** Creates an [Area] from a scalar value in square inches. */
public inline val Int.squareInches: Area
    get() = toDouble().toArea(SquareInches)

/** Returns this [Area] as a scalar value in square inches. */
public inline val Area.inSquareInches: Double
    get() = toDouble(SquareInches)

/** Creates an [Area] from a scalar value in square feet. */
public inline val Double.squareFeet: Area
    get() = toArea(SquareFeet)

/** Creates an [Area] from a scalar value in square feet. */
public inline val Int.squareFeet: Area
    get() = toDouble().toArea(SquareFeet)

/** Returns this [Area] as a scalar value in square feet. */
public inline val Area.inSquareFeet: Double
    get() = toDouble(SquareFeet)

/** Creates an [Area] from a scalar value in square yards. */
public inline val Double.squareYards: Area
    get() = toArea(SquareYards)

/** Creates an [Area] from a scalar value in square yards. */
public inline val Int.squareYards: Area
    get() = toDouble().toArea(SquareYards)

/** Returns this [Area] as a scalar value in square yards. */
public inline val Area.inSquareYards: Double
    get() = toDouble(SquareYards)

/** Creates an [Area] from a scalar value in square miles. */
public inline val Double.squareMiles: Area
    get() = toArea(SquareMiles)

/** Creates an [Area] from a scalar value in square miles. */
public inline val Int.squareMiles: Area
    get() = toDouble().toArea(SquareMiles)

/** Returns this [Area] as a scalar value in square miles. */
public inline val Area.inSquareMiles: Double
    get() = toDouble(SquareMiles)

/** Creates an [Area] from a scalar value in square rods. */
public inline val Double.squareRods: Area
    get() = toArea(SquareRods)

/** Creates an [Area] from a scalar value in square rods. */
public inline val Int.squareRods: Area
    get() = toDouble().toArea(SquareRods)

/** Returns this [Area] as a scalar value in square rods. */
public inline val Area.inSquareRods: Double
    get() = toDouble(SquareRods)

/** Creates an [Area] from a scalar value in acres. */
public inline val Double.acres: Area
    get() = toArea(Acres)

/** Creates an [Area] from a scalar value in acres. */
public inline val Int.acres: Area
    get() = toDouble().toArea(Acres)

/** Returns this [Area] as a scalar value in acres. */
public inline val Area.inAcres: Double
    get() = toDouble(Acres)

// Angle math

/** Returns the sine of this [Rotation]. */
public fun sin(x: Rotation): Double = kotlin.math.sin(x.inRadians)

/** Returns the cosine of this [Rotation]. */
public fun cos(x: Rotation): Double = kotlin.math.cos(x.inRadians)

/** Returns the tangent of this [Rotation]. */
public fun tan(x: Rotation): Double = kotlin.math.tan(x.inRadians)

/** Returns the arcsine as a [Rotation]. */
public fun asin(x: Double): Rotation = kotlin.math.asin(x).radians

/** Returns the arccosine as a [Rotation]. */
public fun acos(x: Double): Rotation = kotlin.math.acos(x).radians

/** Returns the arctangent as a [Rotation]. */
public fun atan(x: Double): Rotation = kotlin.math.atan(x).radians

/** Returns the two-argument arctangent as a [Rotation]. */
public fun atan2(y: Double, x: Double): Rotation = kotlin.math.atan2(y, x).radians
