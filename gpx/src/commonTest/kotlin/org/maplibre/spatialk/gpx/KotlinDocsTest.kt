@file:Suppress("UnusedVariable", "unused")

package org.maplibre.spatialk.gpx

import kotlin.test.Test
import kotlin.time.ExperimentalTime
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.Point

// These snippets are primarily intended to be included in documentation. Though they exist as
// part of the test suite, they are not intended to be comprehensive tests.

class KotlinDocsTest {
    @OptIn(ExperimentalTime::class)
    @Test
    fun example() {
        // --8<-- [start:example]
        val document =
            Document(
                metadata = Metadata(name = "My GPX File"),
                waypoints =
                    listOf(
                        Waypoint(1.0, 2.0),
                        Waypoint(3.0, 4.0),
                        Waypoint(5.0, 6.0),
                        Waypoint(7.0, 8.0),
                    ),
            )

        val gpxString = Gpx.encodeToString(document)
        // --8<-- [end:example]
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun toGeoJson() {
        // --8<-- [start:toGeoJson]
        val waypoint = Waypoint(10.0, 2.0)
        val feature: Feature<Point, Waypoint> = waypoint.toGeoJson()
        // --8<-- [end:toGeoJson]
    }
}
