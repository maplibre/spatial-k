package org.maplibre.spatialk.units

import org.maplibre.spatialk.units.extensions.toRoundedString

/**
 * Common interface for all units of measurement.
 *
 * @see LengthUnit
 * @see AreaUnit
 * @see RotationUnit
 */
public sealed interface UnitOfMeasure {
    /** The symbol used to represent this unit. */
    public val symbol: String

    /**
     * Formats a value with this unit's symbol.
     *
     * @param value The numeric value to format.
     * @param decimalPlaces The number of decimal places to display.
     * @return A formatted string representation of the value with the unit symbol.
     */
    public fun format(value: Double, decimalPlaces: Int = Int.MAX_VALUE): String {
        val rounded = value.toRoundedString(decimalPlaces)
        return if (symbol.length == 1 && !symbol[0].isLetter()) "$rounded$symbol"
        else "$rounded $symbol"
    }
}
