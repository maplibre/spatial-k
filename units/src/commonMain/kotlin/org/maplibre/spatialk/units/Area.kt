package org.maplibre.spatialk.units

import kotlin.jvm.JvmInline
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import org.maplibre.spatialk.units.AreaUnit.International.SquareMeters
import org.maplibre.spatialk.units.LengthUnit.International.Meters

@JvmInline
public value class Area private constructor(private val valueInMetersSquared: Double) :
    Comparable<Area> {

    public fun toDouble(unit: AreaUnit): Double = valueInMetersSquared / unit.metersSquaredPerUnit

    public fun toFloat(unit: AreaUnit): Float = toDouble(unit).toFloat()

    public fun roundToLong(unit: AreaUnit): Long = toDouble(unit).roundToLong()

    public fun roundToInt(unit: AreaUnit): Int = toDouble(unit).roundToInt()

    public operator fun plus(other: Area): Area =
        Area(valueInMetersSquared + other.valueInMetersSquared)

    public operator fun minus(other: Area): Area =
        Area(valueInMetersSquared - other.valueInMetersSquared)

    public operator fun times(other: Number): Area = Area(valueInMetersSquared * other.toDouble())

    public operator fun div(other: Number): Area = Area(valueInMetersSquared / other.toDouble())

    public operator fun div(other: Length): Length =
        Length.of(valueInMetersSquared / other.toDouble(Meters), Meters)

    public operator fun div(other: Area): Double = valueInMetersSquared / other.valueInMetersSquared

    public fun isInfinite(): Boolean =
        valueInMetersSquared == Double.POSITIVE_INFINITY ||
            valueInMetersSquared == Double.POSITIVE_INFINITY

    public fun isFinite(): Boolean = !isInfinite()

    public override fun toString(): String = toString(SquareMeters)

    public fun toString(unit: AreaUnit, decimalPlaces: Int = Int.MAX_VALUE): String =
        "${toDouble(unit).toRoundedString(decimalPlaces)} ${unit.symbol}"

    override fun compareTo(other: Area): Int =
        valueInMetersSquared.compareTo(other.valueInMetersSquared)

    internal companion object {
        fun of(value: Number, unit: AreaUnit) = Area(value.toDouble() * unit.metersSquaredPerUnit)
    }
}
