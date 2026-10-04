@file:OptIn(ExperimentalObjCName::class, ExperimentalObjCRefinement::class)

package org.maplibre.spatialk.units

import kotlin.experimental.ExperimentalObjCName
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.jvm.JvmField
import kotlin.jvm.JvmName
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic
import kotlin.native.HiddenFromObjC
import kotlin.native.ObjCName
import org.maplibre.spatialk.units.Units.Feet
import org.maplibre.spatialk.units.Units.Inches
import org.maplibre.spatialk.units.Units.Kilometers
import org.maplibre.spatialk.units.Units.Meters
import org.maplibre.spatialk.units.Units.Miles
import org.maplibre.spatialk.units.Units.NauticalMiles
import org.maplibre.spatialk.units.Units.Yards

/**
 * Parses and formats [Length] values written as a number followed by a unit symbol, such as `3 m`,
 * or as a compound of several units, such as `12'5"`.
 *
 * Create one with `LengthFormat { ... }` (see [LengthFormat.Companion.invoke]), or use a preset
 * such as [LengthFormat.Osm].
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
public class LengthFormat internal constructor(internal val spec: QuantityFormat<LengthUnit>) {

    /**
     * Parses [input] as a [Length]. From Java, Objective-C, and Swift, the result is in meters.
     *
     * @throws IllegalArgumentException if [input] does not match this format.
     */
    @Throws(IllegalArgumentException::class)
    @JvmName("parse")
    public fun parse(input: String): Length =
        requireNotNull(parseOrNull(input)) { "Cannot parse '$input' as a length." }

    /** Parses [input] as a [Length], or returns `null` if it does not match this format. */
    @HiddenFromObjC
    public fun parseOrNull(input: String): Length? =
        spec.parseOrNull(input)?.let { Length.of(it, Meters) }

    /**
     * Formats [value] in [unit], using the first entry that contains [unit].
     *
     * A simple entry writes the number and the entry's symbol, spaced as in [UnitOfMeasure.format].
     * A compound entry writes every part, from the largest down, with whole numbers in all but the
     * last part. Rounding the last part carries into larger parts, and a negative value gets one
     * leading `-`. A nonfinite value is written with only the first part's symbol. Formatting
     * always uses canonical symbols, even when the format is not strict.
     *
     * @param value The length to format. From Java, Objective-C, and Swift, it is in meters.
     * @param decimalPlaces The number of decimal places for the number, or for the last part of a
     *   compound; see [UnitOfMeasure.format]. With the default, the last part of a compound is
     *   rounded to 12 significant digits of the whole value, to hide floating-point error.
     * @throws IllegalArgumentException if no entry contains [unit], or [decimalPlaces] is out of
     *   range.
     */
    @Throws(IllegalArgumentException::class)
    @JvmOverloads
    @JvmName("format")
    public fun format(value: Length, unit: LengthUnit, decimalPlaces: Int = Int.MAX_VALUE): String =
        spec.format(value.toDouble(Meters), unit, decimalPlaces)

    /** Builds [LengthFormat]s and holds predefined ones. */
    public companion object {
        /**
         * Builds a [LengthFormat], used as `LengthFormat { ... }`. If [base] is not `null`, the
         * format starts with its entries, default unit, and strict setting, and entries declared in
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
            base: LengthFormat? = null,
            configure: Builder.() -> Unit,
        ): LengthFormat = Builder(base).apply(configure).build()

        /**
         * Lengths as written in OpenStreetMap tag values, such as `width=3.5` or `maxheight=12'6"`.
         * See [Map features/Units](https://wiki.openstreetmap.org/wiki/Map_features/Units).
         *
         * Entries, in order:
         * - [Meters] as `m`, with aliases `metre`, `metres`, `meter`, and `meters`.
         * - [Kilometers] as `km`, with aliases `kilometre`, `kilometres`, `kilometer`, and
         *   `kilometers`.
         * - [Miles] as `mi`, with aliases `mile` and `miles`.
         * - [NauticalMiles] as `nmi`.
         * - [Yards] as `yd`, with alias `yds`.
         * - A compound of [Feet] as `'` (apostrophe) and [Inches] as `"` (quotation mark), with no
         *   separator and aliases `ft` and `in`. For example, `12'5"`, `12'`, `5"`, `12 ft`, and
         *   `12 ft 5 in` all parse. Formatting omits zero inches, so 12 feet is written `12'`.
         *
         * A number without a symbol is in meters. Parsing is lenient.
         *
         * Some keys have a different default unit. Use a copy of this format for them, such as
         * `LengthFormat(LengthFormat.Osm) { defaultUnit = Millimeters }` for railway `gauge`,
         * [Kilometers] for `distance`, or [NauticalMiles] for distances at sea.
         */
        @JvmField
        @ObjCName(swiftName = "osm")
        public val Osm: LengthFormat = LengthFormat {
            unit(Meters, aliases = listOf("metre", "metres", "meter", "meters"))
            unit(Kilometers, aliases = listOf("kilometre", "kilometres", "kilometer", "kilometers"))
            unit(Miles, aliases = listOf("mile", "miles"))
            unit(NauticalMiles)
            unit(Yards, aliases = listOf("yds"))
            compound(omitTrailingZeroParts = true) {
                part(Feet, "'", aliases = listOf("ft"))
                part(Inches, "\"", aliases = listOf("in"))
            }
            defaultUnit = Meters
        }
    }

    /**
     * Configures a [LengthFormat]. Use it through [LengthFormat.Companion.invoke].
     *
     * A unit, symbol, or alias may appear in several entries, so an input can fall back to another
     * way of writing the same unit. Parsing uses the first entry that matches, and formatting the
     * first entry that contains the unit.
     */
    @UnitFormatDsl
    public class Builder internal constructor(base: LengthFormat?) {
        private val builder = QuantityFormatBuilder(base?.spec)

        /** The unit for a number without a symbol, or `null` to reject such input. */
        public var defaultUnit: LengthUnit?
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
            unit: LengthUnit,
            symbol: String = unit.symbol,
            aliases: List<String> = emptyList(),
        ) {
            builder.unit(unit, symbol, aliases)
        }

        /**
         * Adds an entry for a compound of several units, such as feet and inches in `12'5"`.
         *
         * When parsing, each part is optional but at least one must be present, and only the last
         * present part may have a fractional value.
         *
         * @param separator The text written between parts.
         * @param omitTrailingZeroParts Whether formatting leaves out parts after the first that are
         *   zero once rounded and have no nonzero part after them, such as writing `12'` instead of
         *   `12'0"`.
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

        internal fun build(): LengthFormat = LengthFormat(builder.build { it.metersPerUnit })
    }

    /** Declares the parts of a compound entry. Use it through [Builder.compound]. */
    @UnitFormatDsl
    public class CompoundBuilder internal constructor() {
        internal val parts = mutableListOf<FormatPart<LengthUnit>>()

        /**
         * Adds the next smaller part of the compound.
         *
         * @param symbol The symbol to write and parse.
         * @param aliases Other symbols accepted when parsing in lenient mode.
         */
        @JvmOverloads
        public fun part(
            unit: LengthUnit,
            symbol: String = unit.symbol,
            aliases: List<String> = emptyList(),
        ) {
            parts += FormatPart(unit, symbol, aliases.toList())
        }
    }
}
