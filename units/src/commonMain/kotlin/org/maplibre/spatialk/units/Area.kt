package org.maplibre.spatialk.units

import kotlin.jvm.JvmInline
import kotlin.jvm.JvmSynthetic
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import org.maplibre.spatialk.units.International.Meters
import org.maplibre.spatialk.units.International.SquareMeters

/**
 * Represents an area, internally stored as a [Double] of square meters.
 *
 * Most arithmetic operations are supported, and will automatically result in a scalar, [Length], or
 * [Area] depending on the operation.
 *
 * @see [AreaUnit]
 */
@JvmInline
public value class Area private constructor(private val valueInMetersSquared: Double) :
    Comparable<Area> {

    public val absoluteValue: Area
        get() = Area(valueInMetersSquared.absoluteValue)

    public val isInfinite: Boolean
        get() =
            valueInMetersSquared == Double.POSITIVE_INFINITY ||
                valueInMetersSquared == Double.POSITIVE_INFINITY

    public val isFinite: Boolean
        get() = !isInfinite

    public val isPositive: Boolean
        get() = valueInMetersSquared > 0

    public val isNegative: Boolean
        get() = valueInMetersSquared < 0

    public val isZero: Boolean
        get() = valueInMetersSquared == 0.0

    public fun toDouble(unit: AreaUnit): Double = valueInMetersSquared / unit.metersSquaredPerUnit

    public fun toFloat(unit: AreaUnit): Float = toDouble(unit).toFloat()

    public fun roundToLong(unit: AreaUnit): Long = toDouble(unit).roundToLong()

    public fun roundToInt(unit: AreaUnit): Int = toDouble(unit).roundToInt()

    public operator fun plus(other: Area): Area =
        Area(valueInMetersSquared + other.valueInMetersSquared)

    public operator fun unaryMinus(): Area = Area(-valueInMetersSquared)

    public operator fun unaryPlus(): Area = Area(valueInMetersSquared)

    public operator fun minus(other: Area): Area =
        Area(valueInMetersSquared - other.valueInMetersSquared)

    public operator fun times(other: Double): Area = Area(valueInMetersSquared * other)

    public operator fun div(other: Double): Area = Area(valueInMetersSquared / other)

    public operator fun div(other: Length): Length =
        Length.of(valueInMetersSquared / other.toDouble(Meters), Meters)

    public operator fun div(other: Area): Double = valueInMetersSquared / other.valueInMetersSquared

    public override fun toString(): String = toString(SquareMeters)

    public fun toString(unit: AreaUnit, decimalPlaces: Int = Int.MAX_VALUE): String =
        unit.format(toDouble(unit), decimalPlaces)

    override fun compareTo(other: Area): Int =
        valueInMetersSquared.compareTo(other.valueInMetersSquared)

    public companion object {

        public val Zero: Area = Area(0.0)
        public val MaxValue: Area = Area(Double.MAX_VALUE)
        public val MinValue: Area = Area(Double.MIN_VALUE)
        public val PositiveInfinity: Area = Area(Double.POSITIVE_INFINITY)
        public val NegativeInfinity: Area = Area(Double.NEGATIVE_INFINITY)

        @JvmSynthetic
        internal fun of(value: Double, unit: AreaUnit) = Area(value * unit.metersSquaredPerUnit)
    }
}
