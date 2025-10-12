package org.maplibre.spatialk.turf.measurement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertIs
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.MultiLineString
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.turf.coordinatemutation.flattenCoordinates

class GreatCircleTest {

    @Test
    fun testGreatCircleBasic() {
        val startLon = -122.0
        val startLat = 48.0
        val endLon = 28.125
        val endLat = 43.32517767999296

        val start = Position(startLon, startLat)
        val end = Position(endLon, endLat)

        val greatCircle = greatCircle(start, end, pointCount = 99)

        assertEquals(99, greatCircle.flattenCoordinates().size)
        assertIs<LineString>(greatCircle)
    }

    @Test
    fun testGreatCirclePassesAntimeridian() {
        val startLon = -122.349358
        val startLat = 47.620422
        val endLon = 77.036560
        val endLat = 38.897957

        val start = Position(startLon, startLat)
        val end = Position(endLon, endLat)
        val geometry = greatCircle(start, end, pointCount = 100)
        assertIs<MultiLineString>(geometry)
    }

    @Test
    fun testGreatCircleAntipodals() {
        val startLat = 47.620422

        val start = Position(-122.349358, startLat)
        val antipodal = Position(106.33, startLat)

        assertFails { greatCircle(start, antipodal) }
    }
}
