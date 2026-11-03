package org.maplibre.spatialk.turf.coordinatemutation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.serialization.json.JsonObject
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Geometry
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.testutil.readResourceFile

class FlipTest {

    @Test
    fun testFeatureCollection() {
        val inputFc =
            FeatureCollection.fromJson<Geometry?, JsonObject?>(
                readResourceFile("coordinatemutation/flip/in/feature-collection-points.geojson")
            )
        val expectedFc =
            FeatureCollection.fromJson<Geometry?, JsonObject?>(
                readResourceFile("coordinatemutation/flip/out/feature-collection-points.geojson")
            )
        assertEquals(expectedFc, inputFc.flip())
    }

    @Test
    fun testFlipLineString() {
        val input =
            Feature.fromJson<Geometry?, JsonObject?>(
                    readResourceFile("coordinatemutation/flip/in/linestring.geojson")
                )
                .geometry!!
        val expected =
            Feature.fromJson<Geometry?, JsonObject?>(
                    readResourceFile("coordinatemutation/flip/out/linestring.geojson")
                )
                .geometry!!
        assertEquals(expected, input.flip())
    }

    @Test
    fun testFlipPointWithElevation() {
        val input =
            Feature.fromJson<Geometry?, JsonObject?>(
                    readResourceFile("coordinatemutation/flip/in/point-with-elevation.geojson")
                )
                .geometry!!
        val expected =
            Feature.fromJson<Geometry?, JsonObject?>(
                    readResourceFile("coordinatemutation/flip/out/point-with-elevation.geojson")
                )
                .geometry!!
        assertEquals(expected, input.flip())
    }

    @Test
    fun testFlipPolygon() {
        val input =
            Feature.fromJson<Geometry?, JsonObject?>(
                    readResourceFile("coordinatemutation/flip/in/polygon.geojson")
                )
                .geometry!!
        val expected =
            Feature.fromJson<Geometry?, JsonObject?>(
                    readResourceFile("coordinatemutation/flip/out/polygon.geojson")
                )
                .geometry!!
        assertEquals(expected, input.flip())
    }

    // Flipping coordinates invalidates a bbox (it would describe the unflipped coordinate order),
    // so it must be dropped from the features and the collection.
    @Test
    fun testFlipFeatureCollectionClearsBbox() {
        val bbox = BoundingBox(-10.0, 20.0, -10.0, 20.0)
        val feature = Feature<Point, JsonObject?>(Point(Position(-10.0, 20.0)), null, bbox = bbox)
        val collection = FeatureCollection<Point, JsonObject?>(feature, bbox = bbox)
        val flipped = collection.flip()
        assertNull(flipped.bbox)
        assertNull(flipped.first().bbox)
        assertEquals(Point(Position(20.0, -10.0)), flipped.first().geometry)
    }
}
