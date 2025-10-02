package org.maplibre.spatialk.units

public sealed interface UnitOfMeasure {
    public val symbol: String

    public fun format(value: Double, decimalPlaces: Int = Int.MAX_VALUE): String =
        "${value.toRoundedString(decimalPlaces)} $symbol"
}
