package org.maplibre.spatialk.turf.misc

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.json.JsonObject
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Geometry
import org.maplibre.spatialk.geojson.LineStringGeometry
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.testutil.assertDoubleEquals
import org.maplibre.spatialk.testutil.readResourceFile
import org.maplibre.spatialk.units.extensions.inKilometers

class NearestPointOnLineTest {

    @Test
    fun testNearestPointOnLine() {
        val fc =
            FeatureCollection.fromJson<Geometry?, JsonObject?>(
                readResourceFile("misc/nearestPointOnLine/multiLine.json")
            )

        val multiline = fc[0].geometry as LineStringGeometry
        val point = fc[1].geometry as Point
        val (nearestPoint, props) = multiline.nearestPointTo(point.coordinates)

        assertDoubleEquals(123.924613, nearestPoint.longitude)
        assertDoubleEquals(-19.025117, nearestPoint.latitude)
        assertDoubleEquals(120.886021, props.distance.inKilometers)
        assertDoubleEquals(214.548785, props.location.inKilometers)
        assertEquals(0, props.index)
    }
}
