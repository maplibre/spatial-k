package org.maplibre.spatialk.turf.transformation

import kotlin.math.roundToInt
import kotlin.test.Test
import kotlin.test.assertEquals
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.testutil.readResourceFile
import org.maplibre.spatialk.turf.ExperimentalTurfApi

@ExperimentalTurfApi
class SimplifyTest {

    @Test
    fun testSimplifyLineString() {
        val feature =
            Feature.fromJson(readResourceFile("transformation/simplify/in/linestring.json"))
        val expected =
            Feature.fromJson(readResourceFile("transformation/simplify/out/linestring.json"))
        val simplified = simplify(feature.geometry as LineString, 0.01, false)
        val roundedSimplified =
            LineString(
                simplified.coordinates.map { position ->
                    Position(
                        (position.longitude * 1000000).roundToInt() / 1000000.0,
                        (position.latitude * 1000000).roundToInt() / 1000000.0,
                    )
                }
            )
        assertEquals(expected.geometry as LineString, roundedSimplified)
    }
}
