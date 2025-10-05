package org.maplibre.spatialk.turf.transformation

import kotlin.test.Test
import kotlin.test.assertEquals
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.testutil.readResourceFile

class BezierSplineTest {

    @Test
    fun testBezierSplineIn() {
        val feature =
            Feature.fromJson<LineString>(
                readResourceFile("transformation/bezierspline/in/bezierIn.json")
            )
        val expectedOut =
            Feature.fromJson<LineString>(
                readResourceFile("transformation/bezierspline/out/bezierIn.json")
            )

        assertEquals(expectedOut.geometry, feature.geometry.bezierSpline())
    }

    @Test
    fun testBezierSplineSimple() {
        val feature =
            Feature.fromJson<LineString>(
                readResourceFile("transformation/bezierspline/in/simple.json")
            )
        val expectedOut =
            Feature.fromJson<LineString>(
                readResourceFile("transformation/bezierspline/out/simple.json")
            )

        assertEquals(expectedOut.geometry, feature.geometry.bezierSpline())
    }

    /**
     * This test is designed to draw a bezierSpline across the 180th Meridian
     *
     * @see <a href="https://github.com/Turfjs/turf/issues/1063">
     */
    @Test
    fun testBezierSplineAcrossPacific() {
        val feature =
            Feature.fromJson<LineString>(
                readResourceFile("transformation/bezierspline/in/issue-#1063.json")
            )
        val expectedOut =
            Feature.fromJson<LineString>(
                readResourceFile("transformation/bezierspline/out/issue-#1063.json")
            )

        assertEquals(expectedOut.geometry, feature.geometry.bezierSpline())
    }
}
