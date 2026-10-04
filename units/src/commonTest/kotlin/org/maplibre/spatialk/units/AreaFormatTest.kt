package org.maplibre.spatialk.units

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.maplibre.spatialk.testutil.assertDoubleEquals
import org.maplibre.spatialk.units.Units.Hectares
import org.maplibre.spatialk.units.Units.SquareMeters
import org.maplibre.spatialk.units.catalog.Thailand.Ngan
import org.maplibre.spatialk.units.catalog.Thailand.Rai
import org.maplibre.spatialk.units.catalog.Thailand.SquareWa
import org.maplibre.spatialk.units.extensions.*

class AreaFormatTest {
    private val format = AreaFormat {
        unit(Hectares)
        unit(SquareMeters, aliases = listOf("m2"))
        compound(" ", omitTrailingZeroParts = true) {
            part(Rai)
            part(Ngan)
            part(SquareWa)
        }
    }

    @Test
    fun parsesSimpleUnits() {
        assertDoubleEquals(2.5, format.parse("2.5 ha").inHectares)
        assertDoubleEquals(40.0, format.parse("40 m2").inSquareMeters)
        assertNull(format.parseOrNull("40"))
    }

    @Test
    fun parsesCompound() {
        assertDoubleEquals(3_000.0, format.parse("1 ไร่ 3 งาน 50 ตร.วา").inSquareMeters)
        assertDoubleEquals(1_600.0, format.parse("1 ไร่").inSquareMeters)
        assertDoubleEquals(202.0, format.parse("50.5 ตร.วา").inSquareMeters)
    }

    @Test
    fun formatsCompound() {
        assertEquals("1 ไร่ 3 งาน 50 ตร.วา", format.format(3_000.squareMeters, Rai, 0))
        assertEquals("2 ไร่", format.format(3_200.squareMeters, Rai, 0))
        assertEquals("0.30 ha", format.format(3_000.squareMeters, Hectares, 2))
    }
}
