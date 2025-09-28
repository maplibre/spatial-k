package org.maplibre.spatialk.units

import kotlin.jvm.JvmInline
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import org.maplibre.spatialk.units.AreaUnit.International.SquareMeters
import org.maplibre.spatialk.units.LengthUnit.International.Meters

@JvmInline
public value class Length private constructor(private val valueInMeters: Double) :
    Comparable<Length> {

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

    public fun isInfinite(): Boolean =
        valueInMeters == Double.POSITIVE_INFINITY || valueInMeters == Double.POSITIVE_INFINITY

    public fun isFinite(): Boolean = !isInfinite()

    override fun toString(): String = toString(Meters)

    public fun toString(unit: LengthUnit, decimalPlaces: Int = Int.MAX_VALUE): String =
        "${toDouble(unit).toRoundedString(decimalPlaces)} ${unit.symbol}"

    override fun compareTo(other: Length): Int = valueInMeters.compareTo(other.valueInMeters)

    internal companion object {
        fun of(value: Number, unit: LengthUnit) = Length(value.toDouble() * unit.metersPerUnit)
    }
}
