package org.maplibre.spatialk.geojson

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.serialization.json.Json

class BoundingBoxTest {
    @Test
    fun southWest_doesReturnMostSouthwestCoordinate() {
        val southwest = Position(1.0, 2.0)
        val northeast = Position(3.0, 4.0)
        val boundingBox = BoundingBox(southwest, northeast)
        assertEquals(southwest, boundingBox.southwest)
    }

    @Test
    fun northEast_doesReturnMostNortheastCoordinate() {
        val southwest = Position(1.0, 2.0)
        val northeast = Position(3.0, 4.0)
        val boundingBox = BoundingBox(southwest, northeast)
        assertEquals(northeast, boundingBox.northeast)
    }

    @Test
    fun accessors() {
        val southwest = Position(1.0, 2.0)
        val northeast = Position(3.0, 4.0)
        val boundingBox = BoundingBox(southwest, northeast)
        assertEquals(southwest, Position(boundingBox.west, boundingBox.south))
        assertEquals(northeast, Position(boundingBox.east, boundingBox.north))
    }

    @Test
    fun JSON_serializing_without_altitude() {
        val boundingBox = BoundingBox(Position(1.1, 2.2), Position(3.3, 4.4))
        val json = boundingBox.json()

        assertEquals(Json.parseToJsonElement(json), Json.parseToJsonElement("[1.1, 2.2, 3.3, 4.4]"))
    }

    @Test
    fun JSON_serializing_with_altitude() {
        val boundingBox = BoundingBox(Position(1.1, 2.2, 3.3), Position(4.4, 5.5, 6.6))
        val json = boundingBox.json()

        assertEquals(
            Json.parseToJsonElement(json),
            Json.parseToJsonElement("[1.1, 2.2, 3.3, 4.4, 5.5, 6.6]"),
        )
    }

    @Test
    fun JSON_deserializing_without_altitude() {
        val json = "[1.0, 2.0, 3.0, 4.0]"
        val boundingBox = BoundingBox.fromJson(json)

        assertEquals(BoundingBox(Position(1.0, 2.0), Position(3.0, 4.0)), boundingBox)
    }

    @Test
    fun JSON_deserializing_with_altitude() {
        val json = "[1.0, 2.0, 3.0, 4.0, 5.0, 6.0]"
        val boundingBox = BoundingBox.fromJson(json)

        assertEquals(BoundingBox(Position(1.0, 2.0, 3.0), Position(4.0, 5.0, 6.0)), boundingBox)
    }

    @Test
    fun JSON_deserializing_with_wrong_array_size() {
        val json = "[1.0, 2.0, 3.0, 4.0, 5.0]"

        assertFailsWith(IllegalArgumentException::class) { BoundingBox.fromJson(json) }
    }
}
