package org.maplibre.spatialk.turf.measurement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.json.JsonObject
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Geometry
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.testutil.readResourceFile

class PolygonTangentsTest {

    @Test
    fun testPolygonTangentsConcave() {
        runTest(
            "measurement/polygontangents/in/concave.geojson",
            "measurement/polygontangents/out/concave.geojson",
        )
    }

    @Test
    fun testPolygonTangentsHigh() {
        runTest(
            "measurement/polygontangents/in/high.geojson",
            "measurement/polygontangents/out/high.geojson",
        )
    }

    @Test
    fun testPolygonTangentsTurfJsIssue785() {
        runTest(
            "measurement/polygontangents/in/issue785.geojson",
            "measurement/polygontangents/out/issue785.geojson",
        )
    }

    @Test
    fun testPolygonTangentsTurfJsIssue1032() {
        runTest(
            "measurement/polygontangents/in/issue1032.geojson",
            "measurement/polygontangents/out/issue1032.geojson",
        )
    }

    @Test
    fun testPolygonTangentsTurfJsIssue1050() {
        runTest(
            "measurement/polygontangents/in/issue1050.geojson",
            "measurement/polygontangents/out/issue1050.geojson",
        )
    }

    @Test
    fun testPolygonTangentsMultipolygon() {
        runTest(
            "measurement/polygontangents/in/multipolygon.geojson",
            "measurement/polygontangents/out/multipolygon.geojson",
        )
    }

    @Test
    fun testPolygonTangentsPolygonWithHole() {
        runTest(
            "measurement/polygontangents/in/polygonWithHole.geojson",
            "measurement/polygontangents/out/polygonWithHole.geojson",
        )
    }

    @Test
    fun testPolygonTangentsSquare() {
        runTest(
            "measurement/polygontangents/in/square.geojson",
            "measurement/polygontangents/out/square.geojson",
        )
    }

    // Regression: a point inside the bbox of a polygon with a hole must not index the outer ring
    // with a hole-inclusive nearest index (previously threw ArrayIndexOutOfBoundsException).
    @Test
    fun testPolygonTangentsPolygonWithHolePointInsideBbox() {
        val polygon =
            org.maplibre.spatialk.geojson.Polygon(
                listOf(
                    listOf(
                        org.maplibre.spatialk.geojson.Position(0.0, 0.0),
                        org.maplibre.spatialk.geojson.Position(0.0, 10.0),
                        org.maplibre.spatialk.geojson.Position(10.0, 10.0),
                        org.maplibre.spatialk.geojson.Position(10.0, 0.0),
                        org.maplibre.spatialk.geojson.Position(0.0, 0.0),
                    ),
                    listOf(
                        org.maplibre.spatialk.geojson.Position(4.0, 4.0),
                        org.maplibre.spatialk.geojson.Position(4.0, 6.0),
                        org.maplibre.spatialk.geojson.Position(6.0, 6.0),
                        org.maplibre.spatialk.geojson.Position(6.0, 4.0),
                        org.maplibre.spatialk.geojson.Position(4.0, 4.0),
                    ),
                )
            )
        val tangents = polygonTangents(org.maplibre.spatialk.geojson.Position(5.0, 5.0), polygon)
        assertEquals(2, tangents.size)
    }

    private fun runTest(inputPath: String, expectedPath: String) {
        val features =
            FeatureCollection.fromJson<Geometry?, JsonObject?>(readResourceFile(inputPath))
                .features
                .map { it.geometry }
        val polygon =
            features.filterIsInstance<org.maplibre.spatialk.geojson.PolygonGeometry>().first()
        val point = features.filterIsInstance<Point>().first()

        val expectedTangents =
            FeatureCollection.fromJson<Geometry?, JsonObject?>(readResourceFile(expectedPath))
                .features
                .mapNotNull { it.geometry }
                .filterIsInstance<Point>()

        val (firstTan, secondTan) = polygonTangents(point.coordinates, polygon)

        assertEquals(expectedTangents[0], firstTan)
        assertEquals(expectedTangents[1], secondTan)
    }
}
