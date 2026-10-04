package org.maplibre.spatialk.units

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.maplibre.spatialk.testutil.assertRotationEquals
import org.maplibre.spatialk.units.Units.ArcSeconds
import org.maplibre.spatialk.units.Units.Degrees
import org.maplibre.spatialk.units.extensions.*

class RotationFormatTest {
    private val osm = RotationFormat.Osm
    private val dms = RotationFormat.Dms
    private val strictDms = RotationFormat(dms) { strict = true }

    @Test
    fun osmParsesDegrees() {
        assertRotationEquals(45.degrees, osm.parse("45°"))
        assertRotationEquals(45.degrees, osm.parse(" 45 ° "))
        assertRotationEquals(45.degrees, osm.parse("45º"))
        assertRotationEquals((-12.5).degrees, osm.parse("-12.5°"))
        for (input in listOf("45", "45 deg", "10 %", "100 ‰", "N", "45°;90°")) {
            assertNull(osm.parseOrNull(input), input)
        }
        val direction = RotationFormat(osm) { defaultUnit = Degrees }
        assertRotationEquals(45.degrees, direction.parse("45"))
    }

    @Test
    fun osmFormatsDegrees() {
        assertEquals("5.71°", osm.format(5.71.degrees, Degrees, 2))
    }

    @Test
    fun parsesDms() {
        val expected = 12.degrees + 30.arcMinutes + 15.arcSeconds
        assertRotationEquals(expected, strictDms.parse("12° 30′ 15″"))
        assertRotationEquals(12.degrees + 15.5.arcSeconds, strictDms.parse("12° 15.5″"))
        assertRotationEquals(30.arcMinutes, strictDms.parse("30′"))
        assertRotationEquals(-(12.degrees + 30.arcMinutes), strictDms.parse("-12° 30′"))
        for (input in
            listOf("12°30′15″", "12° 30′  15″", "12° 30' 15\"", "12.5° 30′", "-12° -30′")) {
            assertNull(strictDms.parseOrNull(input), input)
        }
        for (input in listOf("12°30′15″", " 12 °  30 ′ 15 ″ ", "12° 30' 15\"", "12º30'15\"")) {
            assertRotationEquals(expected, dms.parse(input), message = input)
        }
        assertNull(dms.parseOrNull("12"))
    }

    @Test
    fun formatsDms() {
        assertEquals("12° 0′ 0″", dms.format(12.degrees, ArcSeconds, 0))
        assertEquals(
            "11° 0′ 0.00″",
            dms.format(10.degrees + 59.arcMinutes + 59.999.arcSeconds, Degrees, 2),
        )
    }

    @Test
    fun compoundFormatAtFullPrecisionHidesFloatingPointError() {
        assertEquals("1° 2′ 3.5″", dms.format(dms.parse("1° 2′ 3.5″"), Degrees))
        for (minutes in 0..360 * 60) {
            val value = minutes.arcMinutes
            assertRotationEquals(
                value,
                strictDms.parseOrNull(dms.format(value, Degrees)),
                message = "$value",
            )
        }
    }
}
