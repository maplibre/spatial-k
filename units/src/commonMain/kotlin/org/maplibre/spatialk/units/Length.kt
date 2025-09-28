package org.maplibre.spatialk.units

import kotlin.jvm.JvmInline
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import org.maplibre.spatialk.units.AreaUnit.International.SquareMeters
import org.maplibre.spatialk.units.LengthUnit.International.Meters

@JvmInline
public value class Length private constructor(private val valueInMeters: Double) :
    Comparable<Length> {

    public val absoluteValue: Length
        get() = Length(valueInMeters.absoluteValue)

    public val isInfinite: Boolean
        get() =
            valueInMeters == Double.POSITIVE_INFINITY || valueInMeters == Double.POSITIVE_INFINITY

    public val isFinite: Boolean
        get() = !isInfinite

    public val isPositive: Boolean
        get() = valueInMeters > 0

    public val isNegative: Boolean
        get() = valueInMeters < 0

    public val isZero: Boolean
        get() = valueInMeters == 0.0

    public fun toDouble(unit: LengthUnit): Double = valueInMeters / unit.metersPerUnit

    public fun toFloat(unit: LengthUnit): Float = toDouble(unit).toFloat()

    public fun roundToLong(unit: LengthUnit): Long = toDouble(unit).roundToLong()

    public fun roundToInt(unit: LengthUnit): Int = toDouble(unit).roundToInt()

    public operator fun plus(other: Length): Length = Length(valueInMeters + other.valueInMeters)

    public operator fun minus(other: Length): Length = Length(valueInMeters - other.valueInMeters)

    public operator fun times(other: Number): Length = Length(valueInMeters * other.toDouble())

    public operator fun times(other: Length): Area =
        Area.of(valueInMeters * other.valueInMeters, SquareMeters)

    public operator fun div(other: Number): Length = Length(valueInMeters / other.toDouble())

    public operator fun div(other: Length): Number = valueInMeters / other.valueInMeters

    override fun toString(): String = toString(Meters)

    public fun toString(unit: LengthUnit, decimalPlaces: Int = Int.MAX_VALUE): String =
        unit.format(toDouble(unit), decimalPlaces)

    override fun compareTo(other: Length): Int = valueInMeters.compareTo(other.valueInMeters)

    public companion object {
        public val ZERO: Length = Length(0.0)
        public val MAX_VALUE: Length = Length(Double.MAX_VALUE)
        public val MIN_VALUE: Length = Length(Double.MIN_VALUE)
        public val POSITIVE_INFINITY: Length = Length(Double.POSITIVE_INFINITY)
        public val NEGATIVE_INFINITY: Length = Length(Double.NEGATIVE_INFINITY)

        internal fun of(value: Number, unit: LengthUnit) =
            Length(value.toDouble() * unit.metersPerUnit)
    }
}
