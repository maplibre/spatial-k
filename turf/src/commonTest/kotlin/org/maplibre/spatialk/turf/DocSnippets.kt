@file:Suppress("UnusedVariable", "unused")

package org.maplibre.spatialk.turf

import kotlin.test.Test
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.turf.measurement.destination
import org.maplibre.spatialk.units.kilometers

// These snippets are primarily intended to be included in docs/turf.md. Though they exist as
// part of the test suite, they are not intended to be comprehensive tests.

class DocSnippets {
    @Test
    fun example() {
        // --8<-- [start:example]
        val point = Position(-75.0, 45.0)
        val (longitude, latitude) = destination(point, 100.kilometers, 0.0)
        // --8<-- [end:example]
    }
}
