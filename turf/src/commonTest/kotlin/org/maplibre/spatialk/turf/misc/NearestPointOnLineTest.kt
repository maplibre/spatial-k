package org.maplibre.spatialk.turf.misc

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.json.JsonObject
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Geometry
import org.maplibre.spatialk.geojson.MultiLineString
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.testutil.assertDoubleEquals
import org.maplibre.spatialk.testutil.readResourceFile
import org.maplibre.spatialk.units.extensions.inKilometers

class NearestPointOnLineTest {

    @Test
    fun testNearestPointOnLine() {
        val (multiLine, point) =
            FeatureCollection.fromJson<Geometry?, JsonObject?>(
                    readResourceFile("misc/nearestPointOnLine/multiLine.json")
                )
                .features

        val result =
            (multiLine.geometry as MultiLineString).nearestPointTo(
                (point.geometry as Point).coordinates
            )
        assertDoubleEquals(123.924613, result.point.longitude, 0.00001)
        assertDoubleEquals(-19.025117, result.point.latitude, 0.00001)
        assertDoubleEquals(120.886021, result.distance.inKilometers, 0.00001)
        assertDoubleEquals(214.548785, result.location.inKilometers, 0.00001)
        assertEquals(0, result.index)
    }
}
