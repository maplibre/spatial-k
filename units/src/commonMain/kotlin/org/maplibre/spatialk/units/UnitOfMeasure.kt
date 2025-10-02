package org.maplibre.spatialk.units

public sealed interface UnitOfMeasure {
    public val symbol: String

    public fun format(value: Double, decimalPlaces: Int = Int.MAX_VALUE): String {
        val rounded = value.toRoundedString(decimalPlaces)
        return if (symbol == "°") "$rounded$symbol" else "$rounded $symbol"
    }
}
