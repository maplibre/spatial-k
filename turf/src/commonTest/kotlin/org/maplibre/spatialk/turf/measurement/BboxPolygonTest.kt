package org.maplibre.spatialk.turf.measurement

import kotlin.test.Test
import kotlin.test.assertEquals
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.geojson.dsl.polygon

class BboxPolygonTest {
    @Test
    fun testBboxPolygon() {
        val bbox = BoundingBox(12.1, 34.3, 56.5, 78.7)

        val polygon = polygon {
            ring {
                +Position(12.1, 34.3)
                +Position(56.5, 34.3)
                +Position(56.5, 78.7)
                +Position(12.1, 78.7)
                complete()
            }
        }

        assertEquals(polygon, bbox.toPolygon())
    }
}
