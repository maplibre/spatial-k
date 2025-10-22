package org.maplibre.spatialk.gpx

import kotlin.test.Test
import kotlin.test.assertEquals
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.SensitiveGeoJsonApi
import org.maplibre.spatialk.testutil.readResourceFile

class GpxTest {
    @OptIn(SensitiveGeoJsonApi::class)
    @Test
    fun testWaypoints() {
        val document = Gpx.decodeFromString(readResourceFile("in/waypoints.gpx"))
        assertEquals(3, document.waypoints.size)
        assertEquals("WPT001", document.waypoints[0].name)

        assertEquals(
            FeatureCollection.fromJson<Point, Waypoint>(readResourceFile("out/waypoints.json")),
            document.waypoints.toGeoJson(),
        )

        stripEquals(readResourceFile("out/waypoints.gpx"), Gpx.encodeToString(document))
    }

    @Test
    fun testTrack() {
        val document = Gpx.decodeFromString(readResourceFile("in/track.gpx"))
        stripEquals(readResourceFile("out/track.gpx"), Gpx.encodeToString(document))
    }

    @Test
    fun testRoute() {
        val document = Gpx.decodeFromString(readResourceFile("in/route.gpx"))
        stripEquals(readResourceFile("out/route.gpx"), Gpx.encodeToString(document))
    }

    fun stripEquals(expected: String, actual: String) {
        val expected = expected.replace(Regex("\\s+"), "")
        val actual = actual.replace(Regex("\\s+"), "")

        // dirty hack for different number formatting on nodeJs platform
        if (!actual.contains(".0<")) {
            assertEquals(expected.replace(".0<", "<"), actual)
        } else {
            assertEquals(expected, actual)
        }
    }
}
