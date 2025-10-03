package org.maplibre.spatialk.turf.measurement

import kotlin.test.Test
import kotlin.test.assertEquals
import org.maplibre.spatialk.geojson.BoundingBox

class SquareTest {

    @Test
    fun testSquare() {
        val bbox1 = BoundingBox(0.0, 0.0, 5.0, 10.0)
        val bbox2 = BoundingBox(0.0, 0.0, 10.0, 5.0)

        val square1 = bbox1.square()
        val square2 = bbox2.square()

        assertEquals(BoundingBox(-2.5, 0.0, 7.5, 10.0), square1)
        assertEquals(BoundingBox(0.0, -2.5, 10.0, 7.5), square2)
    }
}
