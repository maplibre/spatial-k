package org.maplibre.spatialk.units

import kotlin.jvm.JvmInline
import kotlin.jvm.JvmSynthetic
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlinx.serialization.Serializable
import org.maplibre.spatialk.units.International.Meters
import org.maplibre.spatialk.units.International.SquareMeters

/**
 * Represents an area, internally stored as a [Double] of square meters.
 *
 * Most arithmetic operations are supported, and will automatically result in a scalar, [Length], or
 * [Area] depending on the operation.
 *
 * @see AreaUnit
 * @see Length
 */
@JvmInline
@Serializable
public value class Area private constructor(private val valueInMetersSquared: Double) :
    Comparable<Area> {

    /** Returns the absolute value of this area. */
    public val absoluteValue: Area
        get() = Area(valueInMetersSquared.absoluteValue)

    /** Returns `true` if this area is infinite (positive or negative). */
    public val isInfinite: Boolean
        get() =
            valueInMetersSquared == Double.POSITIVE_INFINITY ||
                valueInMetersSquared == Double.POSITIVE_INFINITY

    /** Returns `true` if this area is finite (not infinite). */
    public val isFinite: Boolean
        get() = !isInfinite

    /** Returns `true` if this area is greater than zero. */
    public val isPositive: Boolean
        get() = valueInMetersSquared > 0

    /** Returns `true` if this area is less than zero. */
    public val isNegative: Boolean
        get() = valueInMetersSquared < 0

    /** Returns `true` if this area is equal to zero. */
    public val isZero: Boolean
        get() = valueInMetersSquared == 0.0

    /** Converts this area to a [Double] value in the specified [unit]. */
    public fun toDouble(unit: AreaUnit): Double = valueInMetersSquared / unit.metersSquaredPerUnit

    /** Converts this area to a [Float] value in the specified [unit]. */
    public fun toFloat(unit: AreaUnit): Float = toDouble(unit).toFloat()

    /** Rounds this area to a [Long] value in the specified [unit]. */
    public fun roundToLong(unit: AreaUnit): Long = toDouble(unit).roundToLong()

    /** Rounds this area to an [Int] value in the specified [unit]. */
    public fun roundToInt(unit: AreaUnit): Int = toDouble(unit).roundToInt()

    /** Returns the negation of this area. */
    public operator fun unaryMinus(): Area = Area(-valueInMetersSquared)

    /** Returns this area unchanged. */
    public operator fun unaryPlus(): Area = Area(valueInMetersSquared)

    /** Adds another area to this area. */
    public operator fun plus(other: Area): Area =
        Area(valueInMetersSquared + other.valueInMetersSquared)

    /** Subtracts another area from this area. */
    public operator fun minus(other: Area): Area =
        Area(valueInMetersSquared - other.valueInMetersSquared)

    /** Multiplies this area by a scalar value. */
    public operator fun times(other: Double): Area = Area(valueInMetersSquared * other)

    /** Divides this area by a scalar value. */
    public operator fun div(other: Double): Area = Area(valueInMetersSquared / other)

    /** Divides this [Area] by a [Length], resulting in another [Length]. */
    public operator fun div(other: Length): Length =
        Length.of(valueInMetersSquared / other.toDouble(Meters), Meters)

    /** Divides this area by another area, resulting in a dimensionless scalar. */
    public operator fun div(other: Area): Double = valueInMetersSquared / other.valueInMetersSquared

    /** Returns the remainder of dividing this area by another area. */
    public operator fun rem(other: Area): Area =
        Area(valueInMetersSquared % other.valueInMetersSquared)

    /** Returns the modulo of this area with respect to another area. */
    public fun mod(other: Area): Area = Area(valueInMetersSquared.mod(other.valueInMetersSquared))

    /** Returns a string representation of this area in square meters. */
    public override fun toString(): String = toString(SquareMeters)

    /**
     * Returns a formatted string representation of this area.
     *
     * @param unit The unit to display the area in.
     * @param decimalPlaces The number of decimal places to display.
     */
    public fun toString(unit: AreaUnit = SquareMeters, decimalPlaces: Int = 2): String =
        unit.format(toDouble(unit), decimalPlaces)

    override fun compareTo(other: Area): Int =
        valueInMetersSquared.compareTo(other.valueInMetersSquared)

    /** Companion object containing predefined area values. */
    public companion object {

        /** Zero area. */
        public val Zero: Area = Area(0.0)

        /** The maximum representable area value. */
        public val MaxValue: Area = Area(Double.MAX_VALUE)

        /** The minimum representable positive area value. */
        public val MinValue: Area = Area(Double.MIN_VALUE)

        /** Positive infinity area. */
        public val PositiveInfinity: Area = Area(Double.POSITIVE_INFINITY)

        /** Negative infinity area. */
        public val NegativeInfinity: Area = Area(Double.NEGATIVE_INFINITY)

        @JvmSynthetic
        internal fun of(value: Double, unit: AreaUnit) = Area(value * unit.metersSquaredPerUnit)
    }
}
