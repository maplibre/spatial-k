package org.maplibre.spatialk.turf.measurement

import kotlin.test.Test
import kotlin.test.assertEquals
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.testutil.readResourceFile
import org.maplibre.spatialk.units.extensions.inKilometers

class LengthTest {

    @Test
    fun testLength() {
        val geometry = LineString.fromJson(readResourceFile("measurement/length/lineString.json"))

        assertEquals(42.560767589197006, geometry.length().inKilometers)
    }
}
