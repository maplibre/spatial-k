package org.maplibre.spatialk.turf.measurement

import kotlin.test.Test
import kotlinx.serialization.json.JsonObject
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.GeometryCollection
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Polygon
import org.maplibre.spatialk.testutil.assertDoubleEquals
import org.maplibre.spatialk.testutil.readResourceFile

class CentroidTest {

    @Test
    fun testCollection() {
        val geom = readGeometryCollection("measurement/centroid/in/feature-collection.geojson")
        val pos = geom.centroid().coordinates
        assertDoubleEquals(4.8336222767829895, pos.longitude, 0.000001)
        assertDoubleEquals(45.76051644154402, pos.latitude, 0.000001)
    }

    @Test
    fun testImbalancePolygon() {
        val polygon =
            Feature.fromJson<Polygon, JsonObject?>(
                    readResourceFile("measurement/centroid/in/imbalanced-polygon.geojson")
                )
                .geometry
        val pos = polygon.centroid().coordinates
        assertDoubleEquals(4.851791984156558, pos.longitude, 0.000001)
        assertDoubleEquals(45.78143055383553, pos.latitude, 0.000001)
    }

    @Test
    fun testLineString() {
        val lineString =
            Feature.fromJson<LineString, JsonObject?>(
                    readResourceFile("measurement/centroid/in/linestring.geojson")
                )
                .geometry
        val pos = lineString.centroid().coordinates
        assertDoubleEquals(4.860076904296875, pos.longitude, 0.000001)
        assertDoubleEquals(45.75919915723537, pos.latitude, 0.000001)
    }

    @Test
    fun testPoint() {
        val point =
            Feature.fromJson<Point, JsonObject?>(
                    readResourceFile("measurement/centroid/in/point.geojson")
                )
                .geometry
        val pos = point.centroid().coordinates
        assertDoubleEquals(4.831961989402771, pos.longitude, 0.000001)
        assertDoubleEquals(45.75764678012361, pos.latitude, 0.000001)
    }

    @Test
    fun testPolygon() {
        val polygon =
            Feature.fromJson<Polygon, JsonObject?>(
                    readResourceFile("measurement/centroid/in/polygon.geojson")
                )
                .geometry
        val pos = polygon.centroid().coordinates
        assertDoubleEquals(4.841194152832031, pos.longitude, 0.000001)
        assertDoubleEquals(45.75807143030368, pos.latitude, 0.000001)
    }

    private fun readGeometryCollection(path: String): org.maplibre.spatialk.geojson.Geometry {
        val fc =
            org.maplibre.spatialk.geojson.FeatureCollection.fromJson<
                org.maplibre.spatialk.geojson.Geometry,
                JsonObject?,
            >(
                readResourceFile(path)
            )
        return GeometryCollection(fc.features.mapNotNull { it.geometry })
    }
}
