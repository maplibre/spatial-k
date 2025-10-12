package org.maplibre.spatialk.turf.measurement

import kotlin.test.Test
import kotlin.test.assertEquals
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.testutil.assertPositionEquals
import org.maplibre.spatialk.testutil.readResourceFile
import org.maplibre.spatialk.units.extensions.kilometers

class AlongTest {

    @Test
    fun testAlong() {
        val geometry = LineString.fromJson(readResourceFile("measurement/along/lineString.json"))

        assertPositionEquals(
            Position(-79.4179672644524, 43.636029126566484),
            geometry.locateAlong(1.kilometers),
        )
        assertPositionEquals(
            Position(-79.39973865844715, 43.63797943080659),
            geometry.locateAlong(2.5.kilometers),
        )
        assertPositionEquals(
            Position(-79.37493324279785, 43.64470906117713),
            geometry.locateAlong(100.kilometers),
        )
        assertEquals(geometry.coordinates.last(), geometry.locateAlong(100.kilometers))
    }
}
