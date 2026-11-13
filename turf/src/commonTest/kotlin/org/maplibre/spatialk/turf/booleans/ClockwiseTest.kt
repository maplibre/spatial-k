package org.maplibre.spatialk.turf.booleans

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.Position

class ClockwiseTest {

    @Test
    fun testCounterClockwise() {
        val lineString =
            LineString(
                arrayOf(
                    doubleArrayOf(0.0, 0.0),
                    doubleArrayOf(1.0, 0.0),
                    doubleArrayOf(1.0, 1.0),
                    doubleArrayOf(0.0, 0.0),
                )
            )
        assertFalse(clockwise(lineString))
    }

    @Test
    fun testClockwise() {
        val lineString =
            LineString(
                arrayOf(
                    doubleArrayOf(0.0, 0.0),
                    doubleArrayOf(1.0, 1.0),
                    doubleArrayOf(1.0, 0.0),
                    doubleArrayOf(0.0, 0.0),
                )
            )
        assertTrue(clockwise(lineString))
    }

    @Test
    fun testCounterClockwiseRingList() {
        val ring =
            listOf(
                Position(0.0, 0.0),
                Position(1.0, 0.0),
                Position(1.0, 1.0),
                Position(0.0, 0.0),
            )
        assertFalse(clockwise(ring))
    }

    @Test
    fun testClockwiseRingList() {
        val ring =
            listOf(
                Position(0.0, 0.0),
                Position(1.0, 1.0),
                Position(1.0, 0.0),
                Position(0.0, 0.0),
            )
        assertTrue(clockwise(ring))
    }
}
