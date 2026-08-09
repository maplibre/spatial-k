package org.maplibre.spatialk.turf.coordinatemutation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.json.JsonObject
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.Geometry
import org.maplibre.spatialk.geojson.MultiPoint
import org.maplibre.spatialk.geojson.Polygon
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.testutil.readResourceFile

class CleanCoordinatesTest {

    @Test
    fun testCleanSegment() {
        testGeneric("coordinatemutation/cleancoordinates/in/clean-segment.geojson")
    }

    @Test
    fun testClosedLineString() {
        testGeneric("coordinatemutation/cleancoordinates/in/closed-linestring.geojson")
    }

    @Test
    fun testGeometry() {
        val polygon =
            Polygon.fromJson(
                readResourceFile("coordinatemutation/cleancoordinates/in/geometry.geojson")
            )
        val expected =
            Polygon.fromJson(
                readResourceFile("coordinatemutation/cleancoordinates/out/geometry.geojson")
            )
        assertEquals(expected, polygon.cleanCoordinates())
    }

    @Test
    fun testLine3Coords() {
        testGeneric("coordinatemutation/cleancoordinates/in/line-3-coords.geojson")
    }

    @Test
    fun testMultiLine() {
        testGeneric("coordinatemutation/cleancoordinates/in/multiline.geojson")
    }

    @Test
    fun testMultiPoint() {
        testGeneric("coordinatemutation/cleancoordinates/in/multipoint.geojson")
    }

    @Test
    fun testMultiPolygon() {
        testGeneric("coordinatemutation/cleancoordinates/in/multipolygon.geojson")
    }

    @Test
    fun testPoint() {
        testGeneric("coordinatemutation/cleancoordinates/in/point.geojson")
    }

    @Test
    fun testPolygon() {
        testGeneric("coordinatemutation/cleancoordinates/in/polygon.geojson")
    }

    @Test
    fun testPolygonWithHole() {
        testGeneric("coordinatemutation/cleancoordinates/in/polygon-with-hole.geojson")
    }

    @Test
    fun testSegment() {
        testGeneric("coordinatemutation/cleancoordinates/in/segment.geojson")
    }

    @Test
    fun testSimpleLine() {
        testGeneric("coordinatemutation/cleancoordinates/in/simple-line.geojson")
    }

    @Test
    fun testTriangle() {
        testGeneric("coordinatemutation/cleancoordinates/in/triangle.geojson")
    }

    @Test
    fun testTriplicateIssue1255() {
        val polygon =
            Polygon.fromJson(
                readResourceFile(
                    "coordinatemutation/cleancoordinates/in/triplicate-issue1255.geojson"
                )
            )
        val expected =
            Polygon.fromJson(
                readResourceFile(
                    "coordinatemutation/cleancoordinates/out/triplicate-issue1255.geojson"
                )
            )
        assertEquals(expected, polygon.cleanCoordinates())
    }

    // The dedup key must include altitude so positions that differ only in z are preserved.
    @Test
    fun testMultiPointKeepsAltitudeDistinctPositions() {
        val multiPoint =
            MultiPoint(Position(0.0, 0.0, 1.0), Position(0.0, 0.0, 2.0), Position(0.0, 0.0, 1.0))
        assertEquals(
            MultiPoint(Position(0.0, 0.0, 1.0), Position(0.0, 0.0, 2.0)),
            multiPoint.cleanCoordinates(),
        )
    }

    private fun testGeneric(pathIn: String) {
        val pathOut = pathIn.replace("/in/", "/out/")
        val geometry = Feature.fromJson<Geometry?, JsonObject?>(readResourceFile(pathIn)).geometry!!
        val expected =
            Feature.fromJson<Geometry?, JsonObject?>(readResourceFile(pathOut)).geometry!!
        assertEquals(expected, geometry.cleanCoordinates())
    }
}
