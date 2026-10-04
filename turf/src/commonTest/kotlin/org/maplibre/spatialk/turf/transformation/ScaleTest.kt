package org.maplibre.spatialk.turf.transformation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonPrimitive
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Geometry
import org.maplibre.spatialk.geojson.GeometryCollection
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.testutil.assertDoubleEquals
import org.maplibre.spatialk.testutil.assertPositionEquals
import org.maplibre.spatialk.testutil.readResourceFile
import org.maplibre.spatialk.turf.coordinatemutation.flattenCoordinates
import org.maplibre.spatialk.turf.measurement.rhumbDistance

class ScaleTest {

    @Test
    fun testScaleIssue1059() {
        testScale(
            "transformation/scale/in/issue-1059.geojson",
            "transformation/scale/out/issue-1059.geojson",
        )
    }

    @Test
    fun testScaleLine() {
        testScale("transformation/scale/in/line.geojson", "transformation/scale/out/line.geojson")
    }

    @Test
    fun testScaleMultiLine() {
        testScale(
            "transformation/scale/in/multiLine.geojson",
            "transformation/scale/out/multiLine.geojson",
        )
    }

    @Test
    fun testScaleMultiPoint() {
        testScale(
            "transformation/scale/in/multiPoint.geojson",
            "transformation/scale/out/multiPoint.geojson",
        )
    }

    @Test
    fun testScaleMultiPolygon() {
        testScale(
            "transformation/scale/in/multiPolygon.geojson",
            "transformation/scale/out/multiPolygon.geojson",
        )
    }

    @Test
    fun testScaleNoScale() {
        testScale(
            "transformation/scale/in/no-scale.geojson",
            "transformation/scale/out/no-scale.geojson",
        )
    }

    @Test
    fun testScaleOriginInsideBbox() {
        testScale(
            "transformation/scale/in/origin-inside-bbox.geojson",
            "transformation/scale/out/origin-inside-bbox.geojson",
        )
    }

    @Test
    fun testScaleOriginInsideFeature() {
        testScale(
            "transformation/scale/in/origin-inside-feature.geojson",
            "transformation/scale/out/origin-inside-feature.geojson",
        )
    }

    @Test
    fun testScaleOriginOutsideBbox() {
        testScale(
            "transformation/scale/in/origin-outside-bbox.geojson",
            "transformation/scale/out/origin-outside-bbox.geojson",
        )
    }

    @Test
    fun testScalePoint() {
        testScale("transformation/scale/in/point.geojson", "transformation/scale/out/point.geojson")
    }

    @Test
    fun testScalePolyDouble() {
        testScale(
            "transformation/scale/in/poly-double.geojson",
            "transformation/scale/out/poly-double.geojson",
        )
    }

    @Test
    fun testScalePolyHalf() {
        testScale(
            "transformation/scale/in/poly-half.geojson",
            "transformation/scale/out/poly-half.geojson",
        )
    }

    @Test
    fun testScalePolygon() {
        testScale(
            "transformation/scale/in/polygon.geojson",
            "transformation/scale/out/polygon.geojson",
        )
    }

    @Test
    fun testScalePolygonFiji() {
        testScale(
            "transformation/scale/in/polygon-fiji.geojson",
            "transformation/scale/out/polygon-fiji.geojson",
        )
    }

    @Test
    fun testScalePolygonResoluteBay() {
        testScale(
            "transformation/scale/in/polygon-resolute-bay.geojson",
            "transformation/scale/out/polygon-resolute-bay.geojson",
        )
    }

    @Test
    fun testScalePolygonWithHole() {
        testScale(
            "transformation/scale/in/polygon-with-hole.geojson",
            "transformation/scale/out/polygon-with-hole.geojson",
        )
    }

    @Test
    fun testScaleZScaling() {
        val inputFeature =
            Feature.fromJson<Geometry?, JsonObject?>(
                readResourceFile("transformation/scale/in/z-scaling.geojson")
            )
        val factor =
            inputFeature.properties?.get("factor")?.jsonPrimitive?.content?.toDoubleOrNull() ?: 2.0
        val scaled =
            testScale(
                "transformation/scale/in/z-scaling.geojson",
                "transformation/scale/out/z-scaling.geojson",
            )

        val inputPositions = inputFeature.geometry!!.flattenCoordinates()
        val actualPositions = scaled.flattenCoordinates()
        assertEquals(inputPositions.size, actualPositions.size)
        inputPositions.forEachIndexed { index, position ->
            assertDoubleEquals(position.altitude!! * factor, actualPositions[index].altitude)
        }
    }

    // A Point must scale around the origin like any other geometry (matching Turf.js
    // transformScale). The previous Point branch returned the point unchanged, ignoring an explicit
    // origin and skipping altitude scaling.
    @Test
    fun testScalePointWithExplicitOriginMovesThePoint() {
        val point = Point(longitude = 1.0, latitude = 1.0)
        val origin = Position(0.0, 0.0)
        val scaled = point.scale(2.0, ScaleOrigin.Coordinates(origin))
        val originalDistance = rhumbDistance(origin, point.coordinates)
        val scaledDistance = rhumbDistance(origin, scaled.coordinates)
        assertDoubleEquals(2.0, scaledDistance / originalDistance, 0.001)
    }

    // A GeometryCollection scales each member around the same origin instead of throwing.
    @Test
    fun testScaleGeometryCollection() {
        val origin = Position(0.0, 0.0)
        val point = Point(longitude = 1.0, latitude = 1.0)
        val line = LineString(Position(1.0, 1.0), Position(2.0, 2.0))
        val collection = GeometryCollection(point, line)
        val scaled = collection.scale(2.0, ScaleOrigin.Coordinates(origin)) as GeometryCollection<*>

        val originalPointDistance = rhumbDistance(origin, point.coordinates)
        val scaledPointDistance = rhumbDistance(origin, (scaled.geometries[0] as Point).coordinates)
        assertDoubleEquals(2.0, scaledPointDistance / originalPointDistance, 0.001)

        val originalLineDistance = rhumbDistance(origin, line.coordinates.last())
        val scaledLineDistance =
            rhumbDistance(origin, (scaled.geometries[1] as LineString).coordinates.last())
        assertDoubleEquals(2.0, scaledLineDistance / originalLineDistance, 0.001)
    }

    private fun testScale(path: String, expectedPath: String): Geometry {
        val feature = Feature.fromJson<Geometry?, JsonObject?>(readResourceFile(path))
        val expectedFc =
            FeatureCollection.fromJson<Geometry?, JsonObject?>(readResourceFile(expectedPath))
        val factor =
            feature.properties?.get("factor")?.jsonPrimitive?.content?.toDoubleOrNull() ?: 2.0
        val originString = (feature.properties?.get("origin") as? JsonPrimitive)?.contentOrNull
        val originPosition =
            (feature.properties?.get("origin") as? JsonArray)
                ?.let { Json.decodeFromJsonElement<List<Double>>(it) }
                ?.let { Position(it[0], it[1]) }
        val scaleOrigin =
            originString?.let { stringToScaledOrigin(it, null) }
                ?: originPosition?.let { stringToScaledOrigin("coordinates", it) }

        val scaledGeometry = feature.geometry!!.scale(factor, scaleOrigin ?: ScaleOrigin.Centroid)

        assertGeometryEquals(expectedFc.features.first().geometry!!, scaledGeometry, 0.000001)
        return scaledGeometry
    }

    private fun assertGeometryEquals(expected: Geometry, actual: Geometry, epsilon: Double) {
        val expectedPositions = expected.flattenCoordinates()
        val actualPositions = actual.flattenCoordinates()
        assertEquals(expectedPositions.size, actualPositions.size)
        expectedPositions.forEachIndexed { index, expectedPosition ->
            assertPositionEquals(expectedPosition, actualPositions[index], epsilon)
        }
    }

    private fun stringToScaledOrigin(value: String, position: Position?): ScaleOrigin =
        when (value) {
            "sw",
            "southwest",
            "bottomleft" -> ScaleOrigin.SouthWest
            "se",
            "southeast",
            "bottomright" -> ScaleOrigin.SouthEast
            "nw",
            "northwest",
            "topleft" -> ScaleOrigin.NorthWest
            "ne",
            "northeast",
            "topright" -> ScaleOrigin.NorthEast
            "center" -> ScaleOrigin.Center
            "centroid" -> ScaleOrigin.Centroid
            "coordinates" -> ScaleOrigin.Coordinates(position!!)
            else -> error("$value not applicable")
        }
}
