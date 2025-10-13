package org.maplibre.spatialk.units

import kotlin.jvm.JvmInline
import kotlin.jvm.JvmSynthetic
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import org.maplibre.spatialk.units.DMS.ArcMinutes
import org.maplibre.spatialk.units.DMS.ArcSeconds
import org.maplibre.spatialk.units.DMS.Degrees
import org.maplibre.spatialk.units.International.Radians
import org.maplibre.spatialk.units.extensions.*

/**
 * Represents a magnitude of angular displacement, internally stored as a [Double] of radians. It
 * may be greater than a full turn (360 degrees).
 *
 * This representation does not define whether positive rotations are clockwise or anticlockwise, as
 * that depends on axis of rotation (and the observer's frame of reference in space).
 *
 * Most arithmetic operations are supported, and will automatically result in a [Rotation] or
 * [Bearing] depending on the operation.
 *
 * @see RotationUnit
 * @see Bearing
 */
@JvmInline
public value class Rotation private constructor(private val valueInRadians: Double) :
    Comparable<Rotation> {

    /** Returns the absolute value of this rotation. */
    public val absoluteValue: Rotation
        get() = Rotation(valueInRadians.absoluteValue)

    /** Returns `true` if this rotation is infinite (positive or negative). */
    public val isInfinite: Boolean
        get() =
            valueInRadians == Double.POSITIVE_INFINITY || valueInRadians == Double.NEGATIVE_INFINITY

    /** Returns `true` if this rotation is finite (not infinite). */
    public val isFinite: Boolean
        get() = !isInfinite

    /** Returns `true` if this rotation is greater than zero. */
    public val isPositive: Boolean
        get() = valueInRadians > 0

    /** Returns `true` if this rotation is less than zero. */
    public val isNegative: Boolean
        get() = valueInRadians < 0

    /** Returns `true` if this rotation is equal to zero. */
    public val isZero: Boolean
        get() = valueInRadians == 0.0

    /** Converts this rotation to a [Double] value in the specified [unit]. */
    public fun toDouble(unit: RotationUnit): Double = valueInRadians / unit.radiansPerUnit

    /** Converts this rotation to a [Float] value in the specified [unit]. */
    public fun toFloat(unit: RotationUnit): Float = toDouble(unit).toFloat()

    /** Rounds this rotation to a [Long] value in the specified [unit]. */
    public fun roundToLong(unit: RotationUnit): Long = toDouble(unit).roundToLong()

    /** Rounds this rotation to an [Int] value in the specified [unit]. */
    public fun roundToInt(unit: RotationUnit): Int = toDouble(unit).roundToInt()

    /** Returns the negation of this rotation. */
    public operator fun unaryMinus(): Rotation = Rotation(-valueInRadians)

    /** Returns this rotation unchanged. */
    public operator fun unaryPlus(): Rotation = Rotation(valueInRadians)

    /** Adds another rotation to this rotation. */
    public operator fun plus(other: Rotation): Rotation =
        Rotation(valueInRadians + other.valueInRadians)

    /** Adds this [Rotation] to a [Bearing]. */
    public operator fun plus(other: Bearing): Bearing = other + this

    /** Subtracts another rotation from this rotation. */
    public operator fun minus(other: Rotation): Rotation =
        Rotation(valueInRadians - other.valueInRadians)

    /** Multiplies this rotation by a scalar value. */
    public operator fun times(other: Double): Rotation = Rotation(valueInRadians * other)

    /** Divides this rotation by a scalar value. */
    public operator fun div(other: Double): Rotation = Rotation(valueInRadians / other)

    /** Divides this rotation by another rotation, resulting in a dimensionless scalar. */
    public operator fun div(other: Rotation): Double = valueInRadians / other.valueInRadians

    /** Returns the remainder of dividing this rotation by another rotation. */
    public operator fun rem(other: Rotation): Rotation =
        Rotation(valueInRadians % other.valueInRadians)

    /** Returns the modulo of this rotation with respect to another rotation. */
    public fun mod(other: Rotation): Rotation = Rotation(valueInRadians.mod(other.valueInRadians))

    /** Returns a string representation of this rotation in radians. */
    override fun toString(): String = toString(Radians)

    /**
     * Returns a formatted string representation of this rotation.
     *
     * @param unit The unit to display the rotation in.
     * @param decimalPlaces The number of decimal places to display.
     */
    public fun toString(unit: RotationUnit = Radians, decimalPlaces: Int = 2): String =
        unit.format(toDouble(unit), decimalPlaces)

    /**
     * Format this [Rotation] as [Degrees], [ArcMinutes], and [ArcSeconds] components.
     *
     * @param decimalPlaces the number of decimal places to use for the arc seconds component.
     */
    public fun toDmsString(decimalPlaces: Int = 2): String {
        val isNegative = this.isNegative
        val absolute = this.absoluteValue

        val degreesPart = absolute.inDegrees.toInt()
        var remainder = absolute - degreesPart.degrees
        val minutesPart = remainder.inArcMinutes.toInt()
        remainder -= minutesPart.arcMinutes
        val secondsPart = remainder.inArcSeconds

        val sign = if (isNegative) "-" else ""
        return "${sign}${degreesPart}${Degrees.symbol} " +
            "${minutesPart}${ArcMinutes.symbol} " +
            "${secondsPart.toRoundedString(decimalPlaces)}${ArcSeconds.symbol}"
    }

    override fun compareTo(other: Rotation): Int = valueInRadians.compareTo(other.valueInRadians)

    /** Companion object containing predefined rotation values. */
    public companion object {
        /** Zero rotation. */
        public val Zero: Rotation = Rotation(0.0)

        /** The maximum representable rotation value. */
        public val MaxValue: Rotation = Rotation(Double.MAX_VALUE)

        /** The minimum representable positive rotation value. */
        public val MinValue: Rotation = Rotation(Double.MIN_VALUE)

        /** Positive infinity rotation. */
        public val PositiveInfinity: Rotation = Rotation(Double.POSITIVE_INFINITY)

        /** Negative infinity rotation. */
        public val NegativeInfinity: Rotation = Rotation(Double.NEGATIVE_INFINITY)

        @JvmSynthetic
        internal fun of(value: Double, unit: RotationUnit) = Rotation(value * unit.radiansPerUnit)
    }
}
