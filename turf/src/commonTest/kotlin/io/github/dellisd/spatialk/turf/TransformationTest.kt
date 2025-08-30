package io.github.dellisd.spatialk.turf

import io.github.dellisd.spatialk.geojson.Feature
import io.github.dellisd.spatialk.geojson.LineString
import io.github.dellisd.spatialk.geojson.Position
import io.github.dellisd.spatialk.turf.utils.readResource
import kotlin.math.roundToInt
import kotlin.test.Test
import kotlin.test.assertEquals

@ExperimentalTurfApi
class TransformationTest {

    @Test
    fun testBezierSplineIn() {
        val feature = Feature.fromJson(readResource("transformation/bezierspline/in/bezierIn.json"))
        val expectedOut = Feature.fromJson(readResource("transformation/bezierspline/out/bezierIn.json"))

        assertEquals(expectedOut.geometry, bezierSpline(feature.geometry as LineString))
    }

    @Test
    fun testBezierSplineSimple() {
        val feature = Feature.fromJson(readResource("transformation/bezierspline/in/simple.json"))
        val expectedOut = Feature.fromJson(readResource("transformation/bezierspline/out/simple.json"))

        assertEquals(expectedOut.geometry, bezierSpline(feature.geometry as LineString))
    }

    /**
     * This test is designed to draw a bezierSpline across the 180th Meridian
     *
     * @see <a href="https://github.com/Turfjs/turf/issues/1063">
     */
    @Test
    fun testBezierSplineAcrossPacific() {
        val feature = Feature.fromJson(readResource("transformation/bezierspline/in/issue-#1063.json"))
        val expectedOut = Feature.fromJson(readResource("transformation/bezierspline/out/issue-#1063.json"))

        assertEquals(expectedOut.geometry, bezierSpline(feature.geometry as LineString))
    }

    @Test
    fun testSimplifyLineString() {
        val feature = Feature.fromJson(readResource("transformation/simplify/in/linestring.json"))
        val expected = Feature.fromJson(readResource("transformation/simplify/out/linestring.json"))
        val simplified = simplify(feature.geometry as LineString, 0.01, false)
        val roundedSimplified = LineString(simplified.coordinates.map { position ->
            Position(
                (position.longitude * 1000000).roundToInt() / 1000000.0,
                (position.latitude * 1000000).roundToInt() / 1000000.0
            )
        })
        assertEquals(expected.geometry as LineString, roundedSimplified)
    }
}
