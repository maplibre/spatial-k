package org.maplibre.spatialk.turf.measurement

import kotlin.test.Test
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.testutil.assertDoubleEquals
import org.maplibre.spatialk.units.inKilometers

class PointToLineDistanceTest {

    @Test
    fun testPointToLineDistance() {
        val point = Position(-0.54931640625, 0.7470491450051796)
        val line =
            LineString(
                Position(1.0, 3.0),
                Position(2.0, 2.0),
                Position(2.0, 0.0),
                Position(-1.5, -1.5),
            )

        val distance = pointToLineDistance(point, line)
        assertDoubleEquals(188.01568693725255, distance.inKilometers, 0.000001)
    }
}
