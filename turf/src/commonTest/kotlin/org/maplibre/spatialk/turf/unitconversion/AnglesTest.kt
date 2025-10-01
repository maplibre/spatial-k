package org.maplibre.spatialk.turf.unitconversion

import kotlin.test.Test
import kotlin.test.assertEquals

class AnglesTest {
    @Test
    fun testBearingToAzimuth() {
        assertEquals(40.0, bearingToAzimuth(40.0))
        assertEquals(255.0, bearingToAzimuth(-105.0))
        assertEquals(50.0, bearingToAzimuth(410.0))
        assertEquals(160.0, bearingToAzimuth(-200.0))
        assertEquals(325.0, bearingToAzimuth(-395.0))
    }

    @Test
    fun testAzimuthToBearing() {
        assertEquals(40.0, azimuthToBearing(40.0))
        assertEquals(-105.0, azimuthToBearing(255.0))
        assertEquals(50.0, azimuthToBearing(410.0))
        assertEquals(160.0, azimuthToBearing(-200.0))
        assertEquals(-35.0, azimuthToBearing(-395.0))
    }
}
