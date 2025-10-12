package org.maplibre.spatialk.turf.misc

import kotlin.test.Test
import kotlin.test.assertTrue
import kotlinx.serialization.json.JsonObject
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.testutil.assertPositionEquals
import org.maplibre.spatialk.testutil.readResourceFile
import org.maplibre.spatialk.turf.measurement.locateAlong
import org.maplibre.spatialk.units.extensions.miles

class LineSliceAlongTest {

    @Test
    fun testLineSliceAlong() {
        val lineFeature =
            Feature.fromJson<LineString, JsonObject?>(
                readResourceFile("misc/lineSliceAlong/line1.geojson")
            )
        val line = lineFeature.geometry

        val start = 500.miles
        val stop = 750.miles

        val sliced = line.slice(start, stop)

        // Verify the sliced line starts and ends at the correct positions
        val expectedStart = line.locateAlong(start)
        val expectedStop = line.locateAlong(stop)

        assertPositionEquals(expectedStart, sliced.coordinates.first())
        assertPositionEquals(expectedStop, sliced.coordinates.last())

        // Verify it's a valid LineString with at least 2 points
        assertTrue(sliced.coordinates.size >= 2)
    }

    @Test
    fun testLineSliceAlongOvershoot() {
        val lineFeature =
            Feature.fromJson<LineString, JsonObject?>(
                readResourceFile("misc/lineSliceAlong/line1.geojson")
            )
        val line = lineFeature.geometry

        val start = 500.miles
        val stop = 1500.miles

        val sliced = line.slice(start, stop)

        // When stop overshoots, it should end at the last point of the line
        val expectedStart = line.locateAlong(start)
        val expectedStop = line.locateAlong(stop) // This should clamp to the end

        assertPositionEquals(expectedStart, sliced.coordinates.first())
        assertPositionEquals(expectedStop, sliced.coordinates.last())

        // Verify it's a valid LineString
        assertTrue(sliced.coordinates.size >= 2)
    }

    @Test
    fun testLineSliceAlongRoute1() {
        val routeFeature =
            Feature.fromJson<LineString, JsonObject?>(
                readResourceFile("misc/lineSliceAlong/route1.geojson")
            )
        val route = routeFeature.geometry

        val start = 500.miles
        val stop = 750.miles

        val sliced = route.slice(start, stop)

        // Verify the sliced line starts and ends at the correct positions
        val expectedStart = route.locateAlong(start)
        val expectedStop = route.locateAlong(stop)

        assertPositionEquals(expectedStart, sliced.coordinates.first())
        assertPositionEquals(expectedStop, sliced.coordinates.last())

        // Verify it's a valid LineString
        assertTrue(sliced.coordinates.size >= 2)
    }

    @Test
    fun testLineSliceAlongRoute2() {
        val routeFeature =
            Feature.fromJson<LineString, JsonObject?>(
                readResourceFile("misc/lineSliceAlong/route2.geojson")
            )
        val route = routeFeature.geometry

        val start = 25.miles
        val stop = 50.miles

        val sliced = route.slice(start, stop)

        // Verify the sliced line starts and ends at the correct positions
        val expectedStart = route.locateAlong(start)
        val expectedStop = route.locateAlong(stop)

        assertPositionEquals(expectedStart, sliced.coordinates.first())
        assertPositionEquals(expectedStop, sliced.coordinates.last())

        // Verify it's a valid LineString
        assertTrue(sliced.coordinates.size >= 2)
    }
}
