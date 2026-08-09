package org.maplibre.spatialk.turf.measurement

import kotlin.test.Test
import kotlinx.serialization.json.JsonObject
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Geometry
import org.maplibre.spatialk.geojson.GeometryCollection
import org.maplibre.spatialk.geojson.MultiLineString
import org.maplibre.spatialk.geojson.MultiPoint
import org.maplibre.spatialk.geojson.MultiPolygon
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.testutil.assertPositionEquals
import org.maplibre.spatialk.testutil.readResourceFile

class PointOnFeatureTest {

    @Test
    fun testPointOnLine() {
        val fc =
            FeatureCollection.fromJson<Geometry?, JsonObject?>(
                readResourceFile("measurement/pointonfeature/in/lines.json")
            )
        val expectedPoint =
            FeatureCollection.fromJson<Geometry?, JsonObject?>(
                    readResourceFile("measurement/pointonfeature/out/lines.json")
                )
                .features
                .last()
                .geometry as Point
        val geometry = GeometryCollection(fc.features.mapNotNull { it.geometry })

        val point = geometry.pointOnFeature()

        assertPositionEquals(expectedPoint.coordinates, point.coordinates, 0.00001)
    }

    @Test
    fun testPointOnMultiLineString() {
        val feature =
            Feature.fromJson<MultiLineString, JsonObject?>(
                readResourceFile("measurement/pointonfeature/in/multiline.json")
            )
        val expectedPoint =
            FeatureCollection.fromJson<Geometry?, JsonObject?>(
                    readResourceFile("measurement/pointonfeature/out/multiline.json")
                )
                .features
                .last()
                .geometry as Point

        val point = feature.geometry.pointOnFeature()

        assertPositionEquals(expectedPoint.coordinates, point.coordinates, 0.00001)
    }

    @Test
    fun testPointOnMultiPoint() {
        val feature =
            Feature.fromJson<MultiPoint, JsonObject?>(
                readResourceFile("measurement/pointonfeature/in/multipoint.json")
            )
        val expectedPoint =
            FeatureCollection.fromJson<Geometry?, JsonObject?>(
                    readResourceFile("measurement/pointonfeature/out/multipoint.json")
                )
                .features
                .last()
                .geometry as Point

        val point = feature.geometry.pointOnFeature()

        assertPositionEquals(expectedPoint.coordinates, point.coordinates, 0.00001)
    }

    @Test
    fun testPointOnPolygon() {
        val fc =
            FeatureCollection.fromJson<Geometry?, JsonObject?>(
                readResourceFile("measurement/pointonfeature/in/polygons.json")
            )
        val expectedPoint =
            FeatureCollection.fromJson<Geometry?, JsonObject?>(
                    readResourceFile("measurement/pointonfeature/out/polygons.json")
                )
                .features
                .last()
                .geometry as Point
        val geometry = GeometryCollection(fc.features.mapNotNull { it.geometry })

        val point = geometry.pointOnFeature()

        assertPositionEquals(expectedPoint.coordinates, point.coordinates, 0.00001)
    }

    @Test
    fun testPointOnPolygonInCenter() {
        val fc =
            FeatureCollection.fromJson<Geometry?, JsonObject?>(
                readResourceFile("measurement/pointonfeature/in/polygon-in-center.json")
            )
        val expectedPoint =
            FeatureCollection.fromJson<Geometry?, JsonObject?>(
                    readResourceFile("measurement/pointonfeature/out/polygon-in-center.json")
                )
                .features
                .last()
                .geometry as Point
        val geometry = GeometryCollection(fc.features.mapNotNull { it.geometry })

        val point = geometry.pointOnFeature()

        assertPositionEquals(expectedPoint.coordinates, point.coordinates, 0.00001)
    }

    @Test
    fun testPointOnMultiPolygon() {
        val feature =
            Feature.fromJson<MultiPolygon, JsonObject?>(
                readResourceFile("measurement/pointonfeature/in/multipolygon.json")
            )
        val expectedPoint =
            FeatureCollection.fromJson<Geometry?, JsonObject?>(
                    readResourceFile("measurement/pointonfeature/out/multipolygon.json")
                )
                .features
                .last()
                .geometry as Point

        val point = feature.geometry.pointOnFeature()

        assertPositionEquals(expectedPoint.coordinates, point.coordinates, 0.00001)
    }
}
