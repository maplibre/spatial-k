@file:Suppress("UnusedVariable", "unused")

package org.maplibre.spatialk.turf

import kotlin.test.Test
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.turf.measurement.offset
import org.maplibre.spatialk.units.Bearing
import org.maplibre.spatialk.units.extensions.kilometers

// These snippets are primarily intended to be included in documentation. Though they exist as
// part of the test suite, they are not intended to be comprehensive tests.

class KotlinDocsTest {
    @Test
    fun example() {
        // --8<-- [start:example]
        val point = Position(-75.0, 45.0)
        val (longitude, latitude) = point.offset(100.kilometers, Bearing.North)
        // --8<-- [end:example]
    }
}
