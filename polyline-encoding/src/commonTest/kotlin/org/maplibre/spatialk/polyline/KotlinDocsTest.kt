@file:Suppress("UnusedVariable", "unused")

package org.maplibre.spatialk.polyline

import kotlin.test.Test
import org.maplibre.spatialk.geojson.Position

// These snippets are primarily intended to be included in documentation. Though they exist as
// part of the test suite, they are not intended to be comprehensive tests.

class KotlinDocsTest {
    @Test
    fun example() {
        // --8<-- [start:example]
        val positions =
            listOf(
                Position(longitude = -120.2, latitude = 38.5),
                Position(longitude = -120.95, latitude = 40.7),
                Position(longitude = -126.453, latitude = 43.252),
            )
        val encoded = PolylineEncoding.encode(positions)
        val decoded = PolylineEncoding.decode(encoded)
        // --8<-- [end:example]
    }

    @Test
    fun encode() {
        // --8<-- [start:encode]
        val encoded =
            PolylineEncoding.encode(
                listOf(
                    Position(longitude = -120.2, latitude = 38.5),
                    Position(longitude = -120.95, latitude = 40.7),
                )
            )
        // --8<-- [end:encode]
    }

    @Test
    fun decode() {
        // --8<-- [start:decode]
        val positions = PolylineEncoding.decode("_p~iF~ps|U_ulLnnqC")
        // --8<-- [end:decode]
    }

    @Test
    fun decodeOrNull() {
        // --8<-- [start:decodeOrNull]
        val positions = PolylineEncoding.decodeOrNull("_p~iF~ps|U_ulLnnqC")
        // --8<-- [end:decodeOrNull]
    }

    @Test
    fun precision() {
        // --8<-- [start:precision]
        val positions =
            listOf(
                Position(longitude = -120.2, latitude = 38.5),
                Position(longitude = -120.95, latitude = 40.7),
            )
        val encoded = PolylineEncoding.encode(positions, precision = 6)
        val decoded = PolylineEncoding.decode(encoded, precision = 6)
        // --8<-- [end:precision]
    }
}
