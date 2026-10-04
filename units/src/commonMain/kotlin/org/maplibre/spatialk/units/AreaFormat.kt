@file:OptIn(ExperimentalObjCName::class, ExperimentalObjCRefinement::class)

package org.maplibre.spatialk.units

import kotlin.experimental.ExperimentalObjCName
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.jvm.JvmName
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic
import kotlin.native.HiddenFromObjC
import kotlin.native.ObjCName
import org.maplibre.spatialk.units.Units.SquareMeters

/**
 * Parses and formats [Area] values written as a number followed by a unit symbol, such as `3 ha`,
 * or as a compound of several units, such as `2 ไร่ 1 งาน 50 ตร.วา`.
 *
 * Create one with `AreaFormat { ... }` (see [AreaFormat.Companion.invoke]).
 *
 * A format is a list of entries, each declared with [Builder.unit] or [Builder.compound].
 *
 * Parsing tries the entries in declaration order, and the first entry that matches the whole input
 * wins. A number is an optional leading `-`, digits, and an optional `.` followed by digits. A
 * comma is never a decimal separator, and exponent notation is not accepted. A number without a
 * symbol uses [Builder.defaultUnit], and fails to parse if there is none.
 *
 * In strict mode, the input must use canonical symbols and spacing, exactly as [format] writes
 * them. Otherwise, parsing also accepts the aliases of each entry, any amount of whitespace
 * (including none) between a number and its symbol and between compound parts, and whitespace
 * around the whole input.
 */
public class AreaFormat internal constructor(internal val spec: QuantityFormat<AreaUnit>) {

    /**
     * Parses [input] as an [Area]. From Java, Objective-C, and Swift, the result is in square
     * meters.
     *
     * @throws IllegalArgumentException if [input] does not match this format.
     */
    @Throws(IllegalArgumentException::class)
    @JvmName("parse")
    public fun parse(input: String): Area =
        requireNotNull(parseOrNull(input)) { "Cannot parse '$input' as an area." }

    /** Parses [input] as an [Area], or returns `null` if it does not match this format. */
    @HiddenFromObjC
    public fun parseOrNull(input: String): Area? =
        spec.parseOrNull(input)?.let { Area.of(it, SquareMeters) }

    /**
     * Formats [value] in [unit], using the first entry that contains [unit].
     *
     * A simple entry writes the number and the entry's symbol, spaced as in [UnitOfMeasure.format].
     * A compound entry writes every part, from the largest down, with whole numbers in all but the
     * last part. Rounding the last part carries into larger parts, and a negative value gets one
     * leading `-`. A nonfinite value is written with only the first part's symbol. Formatting
     * always uses canonical symbols, even when the format is not strict.
     *
     * @param value The area to format. From Java, Objective-C, and Swift, it is in square meters.
     * @param decimalPlaces The number of decimal places for the number, or for the last part of a
     *   compound; see [UnitOfMeasure.format]. With the default, the last part of a compound is
     *   rounded to 12 significant digits of the whole value, to hide floating-point error.
     * @throws IllegalArgumentException if no entry contains [unit], or [decimalPlaces] is out of
     *   range.
     */
    @Throws(IllegalArgumentException::class)
    @JvmOverloads
    @JvmName("format")
    public fun format(value: Area, unit: AreaUnit, decimalPlaces: Int = Int.MAX_VALUE): String =
        spec.format(value.toDouble(SquareMeters), unit, decimalPlaces)

    /** Builds [AreaFormat]s. */
    public companion object {
        /**
         * Builds a [AreaFormat], used as `AreaFormat { ... }`. If [base] is not `null`, the format
         * starts with its entries, default unit, and strict setting, and entries declared in
         * [configure] are added after them. From Java, Objective-C, and Swift, this is `build`.
         *
         * @throws IllegalArgumentException if a compound has fewer than 2 parts or parts not in
         *   strictly descending unit size, or a symbol or alias is blank or has surrounding
         *   whitespace.
         */
        @Throws(IllegalArgumentException::class)
        @JvmStatic
        @JvmOverloads
        @JvmName("build")
        @ObjCName("build")
        public operator fun invoke(
            base: AreaFormat? = null,
            configure: Builder.() -> Unit,
        ): AreaFormat = Builder(base).apply(configure).build()
    }

    /**
     * Configures a [AreaFormat]. Use it through [AreaFormat.Companion.invoke].
     *
     * A unit, symbol, or alias may appear in several entries, so an input can fall back to another
     * way of writing the same unit. Parsing uses the first entry that matches, and formatting the
     * first entry that contains the unit.
     */
    @UnitFormatDsl
    public class Builder internal constructor(base: AreaFormat?) {
        private val builder = QuantityFormatBuilder(base?.spec)

        /** The unit for a number without a symbol, or `null` to reject such input. */
        public var defaultUnit: AreaUnit?
            get() = builder.defaultUnit
            set(value) {
                builder.defaultUnit = value
            }

        /** Whether parsing accepts only canonical symbols and spacing. Defaults to `false`. */
        public var strict: Boolean
            get() = builder.strict
            set(value) {
                builder.strict = value
            }

        /**
         * Adds an entry for a number followed by one [unit].
         *
         * @param symbol The symbol to write and parse.
         * @param aliases Other symbols accepted when parsing in lenient mode.
         */
        @JvmOverloads
        public fun unit(
            unit: AreaUnit,
            symbol: String = unit.symbol,
            aliases: List<String> = emptyList(),
        ) {
            builder.unit(unit, symbol, aliases)
        }

        /**
         * Adds an entry for a compound of several units, such as rai, ngan, and square wa in `2 ไร่
         * 1 งาน 50 ตร.วา`.
         *
         * When parsing, each part is optional but at least one must be present, and only the last
         * present part may have a fractional value.
         *
         * @param separator The text written between parts.
         * @param omitTrailingZeroParts Whether formatting leaves out parts after the first that are
         *   zero once rounded and have no nonzero part after them, such as writing `2 ไร่` instead
         *   of `2 ไร่ 0 งาน 0 ตร.วา`.
         * @param configure Declares at least 2 parts, in strictly descending unit size.
         */
        @JvmOverloads
        public fun compound(
            separator: String = "",
            omitTrailingZeroParts: Boolean = false,
            configure: CompoundBuilder.() -> Unit,
        ) {
            builder.compound(
                separator,
                omitTrailingZeroParts,
                CompoundBuilder().apply(configure).parts,
            )
        }

        internal fun build(): AreaFormat = AreaFormat(builder.build { it.metersSquaredPerUnit })
    }

    /** Declares the parts of a compound entry. Use it through [Builder.compound]. */
    @UnitFormatDsl
    public class CompoundBuilder internal constructor() {
        internal val parts = mutableListOf<FormatPart<AreaUnit>>()

        /**
         * Adds the next smaller part of the compound.
         *
         * @param symbol The symbol to write and parse.
         * @param aliases Other symbols accepted when parsing in lenient mode.
         */
        @JvmOverloads
        public fun part(
            unit: AreaUnit,
            symbol: String = unit.symbol,
            aliases: List<String> = emptyList(),
        ) {
            parts += FormatPart(unit, symbol, aliases.toList())
        }
    }
}
