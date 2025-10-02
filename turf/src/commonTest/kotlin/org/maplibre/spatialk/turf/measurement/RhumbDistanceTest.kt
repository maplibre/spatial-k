package org.maplibre.spatialk.turf.measurement

import kotlin.test.Test
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.testutil.assertDoubleEquals
import org.maplibre.spatialk.units.extensions.inKilometers

class RhumbDistanceTest {

    @Test
    fun testRhumbDistance() {
        val distance = rhumbDistance(Position(-75.343, 39.984), Position(-75.534, 39.123))

        assertDoubleEquals(97.12923942772163, distance.inKilometers, 0.000001)
    }
}
