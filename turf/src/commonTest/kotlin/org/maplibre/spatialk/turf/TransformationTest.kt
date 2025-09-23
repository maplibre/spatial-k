package org.maplibre.spatialk.turf

import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.turf.utils.assertPositionEquals
import org.maplibre.spatialk.turf.utils.readResource
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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
    fun testCircle() {
        val point = Feature.fromJson(readResource("transformation/circle/in/circle1.json"))
        val expectedOut = FeatureCollection.fromJson(readResource("transformation/circle/out/circle1.json"))

        val (_, expectedCircle) = expectedOut.features

        val circle = circle(
            center = point.geometry as Point,
            radius = point.properties["radius"]?.jsonPrimitive?.double ?: 0.0,
        )

        val allCoordinates = expectedCircle.geometry?.coordAll().orEmpty()
        assertTrue(allCoordinates.isNotEmpty())
        assertEquals(allCoordinates.size, circle.coordAll().size)
        allCoordinates.forEachIndexed { i, position ->
            assertPositionEquals(position, circle.coordAll()[i])
        }
    }
}
