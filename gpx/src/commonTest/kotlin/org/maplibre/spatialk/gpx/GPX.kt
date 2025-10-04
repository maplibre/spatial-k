package org.maplibre.spatialk.gpx

import kotlin.test.Test
import kotlin.test.assertEquals
import org.maplibre.spatialk.testutil.readResourceFile

class GpxTest {
    @Test
    fun testGpx() {
        val resource = readResourceFile("sample.gpx")
        val document = Gpx.decodeFromString(resource)
        assertEquals(3, document.wpt.size)
        assertEquals("Reichstag (Berlin)", document.wpt[0].name)

        val serialized = Gpx.encodeToString(document)
        stripEquals(resource, serialized)
    }

    fun stripEquals(expected: String, actual: String) {
        assertEquals(expected.replace(Regex("\\s+"), ""), actual.replace(Regex("\\s+"), ""))
    }
}
