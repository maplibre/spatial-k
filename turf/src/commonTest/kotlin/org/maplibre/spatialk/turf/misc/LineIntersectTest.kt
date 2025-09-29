package org.maplibre.spatialk.turf.misc

import kotlin.test.Test
import kotlin.test.assertEquals
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.testutil.readResourceFile
import org.maplibre.spatialk.turf.ExperimentalTurfApi

@ExperimentalTurfApi
class LineIntersectTest {

    @Test
    fun testLineIntersect() {
        val features =
            FeatureCollection.Companion.fromJson(
                readResourceFile("misc/lineIntersect/twoPoints.json")
            )
        val intersect =
            lineIntersect(
                features.features[0].geometry as LineString,
                features.features[1].geometry as LineString,
            )

        assertEquals(Position(-120.93653884065287, 51.287945374086675), intersect[0])
    }
}
