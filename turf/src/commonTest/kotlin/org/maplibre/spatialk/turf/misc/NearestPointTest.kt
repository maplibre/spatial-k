package org.maplibre.spatialk.turf.misc

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.dsl.geometryCollectionOf
import org.maplibre.spatialk.testutil.assertDoubleEquals

class NearestPointTest {

    @Test
    fun testNearestPointToFromTurfJs() {
        // Test data from turf.js nearest-point test
        val targetPoint = Point(-75.4, 39.4)
        val points =
            geometryCollectionOf(
                Point(-75.833, 39.284),
                Point(-75.6, 39.984),
                Point(-75.221, 39.125),
                Point(-75.358, 39.987),
                Point(-75.9221, 39.27),
                Point(-75.534, 39.123),
                Point(-75.21, 39.12),
                Point(-75.22, 39.33),
                Point(-75.44, 39.55),
                Point(-75.77, 39.66),
                Point(-75.44, 39.11),
                Point(-75.05, 39.92),
                Point(-75.88, 39.98),
                Point(-75.55, 39.55),
                Point(-75.33, 39.44), // This should be the nearest
                Point(-75.56, 39.24),
                Point(-75.56, 39.36),
            )

        val (nearestPoint, props) = points.nearestPointTo(targetPoint)

        // The nearest point should be at index 14: [-75.33, 39.44]
        assertDoubleEquals(-75.33, nearestPoint.longitude)
        assertDoubleEquals(39.44, nearestPoint.latitude)
        assertEquals(14, props.index)
    }

    @Test
    fun testNearestPointToEmptyCollection() {
        val emptyPoints = geometryCollectionOf<Point>()
        val targetPoint = Point(0.0, 0.0)

        assertFailsWith<NoSuchElementException> { emptyPoints.nearestPointTo(targetPoint) }
    }

    @Test
    fun testNearestPointToSinglePoint() {
        val points = geometryCollectionOf(Point(1.0, 2.0))
        val targetPoint = Point(0.0, 0.0)

        val (nearestPoint) = points.nearestPointTo(targetPoint)
        assertDoubleEquals(1.0, nearestPoint.longitude)
        assertDoubleEquals(2.0, nearestPoint.latitude)
    }

    @Test
    fun testNearestPointToIdenticalPoints() {
        val points = geometryCollectionOf(Point(0.0, 0.0), Point(1.0, 1.0), Point(0.0, 0.0))
        val targetPoint = Point(0.0, 0.0)

        val (nearestPoint, props) = points.nearestPointTo(targetPoint)
        assertDoubleEquals(0.0, nearestPoint.longitude)
        assertDoubleEquals(0.0, nearestPoint.latitude)
        assertEquals(props.index, 0)
        // Should return the first occurrence (index 0)
    }
}
