@file:Suppress("UnusedVariable", "unused")

package org.maplibre.spatialk.geojson

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import org.maplibre.spatialk.geojson.dsl.point

// These snippets are primarily intended to be included in docs/geojson.md. Though they exist as
// part of the test suite, they are not intended to be comprehensive tests.

class DocSnippets {

    @Test
    fun geometryExhaustiveTypeChecks() {
        fun getSomeGeometry(): Geometry = point(0.0, 0.0)

        // --8<-- [start:geometryExhaustiveTypeChecks]
        val geometry: Geometry = getSomeGeometry()

        val type =
            when (geometry) {
                is Point -> "Point"
                is MultiPoint -> "MultiPoint"
                is LineString -> "LineString"
                is MultiLineString -> "MultiLineString"
                is Polygon -> "Polygon"
                is MultiPolygon -> "MultiPolygon"
                is GeometryCollection -> "GeometryCollection"
            }
        // --8<-- [end:geometryExhaustiveTypeChecks]
    }

    private inline fun kotlinAndJsonExample(kotlin: () -> String, json: String) {
        val jsonC = Json { allowComments = true }
        assertEquals<JsonElement>(
            expected = jsonC.decodeFromString(json),
            actual = jsonC.decodeFromString(kotlin()),
        )
    }

    @Test
    fun position() {
        kotlinAndJsonExample(
            kotlin = {
                // --8<-- [start:positionKt]
                val position = Position(-75.0, 45.0)
                val (longitude, latitude, altitude) = position

                // Access values
                position.longitude
                position.latitude
                position.altitude
                // --8<-- [end:positionKt]

                position.json()
            },
            json =
                """
                    // --8<-- [start:positionJson]
                    [-75.0, 45.0]
                    // --8<-- [end:positionJson]
                """,
        )
    }
}
