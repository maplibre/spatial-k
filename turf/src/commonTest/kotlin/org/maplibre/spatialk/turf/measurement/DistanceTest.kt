package org.maplibre.spatialk.turf.measurement

import kotlin.test.Test
import kotlin.test.assertEquals
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.units.inKilometers

class DistanceTest {

    @Test
    fun testDistance() {
        val a = Position(-73.67, 45.48)
        val b = Position(-79.48, 43.68)

        assertEquals(501.64563403765925, distance(a, b).inKilometers)
    }
}
