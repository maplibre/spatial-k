package org.maplibre.spatialk.gpx

import kotlin.test.Test
import kotlin.test.assertEquals
import org.maplibre.spatialk.geojson.GeoJson
import org.maplibre.spatialk.testutil.readResourceFile

class GpxTest {
    @Test
    fun testGpx() {
        val resource = readResourceFile("in/sample.gpx")
        val document = Gpx.decodeFromString(resource)
        assertEquals(3, document.waypoints.size)
        assertEquals("Reichstag (Berlin)", document.waypoints[0].name)

        val serialized = Gpx.encodeToString(document)
        stripEquals(resource, serialized)
    }

    @Test
    fun testGpxToGeoJson() {
        val inFile = readResourceFile("in/Donauradweg.gpx")
        val document = Gpx.decodeFromString(inFile)
        assertEquals(3, document.waypoints.size)

        val geojson = document.tracks.first().trkseg.first().trkpt.toGeoJson()
        val outFile = readResourceFile("out/Donauradweg.json")

        assertEquals(outFile, GeoJson.encodeToString(geojson))
    }

    fun stripEquals(expected: String, actual: String) {
        assertEquals(expected.replace(Regex("\\s+"), ""), actual.replace(Regex("\\s+"), ""))
    }
}
