package org.maplibre.spatialk.units

import kotlin.jvm.JvmInline
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlin.math.roundToLong

@JvmInline
public value class Measurement<D : Dimension> internal constructor(private val rawValue: Double) :
    Comparable<Measurement<D>> {

    public val absoluteValue: Measurement<D>
        get() = Measurement(rawValue.absoluteValue)

    public val isInfinite: Boolean
        get() = rawValue == Double.POSITIVE_INFINITY || rawValue == Double.POSITIVE_INFINITY

    public val isFinite: Boolean
        get() = !isInfinite

    public val isPositive: Boolean
        get() = rawValue > 0

    public val isNegative: Boolean
        get() = rawValue < 0

    public val isZero: Boolean
        get() = rawValue == 0.0

    public fun toDouble(unit: Unit<D>): Double = rawValue / unit.magnitude

    public fun toFloat(unit: Unit<D>): Float = toDouble(unit).toFloat()

    public fun roundToLong(unit: Unit<D>): Long = toDouble(unit).roundToLong()

    public fun roundToInt(unit: Unit<D>): Int = toDouble(unit).roundToInt()

    public operator fun plus(other: Measurement<D>): Measurement<D> =
        Measurement(rawValue + other.rawValue)

    public operator fun minus(other: Measurement<D>): Measurement<D> =
        Measurement(rawValue - other.rawValue)

    public operator fun times(other: Number): Measurement<D> =
        Measurement(rawValue * other.toDouble())

    public operator fun div(other: Number): Measurement<D> =
        Measurement(rawValue / other.toDouble())

    public operator fun div(other: Measurement<D>): Number = rawValue / other.rawValue

    public fun format(unit: Unit<D>, decimalPlaces: Int = Int.MAX_VALUE): String =
        unit.format(toDouble(unit), decimalPlaces)

    override fun compareTo(other: Measurement<D>): Int = rawValue.compareTo(other.rawValue)
}
