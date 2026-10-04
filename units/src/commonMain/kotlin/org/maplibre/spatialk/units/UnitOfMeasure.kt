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
     * Numbers are written in plain decimal notation, never in exponent notation, and padded to the
     * requested precision. Values too large to round at the requested precision are written in full
     * without rounding or padding. Nonfinite values are written as `NaN`, `Infinity`, or
     * `-Infinity`.
     *
     * @param value The numeric value to format.
     * @param decimalPlaces The number of decimal places to round to, from 0 to 15, or
     *   [Int.MAX_VALUE] (the default) to write every digit of the value without rounding or
     *   padding.
     * @return A formatted string representation of the value with the unit symbol.
     */
    public fun format(value: Double, decimalPlaces: Int = Int.MAX_VALUE): String =
        formatWithSymbol(value.toRoundedString(decimalPlaces), symbol)
}
