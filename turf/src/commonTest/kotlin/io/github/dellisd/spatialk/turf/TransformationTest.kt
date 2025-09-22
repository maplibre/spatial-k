package io.github.dellisd.spatialk.turf

import io.github.dellisd.spatialk.geojson.Feature
import io.github.dellisd.spatialk.geojson.LineString
import io.github.dellisd.spatialk.turf.utils.readResource
import kotlin.test.Test
import kotlin.test.assertEquals

@ExperimentalTurfApi
class TransformationTest {

    @Test
    fun testBezierSplineIn() {
        val feature = Feature.fromJson<LineString>(readResource("transformation/bezierspline/in/bezierIn.json"))
        val expectedOut = Feature.fromJson<LineString>(readResource("transformation/bezierspline/out/bezierIn.json"))

        assertEquals(expectedOut.geometry, bezierSpline(feature.geometry!!))
    }

    @Test
    fun testBezierSplineSimple() {
        val feature = Feature.fromJson<LineString>(readResource("transformation/bezierspline/in/simple.json"))
        val expectedOut = Feature.fromJson<LineString>(readResource("transformation/bezierspline/out/simple.json"))

        assertEquals(expectedOut.geometry, bezierSpline(feature.geometry!!))
    }

    /**
     * This test is designed to draw a bezierSpline across the 180th Meridian
     *
     * @see <a href="https://github.com/Turfjs/turf/issues/1063">
     */
    @Test
    fun testBezierSplineAcrossPacific() {
        val feature = Feature.fromJson<LineString>(readResource("transformation/bezierspline/in/issue-#1063.json"))
        val expectedOut = Feature.fromJson<LineString>(readResource("transformation/bezierspline/out/issue-#1063.json"))

        assertEquals(expectedOut.geometry, bezierSpline(feature.geometry!!))
    }
}
