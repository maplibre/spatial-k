package org.maplibre.spatialk.turf.transformation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonPrimitive
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.testutil.assertPositionEquals
import org.maplibre.spatialk.testutil.readResourceFile
import org.maplibre.spatialk.turf.ExperimentalTurfApi
import org.maplibre.spatialk.turf.meta.coordAll
import org.maplibre.spatialk.units.Length
import org.maplibre.spatialk.units.kilometers

@ExperimentalTurfApi
class CircleTest {

    @Test
    fun testCircle() {
        val point = Feature.fromJson(readResourceFile("transformation/circle/in/circle1.json"))
        val expectedOut =
            FeatureCollection.fromJson(readResourceFile("transformation/circle/out/circle1.json"))

        val (_, expectedCircle) = expectedOut.features

        val circle =
            circle(
                center = point.geometry as Point,
                radius =
                    point.properties["radius"]?.jsonPrimitive?.double?.kilometers ?: Length.ZERO,
            )

        val allCoordinates = expectedCircle.geometry?.coordAll().orEmpty()
        assertTrue(allCoordinates.isNotEmpty())
        assertEquals(allCoordinates.size, circle.coordAll().size)
        allCoordinates.forEachIndexed { i, position ->
            assertPositionEquals(position, circle.coordAll()[i])
        }
    }
}
