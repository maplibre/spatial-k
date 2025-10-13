package org.maplibre.spatialk.turf.measurement

import kotlin.test.Test
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.testutil.assertDoubleEquals
import org.maplibre.spatialk.units.Bearing
import org.maplibre.spatialk.units.extensions.kilometers

class DestinationTest {

    @Test
    fun testDestination() {
        val point0 = Position(-75.0, 38.10096062273525)
        val (longitude, latitude) = point0.offset(100.kilometers, Bearing.North)

        assertDoubleEquals(-75.0, longitude, 0.1)
        assertDoubleEquals(39.000281, latitude, 0.000001)
    }
}
