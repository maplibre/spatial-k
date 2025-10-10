package org.maplibre.spatialk.turf.measurement

import kotlin.test.Test
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.testutil.assertPositionEquals

class MidpointTest {

    @Test
    fun testMidpoint() {
        val point1 = Position(-79.3801, 43.6463)
        val point2 = Position(-74.0071, 40.7113)

        val midpoint = midpoint(point1, point2)
        assertPositionEquals(Position(-76.6311, 42.2101), midpoint)
    }
}
