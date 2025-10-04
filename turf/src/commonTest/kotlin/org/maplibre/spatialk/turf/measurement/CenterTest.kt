package org.maplibre.spatialk.turf.measurement

import kotlin.test.Test
import org.maplibre.spatialk.geojson.Polygon
import org.maplibre.spatialk.testutil.assertDoubleEquals
import org.maplibre.spatialk.testutil.readResourceFile

class CenterTest {

    @Test
    fun testCenterFromGeometry() {
        val geometry = Polygon.fromJson(readResourceFile("measurement/area/other.json"))

        val centerPoint = geometry.computeBbox().center()

        assertDoubleEquals(-75.71805238723755, centerPoint.longitude, 0.0001)
        assertDoubleEquals(45.3811030151199, centerPoint.latitude, 0.0001)
    }
}
