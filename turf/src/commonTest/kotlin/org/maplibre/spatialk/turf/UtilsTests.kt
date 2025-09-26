package org.maplibre.spatialk.turf

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@ExperimentalTurfApi
class UtilsTests {

    @Test
    fun testRadiansToLength() {
        assertEquals(1.0, radiansToLength(1.0, Units.Radians))
        assertEquals(EARTH_RADIUS / 1000, radiansToLength(1.0, Units.Kilometers))
        assertEquals(EARTH_RADIUS / 1609.344, radiansToLength(1.0, Units.Miles))

        assertFailsWith<IllegalArgumentException> { radiansToLength(1.0, Units.Acres) }
    }

    @Test
    fun testLengthToRadians() {
        assertEquals(1.0, lengthToRadians(1.0, Units.Radians))
        assertEquals(1.0, lengthToRadians(EARTH_RADIUS / 1000, Units.Kilometers))
        assertEquals(1.0, lengthToRadians(EARTH_RADIUS / 1609.344, Units.Miles))

        assertFailsWith<IllegalArgumentException> { lengthToRadians(1.0, Units.Acres) }
    }

    @Test
    fun testLengthToDegrees() {
        assertEquals(57.29577951308232, lengthToDegrees(1.0, Units.Radians))
        assertEquals(0.899320363724538, lengthToDegrees(100.0, Units.Kilometers))
        assertEquals(0.1447315831437903, lengthToDegrees(10.0, Units.Miles))
    }

    @Test
    fun testConvertLength() {
        assertEquals(1.0, convertLength(1000.0, from = Units.Meters))
        assertEquals(
            0.621371192237334,
            convertLength(1.0, from = Units.Kilometers, to = Units.Miles),
        )
        assertEquals(1.609344, convertLength(1.0, from = Units.Miles, to = Units.Kilometers))
        assertEquals(1.852, convertLength(1.0, from = Units.NauticalMiles))
        assertEquals(
            100.00000000000001,
            convertLength(1.0, from = Units.Meters, to = Units.Centimeters),
        )

        assertFailsWith<IllegalArgumentException> { convertLength(-1.0) }
    }

    @Test
    fun testConvertArea() {
        assertEquals(0.001, convertArea(1000.0))
        assertEquals(0.386, convertArea(1.0, from = Units.Kilometers, to = Units.Miles))
        assertEquals(
            2.5906735751295336,
            convertArea(1.0, from = Units.Miles, to = Units.Kilometers),
        )
        assertEquals(10000.0, convertArea(1.0, from = Units.Meters, to = Units.Centimeters))
        assertEquals(0.0247105, convertArea(100.0, Units.Meters, Units.Acres))
        assertEquals(119.59900459999999, convertArea(100.0, to = Units.Yards))
        assertEquals(1076.3910417, convertArea(100.0, Units.Meters, Units.Feet))
        assertEquals(0.009290303999749462, convertArea(100000.0, from = Units.Feet))

        assertFailsWith<IllegalArgumentException> { convertArea(-1.0) }
        assertFailsWith<IllegalArgumentException> { convertArea(1.0, from = Units.Degrees) }
        assertFailsWith<IllegalArgumentException> { convertArea(1.0, to = Units.Radians) }
    }

    @Test
    fun testBearingToAzimuth() {
        assertEquals(40.0, bearingToAzimuth(40.0))
        assertEquals(255.0, bearingToAzimuth(-105.0))
        assertEquals(50.0, bearingToAzimuth(410.0))
        assertEquals(160.0, bearingToAzimuth(-200.0))
        assertEquals(325.0, bearingToAzimuth(-395.0))
    }
}
