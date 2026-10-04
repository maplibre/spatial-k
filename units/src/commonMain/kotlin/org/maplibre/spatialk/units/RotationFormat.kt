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
import org.maplibre.spatialk.units.Units.ArcMinutes
import org.maplibre.spatialk.units.Units.ArcSeconds
import org.maplibre.spatialk.units.Units.Degrees

/**
 * Parses and formats [Rotation] values written as a number followed by a unit symbol, such as
 * `45°`, or as a compound of several units, such as `12° 30′ 15″`.
 *
 * Create one with `RotationFormat { ... }` (see [RotationFormat.Companion.invoke]), or use a preset
 * such as [RotationFormat.Dms] or [RotationFormat.Osm].
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
public class RotationFormat
internal constructor(internal val spec: QuantityFormat<Rotation, RotationUnit>) {

    /**
     * Parses [input] as a [Rotation]. From Java, Objective-C, and Swift, the result is in degrees.
     *
     * @throws IllegalArgumentException if [input] does not match this format.
     */
    @Throws(IllegalArgumentException::class)
    @JvmName("parse")
    public fun parse(input: String): Rotation =
        requireNotNull(parseOrNull(input)) { "Cannot parse '$input' as a rotation." }

    /** Parses [input] as a [Rotation], or returns `null` if it does not match this format. */
    @HiddenFromObjC public fun parseOrNull(input: String): Rotation? = spec.parseOrNull(input)

    /**
     * Formats [value] in [unit], using the first entry that contains [unit], as described for
     * [Builder.unit] and [Builder.compound].
     *
     * @param value The rotation to format. From Java, Objective-C, and Swift, it is in degrees.
     * @param decimalPlaces The number of decimal places for the number, or for the last part of a
     *   compound; see [UnitOfMeasure.format].
     * @throws IllegalArgumentException if no entry contains [unit], or [decimalPlaces] is out of
     *   range.
     */
    @Throws(IllegalArgumentException::class)
    @JvmOverloads
    @JvmName("format")
    public fun format(
        value: Rotation,
        unit: RotationUnit,
        decimalPlaces: Int = Int.MAX_VALUE,
    ): String = spec.format(value, unit, decimalPlaces)

    /** Builds [RotationFormat]s and holds predefined ones. */
    public companion object {
        /**
         * Builds a [RotationFormat], used as `RotationFormat { ... }`. If [base] is not `null`, the
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
            base: RotationFormat? = null,
            configure: Builder.() -> Unit,
        ): RotationFormat = Builder(base).apply(configure).build()

        /**
         * Angles as written in OpenStreetMap tag values, such as `direction=45°`. See
         * [Map features/Units](https://wiki.openstreetmap.org/wiki/Map_features/Units).
         *
         * Entries, in order:
         * - [Degrees] as `°`, with alias `º` (masculine ordinal indicator).
         *
         * A number without a symbol does not parse. For keys where it means degrees, such as
         * `direction`, use `RotationFormat(RotationFormat.Osm) { defaultUnit = Degrees }`. Parsing
         * is lenient. Inclines in percent or per mille, such as `10%`, are grades rather than
         * rotations and do not parse.
         */
        @JvmField
        @ObjCName(swiftName = "osm")
        public val Osm: RotationFormat = RotationFormat { unit(Degrees, aliases = listOf("º")) }

        /**
         * Angles in degrees, arcminutes, and arcseconds, such as `12° 30′ 15″`, as written by
         * [Rotation.toDmsString].
         *
         * Entries, in order:
         * - A compound of [Degrees] as `°`, [ArcMinutes] as `′` (prime), and [ArcSeconds] as `″`
         *   (double prime), separated by a space, with aliases `º` (masculine ordinal indicator),
         *   `'` (apostrophe), and `"` (quotation mark). For example, `12° 30′ 15″`, `12° 30′`,
         *   `30′`, and `12°30'15"` all parse. Formatting writes every part, like `12° 0′ 0″`.
         *
         * A number without a symbol does not parse. Parsing is lenient.
         */
        @JvmField
        @ObjCName(swiftName = "dms")
        public val Dms: RotationFormat = RotationFormat {
            compound(" ") {
                part(Degrees, aliases = listOf("º"))
                part(ArcMinutes, aliases = listOf("'"))
                part(ArcSeconds, aliases = listOf("\""))
            }
        }
    }

    /**
     * Configures a [RotationFormat]. Use it through [RotationFormat.Companion.invoke].
     *
     * A unit, symbol, or alias may appear in several entries, so an input can fall back to another
     * way of writing the same unit. Parsing uses the first entry that matches, and formatting the
     * first entry that contains the unit.
     */
    @UnitFormatDsl
    public class Builder internal constructor(base: RotationFormat?) {
        private val builder = QuantityFormatBuilder(RotationKind, base?.spec)

        /** The unit for a number without a symbol, or `null` to reject such input. */
        public var defaultUnit: RotationUnit?
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
         * Adds an entry for a number followed by one [unit], such as `45°`. The number and symbol
         * are spaced as in [UnitOfMeasure.format].
         *
         * @param symbol The symbol to write and parse.
         * @param aliases Other symbols accepted when parsing in lenient mode. Formatting always
         *   writes [symbol].
         */
        @JvmOverloads
        public fun unit(
            unit: RotationUnit,
            symbol: String = unit.symbol,
            aliases: List<String> = emptyList(),
        ) {
            builder.unit(unit, symbol, aliases)
        }

        /**
         * Adds an entry for a compound of several units, such as degrees, arcminutes, and
         * arcseconds in `12° 30′ 15″`.
         *
         * When parsing, each part is optional but at least one must be present, and only the last
         * present part may have a fractional value. When formatting, every part but the last is a
         * whole number, rounding the last part carries into larger parts, and a negative value gets
         * one leading `-`. With the default precision, the last part is rounded to 12 significant
         * digits of the whole value. A nonfinite value is written with only the first part's
         * symbol.
         *
         * @param separator The text written between parts.
         * @param omitTrailingZeroParts Whether formatting leaves out parts after the first that are
         *   zero once rounded and have no nonzero part after them, such as writing `12°` instead of
         *   `12° 0′ 0″`.
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

        internal fun build(): RotationFormat = RotationFormat(builder.build())
    }

    /** Declares the parts of a compound entry. Use it through [Builder.compound]. */
    @UnitFormatDsl
    public class CompoundBuilder internal constructor() {
        internal val parts = mutableListOf<FormatPart<RotationUnit>>()

        /**
         * Adds the next smaller part of the compound.
         *
         * @param symbol The symbol to write and parse.
         * @param aliases Other symbols accepted when parsing in lenient mode. Formatting always
         *   writes [symbol].
         */
        @JvmOverloads
        public fun part(
            unit: RotationUnit,
            symbol: String = unit.symbol,
            aliases: List<String> = emptyList(),
        ) {
            parts += FormatPart(unit, symbol, aliases.toList())
        }
    }
}

private object RotationKind : QuantityKind<Rotation, RotationUnit> {
    override fun toBase(value: Rotation): Double = value.toDouble(Degrees)

    override fun fromBase(value: Double): Rotation = Rotation.of(value, Degrees)

    override fun basePerUnit(unit: RotationUnit): Double = unit.degreesPerUnit
}
