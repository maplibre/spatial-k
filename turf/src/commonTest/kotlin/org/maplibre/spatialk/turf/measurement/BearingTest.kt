package org.maplibre.spatialk.turf.measurement

import kotlin.test.Test
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.testutil.assertBearingEquals
import org.maplibre.spatialk.units.Bearing.Companion.North
import org.maplibre.spatialk.units.extensions.degrees

class BearingTest {

    @Test
    fun testBearing() {
        val start = Position(-75.0, 45.0)
        val end = Position(20.0, 60.0)

        assertBearingEquals(
            North + 37.75495.degrees,
            start.bearingTo(end),
            message = "Initial Bearing",
        )
        assertBearingEquals(
            North + 120.01405.degrees,
            start.bearingTo(end, final = true),
            message = "Final Bearing",
        )
    }

    @Test
    fun testRhumbBearing() {
        val start = Position(-75.0, 45.0)
        val end = Position(20.0, 60.0)
        assertBearingEquals(North + 75.28061364784332.degrees, start.rhumbBearingTo(end))
    }
}
