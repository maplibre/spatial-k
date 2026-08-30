package org.maplibre.spatialk.turf.booleans

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.testutil.readResourceFile

class PointOnLineTest {

    @Test
    fun testDefaultValues() {
        val start = Position(54.0, 10.0)
        val end = Position(53.0, 10.0)
        val lineString = LineString(start, end)
        assertTrue(pointOnLine(start, lineString))
        assertTrue(pointOnLine(Point(start), lineString))
    }

    // A degenerate segment (identical endpoints) contains exactly its own coordinate; the bounding
    // checks must not exclude it.
    @Test
    fun testZeroLengthSegmentContainsItsPoint() {
        val position = Position(0.0, 0.0)
        val degenerate = LineString(position, position)
        assertTrue(pointOnLine(position, degenerate))
        assertTrue(pointOnLine(Point(position), degenerate))
        assertFalse(pointOnLine(Position(1.0, 1.0), degenerate))
    }

    @Test
    fun testTrue() {
        listOf(
                "booleans/pointOnLine/true/LineWithOnly1Segment.geojson",
                "booleans/pointOnLine/true/LineWithOnly1SegmentOnStart.geojson",
                "booleans/pointOnLine/true/PointOnFirstSegment.geojson",
                "booleans/pointOnLine/true/PointOnLastSegment.geojson",
                "booleans/pointOnLine/true/PointOnLineEnd.geojson",
                "booleans/pointOnLine/true/PointOnLineMidpoint.geojson",
                "booleans/pointOnLine/true/PointOnLineMidVertice.geojson",
                "booleans/pointOnLine/true/PointOnLineStart.geojson",
                "booleans/pointOnLine/true/PointOnLineWithEpsilon.geojson",
            )
            .forEach { path ->
                val (ignoreEndVertices, epsilon) = readTestParams(path)
                val fc =
                    FeatureCollection.fromJson<
                        org.maplibre.spatialk.geojson.Geometry,
                        kotlinx.serialization.json.JsonObject?,
                    >(
                        readResourceFile(path)
                    )
                val point = fc.first().geometry as Point
                val lineString = fc.last().geometry as LineString

                assertTrue(
                    pointOnLine(point, lineString, ignoreEndVertices, epsilon),
                    "assertion failed for path $path",
                )
            }
    }

    @Test
    fun testFalse() {
        listOf(
                "booleans/pointOnLine/false/LineWithOnly1SegmentIgnoreBoundary.geojson",
                "booleans/pointOnLine/false/LineWithOnly1SegmentIgnoreBoundaryEnd.geojson",
                "booleans/pointOnLine/false/notOnLine.geojson",
                "booleans/pointOnLine/false/PointIsOnLineButFailsWithoutEpsilonForBackwardsCompatibility.geojson",
                "booleans/pointOnLine/false/PointIsOnLineButFailsWithSmallEpsilonValue.geojson",
                "booleans/pointOnLine/false/PointOnEndFailsWhenIgnoreEndpoints.geojson",
                "booleans/pointOnLine/false/PointOnStartFailsWhenIgnoreEndpoints.geojson",
            )
            .forEach { path ->
                val (ignoreEndVertices, epsilon) = readTestParams(path)
                val fc =
                    FeatureCollection.fromJson<
                        org.maplibre.spatialk.geojson.Geometry,
                        kotlinx.serialization.json.JsonObject?,
                    >(
                        readResourceFile(path)
                    )
                val point = fc.first().geometry as Point
                val lineString = fc.last().geometry as LineString

                assertFalse(
                    pointOnLine(point, lineString, ignoreEndVertices, epsilon),
                    "assertion failed for path $path",
                )
            }
    }

    private fun readTestParams(path: String): Pair<Boolean, Double?> {
        val json = Json.parseToJsonElement(readResourceFile(path)).jsonObject
        val props = json["properties"]?.jsonObject
        return (props?.get("ignoreEndVertices")?.jsonPrimitive?.booleanOrNull ?: false) to
            props?.get("epsilon")?.jsonPrimitive?.doubleOrNull
    }
}
