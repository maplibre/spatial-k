package org.maplibre.spatialk.units

import kotlin.test.Test
import kotlin.test.assertEquals
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
    fun testSumLength() {
        val lengths = listOf(2.meters, 3.kilometers)
        assertEquals(3002.meters, lengths.sum())
        assertEquals(3002.meters, lengths.sumOf { it })
    }
}
