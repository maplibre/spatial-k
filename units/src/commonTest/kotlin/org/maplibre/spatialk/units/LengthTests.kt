package org.maplibre.spatialk.units

import kotlin.test.Test
import org.maplibre.spatialk.testutil.assertDoubleEquals
import org.maplibre.spatialk.units.extensions.*

class LengthTests {
    private companion object {
        const val EARTH_RADIUS = 6371008.8 // meters
    }

    @Test
    fun testRadiansToLength() {
        assertDoubleEquals(1.0, 1.0.earthRadians.inEarthRadians)
        assertDoubleEquals(EARTH_RADIUS / 1000, 1.0.earthRadians.inKilometers)
        assertDoubleEquals(EARTH_RADIUS / 1609.344, 1.0.earthRadians.inMiles)
    }

    @Test
    fun testLengthToRadians() {
        assertDoubleEquals(1.0, 1.0.earthRadians.inEarthRadians)
        assertDoubleEquals(1.0, (EARTH_RADIUS / 1000).kilometers.inEarthRadians)
        assertDoubleEquals(1.0, (EARTH_RADIUS / 1609.344).miles.inEarthRadians)
    }

    @Test
    fun testLengthToDegrees() {
        assertDoubleEquals(57.2958, 1.0.earthRadians.inEarthDegrees)
        assertDoubleEquals(0.8993, 100.0.kilometers.inEarthDegrees)
        assertDoubleEquals(0.1447, 10.0.miles.inEarthDegrees)
    }

    @Test
    fun testConvertLength() {
        assertDoubleEquals(1.0, 1000.0.meters.inKilometers)
        assertDoubleEquals(0.6214, 1.0.kilometers.inMiles)
        assertDoubleEquals(1.6093, 1.0.miles.inKilometers)
        assertDoubleEquals(1.852, 1.0.nauticalMiles.inKilometers)
        assertDoubleEquals(100.0, 1.0.meters.inCentimeters)
    }
}
