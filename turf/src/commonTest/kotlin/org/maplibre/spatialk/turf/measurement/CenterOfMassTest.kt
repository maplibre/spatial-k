package org.maplibre.spatialk.turf.measurement

import kotlin.test.Test
import kotlinx.serialization.json.JsonObject
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Geometry
import org.maplibre.spatialk.geojson.GeometryCollection
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Polygon
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.testutil.assertDoubleEquals
import org.maplibre.spatialk.testutil.assertPositionEquals
import org.maplibre.spatialk.testutil.readResourceFile

class CenterOfMassTest {

    @Test
    fun testCalculateCenterOfMassForFeatureCollection() {
        val fc =
            FeatureCollection.fromJson<Geometry?, JsonObject?>(
                readResourceFile("measurement/centerofmass/in/feature-collection.geojson")
            )
        val expectedPoint =
            FeatureCollection.fromJson<Geometry?, JsonObject?>(
                    readResourceFile("measurement/centerofmass/out/feature-collection.geojson")
                )
                .first()
                .geometry as Point
        val geometryCollection = GeometryCollection(fc.features.mapNotNull { it.geometry })
        val centerOfMass = geometryCollection.centerOfMass()

        assertDoubleEquals(
            expectedPoint.coordinates.longitude,
            centerOfMass.coordinates.longitude,
            0.0001,
        )
        assertDoubleEquals(
            expectedPoint.coordinates.latitude,
            centerOfMass.coordinates.latitude,
            0.0001,
        )
    }

    @Test
    fun testCalculateCenterOfMassForImbalancedPolygon() {
        testCenterOfMass<Polygon>(
            "measurement/centerofmass/in/imbalanced-polygon.geojson",
            "measurement/centerofmass/out/imbalanced-polygon.geojson",
        )
    }

    @Test
    fun testCalculateCenterOfMassForLinestring() {
        testCenterOfMass<LineString>(
            "measurement/centerofmass/in/linestring.geojson",
            "measurement/centerofmass/out/linestring.geojson",
        )
    }

    @Test
    fun testCalculateCenterOfMassForPoint() {
        testCenterOfMass<Point>(
            "measurement/centerofmass/in/point.geojson",
            "measurement/centerofmass/out/point.geojson",
        )
    }

    @Test
    fun testCalculateCenterOfMassForPolygon() {
        testCenterOfMass<Polygon>(
            "measurement/centerofmass/in/polygon.geojson",
            "measurement/centerofmass/out/polygon.geojson",
        )
    }

    // The exterior ring's closing coordinate must not be paired with the first coordinate of the
    // hole ring; that phantom edge skewed the signed area and the center of mass for polygons with
    // holes.
    @Test
    fun testCalculateCenterOfMassForPolygonWithHole() {
        val polygon =
            Polygon(
                listOf(
                    listOf(
                        Position(0.0, 0.0),
                        Position(10.0, 0.0),
                        Position(10.0, 10.0),
                        Position(0.0, 10.0),
                        Position(0.0, 0.0),
                    ),
                    listOf(
                        Position(2.0, 2.0),
                        Position(2.0, 4.0),
                        Position(4.0, 4.0),
                        Position(4.0, 2.0),
                        Position(2.0, 2.0),
                    ),
                )
            )
        // (100 * (5,5) - 4 * (3,3)) / (100 - 4)
        val expected = Position(488.0 / 96.0, 488.0 / 96.0)
        val center = polygon.centerOfMass()
        assertPositionEquals(expected, center.coordinates, 0.0001)
    }

    private inline fun <reified T : Geometry> testCenterOfMass(input: String, expected: String) {
        val feature = Feature.fromJson<T, JsonObject?>(readResourceFile(input))
        val expectedPoint =
            FeatureCollection.fromJson<Geometry?, JsonObject?>(readResourceFile(expected))
                .first()
                .geometry as Point
        val centerOfMass = feature.geometry.centerOfMass()

        assertPositionEquals(expectedPoint.coordinates, centerOfMass.coordinates, 0.0001)
    }
}
