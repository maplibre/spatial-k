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
 */
@JvmInline
public value class Rotation private constructor(private val valueInRadians: Double) :
    Comparable<Rotation> {

    public val absoluteValue: Rotation
        get() = Rotation(valueInRadians.absoluteValue)

    public val isInfinite: Boolean
        get() =
            valueInRadians == Double.POSITIVE_INFINITY || valueInRadians == Double.NEGATIVE_INFINITY

    public val isFinite: Boolean
        get() = !isInfinite

    public val isPositive: Boolean
        get() = valueInRadians > 0

    public val isNegative: Boolean
        get() = valueInRadians < 0

    public val isZero: Boolean
        get() = valueInRadians == 0.0

    public fun toDouble(unit: RotationUnit): Double = valueInRadians / unit.radiansPerUnit

    public fun toFloat(unit: RotationUnit): Float = toDouble(unit).toFloat()

    public fun roundToLong(unit: RotationUnit): Long = toDouble(unit).roundToLong()

    public fun roundToInt(unit: RotationUnit): Int = toDouble(unit).roundToInt()

    public operator fun unaryMinus(): Rotation = Rotation(-valueInRadians)

    public operator fun unaryPlus(): Rotation = Rotation(valueInRadians)

    public operator fun plus(other: Rotation): Rotation =
        Rotation(valueInRadians + other.valueInRadians)

    public operator fun plus(other: Bearing): Bearing = other + this

    public operator fun minus(other: Rotation): Rotation =
        Rotation(valueInRadians - other.valueInRadians)

    public operator fun times(other: Double): Rotation = Rotation(valueInRadians * other)

    public operator fun div(other: Double): Rotation = Rotation(valueInRadians / other)

    public operator fun div(other: Rotation): Double = valueInRadians / other.valueInRadians

    public operator fun rem(other: Rotation): Rotation =
        Rotation(valueInRadians % other.valueInRadians)

    public fun mod(other: Rotation): Rotation = Rotation(valueInRadians.mod(other.valueInRadians))

    override fun toString(): String = toString(Radians)

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

    public companion object {
        public val Zero: Rotation = Rotation(0.0)
        public val MaxValue: Rotation = Rotation(Double.MAX_VALUE)
        public val MinValue: Rotation = Rotation(Double.MIN_VALUE)
        public val PositiveInfinity: Rotation = Rotation(Double.POSITIVE_INFINITY)
        public val NegativeInfinity: Rotation = Rotation(Double.NEGATIVE_INFINITY)

        @JvmSynthetic
        internal fun of(value: Double, unit: RotationUnit) = Rotation(value * unit.radiansPerUnit)
    }
}
