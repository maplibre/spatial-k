@file:JvmName("Utils")
@file:JvmMultifileClass

package org.maplibre.spatialk.units.extensions

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import org.maplibre.spatialk.units.AreaUnit
import org.maplibre.spatialk.units.LengthUnit
import org.maplibre.spatialk.units.RotationUnit

/**
 * Convert a value from one [LengthUnit] to another.
 *
 * This method provides scalar conversion for Java callers. Kotlin callers can use unit accessors
 * such as `5.meters.inFeet`.
 */
public fun Double.convert(from: LengthUnit, to: LengthUnit): Double = toLength(from).toDouble(to)

/**
 * Convert a value from one [AreaUnit] to another.
 *
 * This method provides scalar conversion for Java callers. Kotlin callers can use unit accessors
 * such as `5.squareMeters.inSquareFeet`.
 */
public fun Double.convert(from: AreaUnit, to: AreaUnit): Double = toArea(from).toDouble(to)

/**
 * Convert a value from one [RotationUnit] to another.
 *
 * This method provides scalar conversion for Java callers. Kotlin callers can use unit accessors
 * such as `5.radians.inDegrees`.
 */
public fun Double.convert(from: RotationUnit, to: RotationUnit): Double =
    toRotation(from).toDouble(to)
