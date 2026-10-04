package org.maplibre.spatialk.units

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import org.maplibre.spatialk.testutil.assertLengthEquals
import org.maplibre.spatialk.units.Units.Centimeters
import org.maplibre.spatialk.units.Units.Feet
import org.maplibre.spatialk.units.Units.Inches
import org.maplibre.spatialk.units.Units.Kilometers
import org.maplibre.spatialk.units.Units.Meters
import org.maplibre.spatialk.units.Units.Millimeters
import org.maplibre.spatialk.units.extensions.*

class LengthFormatTest {
    private val osm = LengthFormat.Osm
    private val strictOsm = LengthFormat(osm) { strict = true }

    @Test
    fun osmParsesSimpleUnits() {
        assertLengthEquals(3.5.meters, osm.parse("3.5"))
        assertLengthEquals(3.meters, osm.parse("3 m"))
        assertLengthEquals(3.meters, osm.parse("3m"))
        assertLengthEquals(3.meters, osm.parse(" 3  metres\t"))
        assertLengthEquals((-2).meters, osm.parse("-2 m"))
        assertLengthEquals(1.5.kilometers, osm.parse("1.5 km"))
        assertLengthEquals(2.miles, osm.parse("2 miles"))
        assertLengthEquals(1.nauticalMiles, osm.parse("1 nmi"))
        assertLengthEquals(3.yards, osm.parse("3 yds"))
    }

    @Test
    fun osmParsesFeetAndInches() {
        val expected = 12.feet + 5.inches
        for (input in listOf("12'5\"", "12' 5\"", "12 ft 5 in", "12ft5in")) {
            assertLengthEquals(expected, osm.parse(input), message = input)
        }
        assertLengthEquals(12.feet, osm.parse("12 ft"))
        assertLengthEquals(12.5.feet, osm.parse("12.5'"))
        assertLengthEquals(5.5.inches, osm.parse("5.5\""))
        assertLengthEquals(-expected, osm.parse("-12'5\""))
    }

    @Test
    fun osmRejectsOtherValues() {
        val inputs =
            listOf(
                "",
                "none",
                "default",
                "RU:urban",
                "3;4",
                "3,5 m",
                "1e3 m",
                ".5 m",
                "5. m",
                "+3 m",
                "- 3 m",
                "3 M",
                "3 m m",
                "12.5'3\"",
                "5\"12'",
                "12'5",
            )
        for (input in inputs) assertNull(osm.parseOrNull(input), input)
        assertFailsWith<IllegalArgumentException> { osm.parse("none") }
    }

    @Test
    fun strictAcceptsOnlyCanonicalText() {
        assertLengthEquals(3.meters, strictOsm.parse("3 m"))
        assertLengthEquals(3.meters, strictOsm.parse("3"))
        assertLengthEquals(12.feet + 5.inches, strictOsm.parse("12'5\""))
        assertLengthEquals(5.inches, strictOsm.parse("5\""))
        for (input in listOf("3m", "3  m", " 3 m", "3 metres", "12 ft", "12' 5\"", "12 '")) {
            assertNull(strictOsm.parseOrNull(input), input)
        }
    }

    @Test
    fun entriesForTheSameUnitAreParseFallbacks() {
        val compoundFirst = LengthFormat {
            compound {
                part(Feet, "'")
                part(Inches, "\"")
            }
            unit(Feet)
            strict = true
        }
        assertLengthEquals(12.feet, compoundFirst.parse("12'"))
        assertLengthEquals(12.feet, compoundFirst.parse("12 ft"))
        assertEquals("12'0\"", compoundFirst.format(12.feet, Feet, 0))

        val strictWithFeet =
            LengthFormat(osm) {
                strict = true
                unit(Feet)
            }
        assertLengthEquals(12.feet, strictWithFeet.parse("12 ft"))
        assertEquals("12'", strictWithFeet.format(12.feet, Feet))

        val simpleFirst = LengthFormat {
            unit(Feet)
            compound {
                part(Feet, "'")
                part(Inches, "\"")
            }
        }
        assertEquals("12 ft", simpleFirst.format(12.feet, Feet, 0))
        assertEquals("0'5\"", simpleFirst.format(5.inches, Inches, 0))
    }

    @Test
    fun defaultUnit() {
        val gauge = LengthFormat(osm) { defaultUnit = Millimeters }
        assertLengthEquals(1435.millimeters, gauge.parse("1435"))
        assertLengthEquals(3.meters, gauge.parse("3 m"))

        val noDefault = LengthFormat(osm) { defaultUnit = null }
        assertNull(noDefault.parseOrNull("3"))
        assertNull(LengthFormat(strictOsm) {}.parseOrNull("3m"))
    }

    @Test
    fun formatUsesFirstEntryForUnit() {
        assertEquals("3.50 m", osm.format(3.5.meters, Meters, 2))
        assertEquals("1.5 km", osm.format(1.5.kilometers, Kilometers))
        assertEquals("12'5\"", osm.format(12.feet + 5.inches, Feet, 0))
        assertEquals("1'0.5\"", osm.format(12.5.inches, Inches, 1))
        assertEquals("-12'5.0\"", osm.format(-(12.feet + 5.inches), Inches, 1))
        assertEquals("-0'0.5\"", osm.format((-0.5).inches, Feet, 1))
        assertEquals("Infinity'", osm.format(Length.PositiveInfinity, Feet))
        assertFailsWith<IllegalArgumentException> { osm.format(3.meters, Centimeters) }
        assertFailsWith<IllegalArgumentException> { osm.format(3.meters, Meters, -1) }
    }

    @Test
    fun formatCarriesRoundingIntoLargerParts() {
        assertEquals("12'", osm.format(11.feet + 11.99.inches, Feet, 1))
        assertEquals("12'", osm.format(11.feet + 11.5.inches, Feet, 0))
        val yardsFeetInches = LengthFormat {
            compound(" ") {
                part(Units.Yards)
                part(Feet)
                part(Inches)
            }
        }
        assertEquals(
            "2 yd 0 ft 0 in",
            yardsFeetInches.format(1.yards + 2.feet + 11.6.inches, Feet, 0),
        )
    }

    @Test
    fun formatOmitsTrailingZeroParts() {
        assertEquals("12'", osm.format(12.feet, Feet))
        assertEquals("12'", osm.format(12.feet, Feet, 2))
        assertEquals("0'5\"", osm.format(5.inches, Feet, 0))
        assertEquals("0'", osm.format(Length.Zero, Feet))
        val yardsFeetInches = LengthFormat {
            compound(" ", omitTrailingZeroParts = true) {
                part(Units.Yards)
                part(Feet)
                part(Inches)
            }
        }
        assertEquals("1 yd 0 ft 5 in", yardsFeetInches.format(1.yards + 5.inches, Feet, 0))
        assertEquals("1 yd 2 ft", yardsFeetInches.format(1.yards + 2.feet, Feet))
    }

    @Test
    fun compoundFormatAtFullPrecisionHidesFloatingPointError() {
        assertEquals("12'4.5\"", osm.format(osm.parse("12'4.5\""), Feet))
        assertEquals("3'0.5\"", osm.format(1.yards + 0.5.inches, Feet))
        assertEquals("6'11.5\"", osm.format(7.feet - 0.5.inches, Feet))
        for (inches in 0..480) {
            val value = inches.inches
            assertLengthEquals(
                value,
                strictOsm.parseOrNull(osm.format(value, Feet)),
                message = "$value",
            )
        }
    }

    @Test
    fun builderValidation() {
        assertFailsWith<IllegalArgumentException> { LengthFormat { unit(Meters, " ") } }
        assertFailsWith<IllegalArgumentException> { LengthFormat { compound { part(Feet) } } }
        assertFailsWith<IllegalArgumentException> {
            LengthFormat {
                compound {
                    part(Inches)
                    part(Feet)
                }
            }
        }
        assertFailsWith<IllegalArgumentException> {
            LengthFormat {
                compound {
                    part(Feet)
                    part(Feet, "'")
                }
            }
        }
        LengthFormat {
            unit(Meters)
            unit(Meters, "metres", aliases = listOf("m"))
        }
    }
}
