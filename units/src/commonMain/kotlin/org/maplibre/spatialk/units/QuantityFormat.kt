package org.maplibre.spatialk.units

import kotlin.math.absoluteValue
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.roundToLong
import org.maplibre.spatialk.units.extensions.toRoundedString

@DslMarker internal annotation class UnitFormatDsl

/** A unit with the symbol written for it and the aliases also accepted when parsing. */
internal class FormatPart<U : UnitOfMeasure>(
    val unit: U,
    val symbol: String,
    val aliases: List<String>,
)

/** One way to write a quantity: either a single unit, or several units in descending size. */
internal sealed class FormatEntry<U : UnitOfMeasure> {
    abstract val parts: List<FormatPart<U>>

    class Simple<U : UnitOfMeasure>(val part: FormatPart<U>) : FormatEntry<U>() {
        override val parts: List<FormatPart<U>> = listOf(part)
    }

    class Compound<U : UnitOfMeasure>(
        val separator: String,
        val omitTrailingZeroParts: Boolean,
        override val parts: List<FormatPart<U>>,
    ) : FormatEntry<U>()
}

/**
 * Unit-agnostic parsing and formatting shared by the public quantity formats. Values are measured
 * in base units, and [basePerUnit] converts one of a unit into base units.
 */
internal class QuantityFormat<U>(
    val entries: List<FormatEntry<U>>,
    val defaultUnit: U?,
    val strict: Boolean,
    private val basePerUnit: (U) -> Double,
) where U : UnitOfMeasure, U : Comparable<U> {

    init {
        for (entry in entries) {
            if (entry is FormatEntry.Compound) {
                require(entry.parts.size >= 2) { "A compound must have at least 2 parts." }
                entry.parts.zipWithNext { larger, smaller ->
                    require(larger.unit > smaller.unit) {
                        "Compound parts must be in strictly descending unit size, but " +
                            "'${smaller.symbol}' is not smaller than '${larger.symbol}'."
                    }
                }
            }
            for (part in entry.parts) {
                for (symbol in listOf(part.symbol) + part.aliases) {
                    require(symbol.isNotBlank() && symbol.trim() == symbol) {
                        "Symbols and aliases must be non-blank without surrounding whitespace, " +
                            "but got '$symbol'."
                    }
                }
            }
        }
    }

    fun parseOrNull(input: String): Double? {
        val text = if (strict) input else input.trim()
        val negative = text.startsWith('-')
        val start = if (negative) 1 else 0
        val magnitude =
            entries.firstNotNullOfOrNull { entry ->
                when (entry) {
                    is FormatEntry.Simple -> parseSimple(text, start, entry.part)
                    is FormatEntry.Compound -> parseParts(text, start, entry, 0, 0.0)
                }
            } ?: parseBareNumber(text, start) ?: return null
        return if (negative) -magnitude else magnitude
    }

    fun format(value: Double, unit: U, decimalPlaces: Int): String {
        val entry =
            requireNotNull(entries.firstOrNull { e -> e.parts.any { it.unit == unit } }) {
                "This format has no entry for unit '${unit.symbol}'."
            }
        return when (entry) {
            is FormatEntry.Simple ->
                formatWithSymbol(
                    (value / basePerUnit(unit)).toRoundedString(decimalPlaces),
                    entry.part.symbol,
                )
            is FormatEntry.Compound ->
                formatCompound(
                    value,
                    entry.parts.map { basePerUnit(it.unit) },
                    entry.parts.map { it.symbol },
                    entry.separator,
                    decimalPlaces,
                    entry.omitTrailingZeroParts,
                )
        }
    }

    private fun parseSimple(text: String, start: Int, part: FormatPart<U>): Double? {
        val number = readNumber(text, start) ?: return null
        if (symbolsOf(part).none { matchSymbol(text, number.end, it) == text.length }) return null
        return number.value * basePerUnit(part.unit)
    }

    private fun parseBareNumber(text: String, start: Int): Double? {
        val unit = defaultUnit ?: return null
        val number = readNumber(text, start) ?: return null
        return if (number.end == text.length) number.value * basePerUnit(unit) else null
    }

    /**
     * Matches the parts of [entry] from [firstPart] onward against [text] starting at [start],
     * backtracking over symbols that are prefixes of one another.
     */
    private fun parseParts(
        text: String,
        start: Int,
        entry: FormatEntry.Compound<U>,
        firstPart: Int,
        total: Double,
    ): Double? {
        val number = readNumber(text, start) ?: return null
        for (index in firstPart until entry.parts.size) {
            val part = entry.parts[index]
            val sum = total + number.value * basePerUnit(part.unit)
            for (symbol in symbolsOf(part)) {
                val end = matchSymbol(text, number.end, symbol) ?: continue
                if (end == text.length) return sum
                if (number.isFractional || index == entry.parts.lastIndex) continue
                val next = matchSeparator(text, end, entry.separator) ?: continue
                parseParts(text, next, entry, index + 1, sum)?.let {
                    return it
                }
            }
        }
        return null
    }

    private fun symbolsOf(part: FormatPart<U>): List<String> =
        if (strict) listOf(part.symbol) else listOf(part.symbol) + part.aliases

    /** Returns the index after [symbol] and the spacing before it, or `null` if absent. */
    private fun matchSymbol(text: String, start: Int, symbol: String): Int? {
        val symbolStart =
            if (strict) {
                val spacing = symbolSpacing(symbol)
                if (!text.startsWith(spacing, start)) return null
                start + spacing.length
            } else {
                skipSpace(text, start)
            }
        return if (text.startsWith(symbol, symbolStart)) symbolStart + symbol.length else null
    }

    private fun matchSeparator(text: String, start: Int, separator: String): Int? {
        if (strict) return if (text.startsWith(separator, start)) start + separator.length else null
        val trimmed = separator.trim()
        val afterSpace = skipSpace(text, start)
        if (!text.startsWith(trimmed, afterSpace)) return null
        return skipSpace(text, afterSpace + trimmed.length)
    }

    private fun skipSpace(text: String, start: Int): Int {
        var index = start
        while (index < text.length && text[index].isWhitespace()) index++
        return index
    }

    private class NumberToken(val value: Double, val end: Int, val isFractional: Boolean)

    /** Reads digits with an optional fractional part at [start]; no sign or exponent. */
    private fun readNumber(text: String, start: Int): NumberToken? {
        var index = start
        while (index < text.length && text[index] in '0'..'9') index++
        if (index == start) return null
        var isFractional = false
        if (index + 1 < text.length && text[index] == '.' && text[index + 1] in '0'..'9') {
            isFractional = true
            index += 2
            while (index < text.length && text[index] in '0'..'9') index++
        }
        return NumberToken(text.substring(start, index).toDouble(), index, isFractional)
    }
}

/** The spacing written between a number and [symbol]; see [UnitOfMeasure.format]. */
internal fun symbolSpacing(symbol: String): String =
    if (symbol.length == 1 && !symbol[0].isLetter()) "" else " "

/** Joins an already formatted [number] and [symbol] as described in [UnitOfMeasure.format]. */
internal fun formatWithSymbol(number: String, symbol: String): String =
    "$number${symbolSpacing(symbol)}$symbol"

/**
 * Formats [value], measured in base units, as a compound of parts in descending size. [basePerUnit]
 * holds the size of each part's unit in base units. Every part but the last is a whole number; the
 * last is rounded to [decimalPlaces], and rounding carries into larger parts. A negative value gets
 * one leading `-`. Nonfinite values are written with the first part's symbol only. With
 * [omitTrailingZeroParts], zero parts after the first are left out from the end.
 *
 * With [decimalPlaces] of [Int.MAX_VALUE], the last part is rounded to [SIGNIFICANT_DIGITS] of the
 * whole value, which hides the floating-point error left over from splitting it into parts.
 */
internal fun formatCompound(
    value: Double,
    basePerUnit: List<Double>,
    symbols: List<String>,
    separator: String,
    decimalPlaces: Int,
    omitTrailingZeroParts: Boolean = false,
): String {
    if (!value.isFinite()) {
        return formatWithSymbol(value.toRoundedString(decimalPlaces), symbols.first())
    }
    val last = basePerUnit.lastIndex
    val wholeParts = LongArray(last)
    var remainder = value.absoluteValue
    for (i in 0 until last) {
        wholeParts[i] = (remainder / basePerUnit[i]).toLong()
        remainder -= wholeParts[i] * basePerUnit[i]
    }
    // Subtracting whole parts can leave a remainder just below zero.
    var lastValue = (remainder / basePerUnit[last]).coerceAtLeast(0.0)
    if (decimalPlaces == Int.MAX_VALUE) {
        lastValue = lastValue.roundToSignificantDigitsOf(value.absoluteValue / basePerUnit[last])
    }
    var lastPart = lastValue.toRoundedString(decimalPlaces)

    // Carry parts that round up to a whole unit of the next larger part.
    if (lastPart.toDouble() >= partsPerLargerPart(basePerUnit, last)) {
        lastPart = 0.0.toRoundedString(decimalPlaces)
        wholeParts[last - 1]++
    }
    for (i in last - 1 downTo 1) {
        if (wholeParts[i] >= partsPerLargerPart(basePerUnit, i)) {
            wholeParts[i] = 0
            wholeParts[i - 1]++
        }
    }

    val sign = if (value < 0) "-" else ""
    val numbers = wholeParts.map { it.toString() } + lastPart
    var count = numbers.size
    if (omitTrailingZeroParts) {
        while (count > 1 && numbers[count - 1].toDouble() == 0.0) count--
    }
    return sign +
        (0 until count).joinToString(separator) { formatWithSymbol(numbers[it], symbols[it]) }
}

/** How many of part [index] make one of the part before it, snapped to a nearby integer. */
private fun partsPerLargerPart(basePerUnit: List<Double>, index: Int): Double {
    val ratio = basePerUnit[index - 1] / basePerUnit[index]
    val whole = ratio.roundToLong().toDouble()
    return if ((ratio - whole).absoluteValue <= ratio * RATIO_TOLERANCE) whole else ratio
}

private const val RATIO_TOLERANCE = 1e-9

/**
 * The significant digits of a compound's whole value written by default. A double carries about 15,
 * and splitting a value into parts loses a few of them.
 */
private const val SIGNIFICANT_DIGITS = 12

/** Rounds this number to the decimal place of the [SIGNIFICANT_DIGITS]th digit of [total]. */
private fun Double.roundToSignificantDigitsOf(total: Double): Double {
    if (total == 0.0) return this
    val decimals = (SIGNIFICANT_DIGITS - 1 - floor(log10(total)).toInt()).coerceIn(-300, 300)
    val scale = 10.0.pow(decimals)
    return round(this * scale) / scale
}

/** Collects entries for a [QuantityFormat] builder. */
internal class QuantityFormatBuilder<U>(base: QuantityFormat<U>?)
    where U : UnitOfMeasure, U : Comparable<U> {
    private val entries: MutableList<FormatEntry<U>> = base?.entries.orEmpty().toMutableList()
    var defaultUnit: U? = base?.defaultUnit
    var strict: Boolean = base?.strict ?: false

    fun unit(unit: U, symbol: String, aliases: List<String>) {
        entries += FormatEntry.Simple(FormatPart(unit, symbol, aliases.toList()))
    }

    fun compound(separator: String, omitTrailingZeroParts: Boolean, parts: List<FormatPart<U>>) {
        entries += FormatEntry.Compound(separator, omitTrailingZeroParts, parts.toList())
    }

    fun build(basePerUnit: (U) -> Double): QuantityFormat<U> =
        QuantityFormat(entries.toList(), defaultUnit, strict, basePerUnit)
}
