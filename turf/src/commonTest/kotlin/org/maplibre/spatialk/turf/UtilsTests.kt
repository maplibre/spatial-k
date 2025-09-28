package org.maplibre.spatialk.turf

import kotlin.test.Test
import kotlin.test.assertEquals

@ExperimentalTurfApi
class UtilsTests {
    @Test
    fun testBearingToAzimuth() {
        assertEquals(40.0, bearingToAzimuth(40.0))
        assertEquals(255.0, bearingToAzimuth(-105.0))
        assertEquals(50.0, bearingToAzimuth(410.0))
        assertEquals(160.0, bearingToAzimuth(-200.0))
        assertEquals(325.0, bearingToAzimuth(-395.0))
    }
}
