@file:JvmSynthetic

package org.maplibre.spatialk.units.extensions

import kotlin.experimental.ExperimentalTypeInference
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic
import kotlin.math.pow
import kotlin.math.roundToLong
import org.maplibre.spatialk.units.*

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
