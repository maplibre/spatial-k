package org.maplibre.spatialk.units

import kotlin.jvm.JvmInline
import kotlin.jvm.JvmSynthetic
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import org.maplibre.spatialk.units.International.Meters
import org.maplibre.spatialk.units.International.SquareMeters

/**
 * Represents a length or distance, internally stored as a [Double] of meters.
 *
 * Most arithmetic operations are supported, and will automatically result in a scalar, [Length], or
 * [Area] depending on the operation.
 *
 * @see LengthUnit
 * @see Area
 */
@JvmInline
public value class Length private constructor(private val valueInMeters: Double) :
    Comparable<Length> {

    /** Returns the absolute value of this length. */
    public val absoluteValue: Length
        get() = Length(valueInMeters.absoluteValue)

    /** Returns `true` if this length is infinite (positive or negative). */
    public val isInfinite: Boolean
        get() =
            valueInMeters == Double.POSITIVE_INFINITY || valueInMeters == Double.POSITIVE_INFINITY

    /** Returns `true` if this length is finite (not infinite). */
    public val isFinite: Boolean
        get() = !isInfinite

    /** Returns `true` if this length is greater than zero. */
    public val isPositive: Boolean
        get() = valueInMeters > 0

    /** Returns `true` if this length is less than zero. */
    public val isNegative: Boolean
        get() = valueInMeters < 0

    /** Returns `true` if this length is equal to zero. */
    public val isZero: Boolean
        get() = valueInMeters == 0.0

    /** Converts this length to a [Double] value in the specified [unit]. */
    public fun toDouble(unit: LengthUnit): Double = valueInMeters / unit.metersPerUnit

    /** Converts this length to a [Float] value in the specified [unit]. */
    public fun toFloat(unit: LengthUnit): Float = toDouble(unit).toFloat()

    /** Rounds this length to a [Long] value in the specified [unit]. */
    public fun roundToLong(unit: LengthUnit): Long = toDouble(unit).roundToLong()

    /** Rounds this length to an [Int] value in the specified [unit]. */
    public fun roundToInt(unit: LengthUnit): Int = toDouble(unit).roundToInt()

    /** Returns the negation of this length. */
    public operator fun unaryMinus(): Length = Length(-valueInMeters)

    /** Returns this length unchanged. */
    public operator fun unaryPlus(): Length = Length(valueInMeters)

    /** Adds another length to this length. */
    public operator fun plus(other: Length): Length = Length(valueInMeters + other.valueInMeters)

    /** Subtracts another length from this length. */
    public operator fun minus(other: Length): Length = Length(valueInMeters - other.valueInMeters)

    /** Multiplies this length by a scalar value. */
    public operator fun times(other: Double): Length = Length(valueInMeters * other)

    /** Multiplies this [Length] by another [Length], resulting in an [Area]. */
    public operator fun times(other: Length): Area =
        Area.of(valueInMeters * other.valueInMeters, SquareMeters)

    /** Divides this length by a scalar value. */
    public operator fun div(other: Double): Length = Length(valueInMeters / other)

    /** Divides this length by another length, resulting in a dimensionless scalar. */
    public operator fun div(other: Length): Double = valueInMeters / other.valueInMeters

    /** Returns the remainder of dividing this length by another length. */
    public operator fun rem(other: Length): Length = Length(valueInMeters % other.valueInMeters)

    /** Returns the modulo of this length with respect to another length. */
    public fun mod(other: Length): Length = Length(valueInMeters.mod(other.valueInMeters))

    /** Returns a string representation of this length in meters. */
    override fun toString(): String = toString(Meters)

    /**
     * Returns a formatted string representation of this length.
     *
     * @param unit The unit to display the length in.
     * @param decimalPlaces The number of decimal places to display.
     */
    public fun toString(unit: LengthUnit = Meters, decimalPlaces: Int = 2): String =
        unit.format(toDouble(unit), decimalPlaces)

    override fun compareTo(other: Length): Int = valueInMeters.compareTo(other.valueInMeters)

    /** Companion object containing predefined length values. */
    public companion object {
        /** Zero length. */
        public val Zero: Length = Length(0.0)

        /** The maximum representable length value. */
        public val MaxValue: Length = Length(Double.MAX_VALUE)

        /** The minimum representable positive length value. */
        public val MinValue: Length = Length(Double.MIN_VALUE)

        /** Positive infinity length. */
        public val PositiveInfinity: Length = Length(Double.POSITIVE_INFINITY)

        /** Negative infinity length. */
        public val NegativeInfinity: Length = Length(Double.NEGATIVE_INFINITY)

        @JvmSynthetic
        internal fun of(value: Double, unit: LengthUnit) = Length(value * unit.metersPerUnit)
    }
}
