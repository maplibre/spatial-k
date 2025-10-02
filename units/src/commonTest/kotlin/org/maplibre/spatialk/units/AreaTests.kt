package org.maplibre.spatialk.units

import kotlin.test.Test
import org.maplibre.spatialk.testutil.assertDoubleEquals
import org.maplibre.spatialk.units.extensions.*

class AreaTests {
    @Test
    fun testConvertArea() {
        assertDoubleEquals(0.386102, 1.0.squareKilometers.inSquareMiles)
        assertDoubleEquals(2.58999, 1.0.squareMiles.inSquareKilometers)
        assertDoubleEquals(10000.0, 1.0.squareMeters.inSquareCentimeters)
        assertDoubleEquals(0.0247105, 100.0.squareMeters.inAcres)
        assertDoubleEquals(119.599, 100.0.squareMeters.inSquareYards)
        assertDoubleEquals(1076.391, 100.0.squareMeters.inSquareFeet)
        assertDoubleEquals(0.0092903, 100000.0.squareFeet.inSquareKilometers)
    }
}
