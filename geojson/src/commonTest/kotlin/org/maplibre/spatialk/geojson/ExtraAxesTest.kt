@file:OptIn(SensitiveGeoJsonApi::class)

package org.maplibre.spatialk.geojson

import kotlin.test.Test
import kotlin.test.assertEquals
import org.maplibre.spatialk.geojson.utils.assertJsonEquals

class ExtraAxesTest {
    @Test
    fun position_preservesAdditionalElements() {
        val position = Position(-75.0, 45.0, 100.0, 1.5)

        assertEquals(4, position.size)
        assertEquals(-75.0, position.longitude)
        assertEquals(45.0, position.latitude)
        assertEquals(100.0, position.altitude)
        assertEquals(1.5, position[3])
    }

    @Test
    fun position_jsonRoundTripPreservesAdditionalElements() {
        val position = Position(-75.0, 45.0, 100.0, 1.5)

        assertJsonEquals("[-75.0, 45.0, 100.0, 1.5]", position.toJson())
        assertEquals(position, Position.fromJson(position.toJson()))
    }

    @Test
    fun boundingBox_cornersPreserveAdditionalElements() {
        val southwest = Position(1.0, 2.0, 3.0, 7.0)
        val northeast = Position(4.0, 5.0, 6.0, 8.0)
        val boundingBox = BoundingBox(southwest, northeast)

        assertEquals(8, boundingBox.size)
        assertEquals(4, boundingBox.southwest.size)
        assertEquals(4, boundingBox.northeast.size)
        assertEquals(southwest, boundingBox.southwest)
        assertEquals(northeast, boundingBox.northeast)
    }

    @Test
    fun boundingBox_jsonRoundTripPreservesAdditionalElements() {
        val boundingBox = BoundingBox(Position(1.0, 2.0, 3.0, 7.0), Position(4.0, 5.0, 6.0, 8.0))
        val decoded = BoundingBox.fromJson(boundingBox.toJson())

        assertJsonEquals("[1.0, 2.0, 3.0, 7.0, 4.0, 5.0, 6.0, 8.0]", boundingBox.toJson())
        assertEquals(boundingBox, decoded)
        assertEquals(4, decoded.southwest.size)
        assertEquals(4, decoded.northeast.size)
        assertEquals(Position(1.0, 2.0, 3.0, 7.0), decoded.southwest)
        assertEquals(Position(4.0, 5.0, 6.0, 8.0), decoded.northeast)
    }

    @Test
    fun point_jsonRoundTripPreservesAdditionalElements() {
        val point = Point(Position(100.0, 0.0, 200.0, 1.5))

        assertJsonEquals(
            """{"type":"Point","coordinates":[100.0, 0.0, 200.0, 1.5]}""",
            point.toJson(),
        )
        assertEquals(point, Point.fromJson(point.toJson()))
    }
}
