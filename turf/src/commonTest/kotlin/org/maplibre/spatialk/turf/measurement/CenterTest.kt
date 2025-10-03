package org.maplibre.spatialk.turf.measurement

import kotlin.test.Test
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.Polygon
import org.maplibre.spatialk.testutil.assertDoubleEquals
import org.maplibre.spatialk.testutil.readResourceFile

class CenterTest {

    @Test
    fun testCenterFromFeature() {
        val geometry = Polygon.fromJson(readResourceFile("measurement/area/other.json"))

        val centerPoint = Feature(geometry).center()!!

        assertDoubleEquals(-75.71805238723755, centerPoint.coordinates.longitude, 0.0001)
        assertDoubleEquals(45.3811030151199, centerPoint.coordinates.latitude, 0.0001)
    }

    @Test
    fun testCenterFromGeometry() {
        val geometry = Polygon.fromJson(readResourceFile("measurement/area/other.json"))

        val centerPoint = geometry.center()

        assertDoubleEquals(-75.71805238723755, centerPoint.coordinates.longitude, 0.0001)
        assertDoubleEquals(45.3811030151199, centerPoint.coordinates.latitude, 0.0001)
    }
}
