package org.maplibre.spatialk.turf.measurement

import kotlin.test.Test
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.testutil.assertDoubleEquals

class MidpointTest {

    @Test
    fun testMidpoint() {
        val point1 = Position(-79.3801, 43.6463)
        val point2 = Position(-74.0071, 40.7113)

        val midpoint = midpoint(point1, point2)

        assertDoubleEquals(-76.6311, midpoint.longitude, 0.0001)
        assertDoubleEquals(42.2101, midpoint.latitude, 0.0001)
    }
}
