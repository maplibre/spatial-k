package org.maplibre.spatialk.turf.measurement

import kotlin.test.Test
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.testutil.assertDoubleEquals
import org.maplibre.spatialk.units.extensions.inKilometers

class DistanceTest {

    @Test
    fun testDistance() {
        val a = Position(-73.67, 45.48)
        val b = Position(-79.48, 43.68)

        assertDoubleEquals(501.64563403765925, distance(a, b).inKilometers)
    }
}
