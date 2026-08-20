@file:OptIn(SensitiveGeoJsonApi::class)

package org.maplibre.spatialk.geojson

import kotlin.test.Test
import kotlin.test.assertEquals
import org.maplibre.spatialk.testutil.assertDoubleEquals

class ExtraAxesTest {
    @Test
    fun position_preservesAdditionalElements() {
        val position = Position(-75.1, 45.1, 100.5, 1.5)

        assertEquals(4, position.size)
        assertDoubleEquals(-75.1, position.longitude)
        assertDoubleEquals(45.1, position.latitude)
        assertDoubleEquals(100.5, position.altitude)
        assertDoubleEquals(1.5, position[3])
    }

    @Test
    fun position_jsonRoundTripPreservesAdditionalElements() {
        val position = Position(-75.1, 45.1, 100.5, 1.5)

        assertEquals("[-75.1,45.1,100.5,1.5]", position.toJson())
        assertEquals(position, Position.fromJson(position.toJson()))
    }

    @Test
    fun boundingBox_cornersPreserveAdditionalElements() {
        val southwest = Position(1.1, 2.2, 3.3, 7.7)
        val northeast = Position(4.4, 5.5, 6.6, 8.8)
        val boundingBox = BoundingBox(southwest, northeast)

        assertEquals(8, boundingBox.size)
        assertEquals(4, boundingBox.southwest.size)
        assertEquals(4, boundingBox.northeast.size)
        assertEquals(southwest, boundingBox.southwest)
        assertEquals(northeast, boundingBox.northeast)
    }

    @Test
    fun boundingBox_jsonRoundTripPreservesAdditionalElements() {
        val boundingBox = BoundingBox(Position(1.1, 2.2, 3.3, 7.7), Position(4.4, 5.5, 6.6, 8.8))
        val decoded = BoundingBox.fromJson(boundingBox.toJson())

        assertEquals("[1.1,2.2,3.3,7.7,4.4,5.5,6.6,8.8]", boundingBox.toJson())
        assertEquals(boundingBox, decoded)
        assertEquals(4, decoded.southwest.size)
        assertEquals(4, decoded.northeast.size)
        assertEquals(Position(1.1, 2.2, 3.3, 7.7), decoded.southwest)
        assertEquals(Position(4.4, 5.5, 6.6, 8.8), decoded.northeast)
    }

    @Test
    fun point_jsonRoundTripPreservesAdditionalElements() {
        val point = Point(Position(12.3, 45.6, 100.5, 1.5))

        assertEquals("""{"type":"Point","coordinates":[12.3,45.6,100.5,1.5]}""", point.toJson())
        assertEquals(point, Point.fromJson(point.toJson()))
    }
}
