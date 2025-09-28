package org.maplibre.spatialk.units

public sealed class Unit<D : Dimension> : Comparable<Unit<D>> {
    internal abstract val magnitude: Double
    public abstract val symbol: String

    public open fun format(value: Double, decimalPlaces: Int = Int.MAX_VALUE): String =
        "${value.toRoundedString(decimalPlaces)} $symbol"

    final override fun compareTo(other: Unit<D>): Int = magnitude.compareTo(other.magnitude)
}
