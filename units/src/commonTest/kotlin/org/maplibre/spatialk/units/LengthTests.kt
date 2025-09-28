package org.maplibre.spatialk.units

import kotlin.test.Test
import org.maplibre.spatialk.testutil.assertDoubleEquals
import org.maplibre.spatialk.units.LengthUnit.Geodesy.*
import org.maplibre.spatialk.units.LengthUnit.Imperial.NauticalMiles
import org.maplibre.spatialk.units.LengthUnit.International.Centimeters

class LengthTests {
    private companion object {
        const val EARTH_RADIUS = 6371008.8 // meters
    }

    @Test
    fun testRadiansToLength() {
        assertDoubleEquals(1.0, 1.toLength(Radians).toDouble(Radians))
        assertDoubleEquals(EARTH_RADIUS / 1000, 1.toLength(Radians).inKilometers)
        assertDoubleEquals(EARTH_RADIUS / 1609.344, 1.toLength(Radians).inMiles)
    }

    @Test
    fun testLengthToRadians() {
        assertDoubleEquals(1.0, 1.toLength(Radians).toDouble(Radians))
        assertDoubleEquals(1.0, (EARTH_RADIUS / 1000).kilometers.toDouble(Radians))
        assertDoubleEquals(1.0, (EARTH_RADIUS / 1609.344).miles.toDouble(Radians))
    }

    @Test
    fun testLengthToDegrees() {
        assertDoubleEquals(57.2958, 1.toLength(Radians).toDouble(Degrees))
        assertDoubleEquals(0.8993, 100.kilometers.toDouble(Degrees))
        assertDoubleEquals(0.1447, 10.miles.toDouble(Degrees))
    }

    @Test
    fun testConvertLength() {
        assertDoubleEquals(1.0, 1000.meters.inKilometers)
        assertDoubleEquals(0.6214, 1.kilometers.inMiles)
        assertDoubleEquals(1.6093, 1.miles.inKilometers)
        assertDoubleEquals(1.852, 1.toLength(NauticalMiles).inKilometers)
        assertDoubleEquals(100.0, 1.meters.toDouble(Centimeters))
    }
}
