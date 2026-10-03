package org.maplibre.spatialk.units

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.maplibre.spatialk.testutil.assertDoubleEquals
import org.maplibre.spatialk.units.extensions.*

class LengthTests {

    @Test
    fun testRadiansToLength() {
        assertDoubleEquals(1.0, 1.earthRadians.inEarthRadians)
        assertDoubleEquals(Earth.averageRadius.inMeters / 1000, 1.earthRadians.inKilometers)
        assertDoubleEquals(Earth.averageRadius.inMeters / 1609.344, 1.earthRadians.inMiles)
    }

    @Test
    fun testLengthToRadians() {
        assertDoubleEquals(1.0, 1.earthRadians.inEarthRadians)
        assertDoubleEquals(1.0, (Earth.averageRadius.inMeters / 1000).kilometers.inEarthRadians)
        assertDoubleEquals(1.0, (Earth.averageRadius.inMeters / 1609.344).miles.inEarthRadians)
    }

    @Test
    fun testLengthToDegrees() {
        assertDoubleEquals(57.2958, 1.earthRadians.inEarthDegrees)
        assertDoubleEquals(0.8993, 100.kilometers.inEarthDegrees)
        assertDoubleEquals(0.1447, 10.miles.inEarthDegrees)
    }

    @Test
    fun testConvertLength() {
        assertDoubleEquals(1.0, 1000.meters.inKilometers)
        assertDoubleEquals(0.6214, 1.kilometers.inMiles)
        assertDoubleEquals(1.6093, 1.miles.inKilometers)
        assertDoubleEquals(1.852, 1.nauticalMiles.inKilometers)
        assertDoubleEquals(100.0, 1.meters.inCentimeters)
    }

    @Test
    fun testToString() {
        for ((value, precision) in listOf(1e7 to 2, 1e308 to 2, 1e-7 to 7)) {
            assertDoubleEquals(
                value,
                value.meters.toString(decimalPlaces = precision).removeSuffix(" m").toDouble(),
                epsilon = 0.0,
            )
        }
        for (value in listOf(1e7, 1e308, 1e-7, Double.MIN_VALUE)) {
            assertDoubleEquals(
                value,
                Units.Meters.format(value).removeSuffix(" m").toDouble(),
                epsilon = 0.0,
            )
        }
        assertEquals("Infinity m", Length.PositiveInfinity.toString())
        assertFailsWith<IllegalArgumentException> { 1.meters.toString(decimalPlaces = -1) }
    }

    @Test
    fun testSumLength() {
        val lengths = listOf(2.meters, 3.kilometers)
        assertEquals(3002.meters, lengths.sum())
        assertEquals(3002.meters, lengths.sumOf { it })
    }
}
