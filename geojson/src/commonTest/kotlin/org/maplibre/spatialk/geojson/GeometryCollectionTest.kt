package org.maplibre.spatialk.geojson

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.maplibre.spatialk.geojson.dsl.buildGeometryCollection
import org.maplibre.spatialk.geojson.dsl.geometryCollectionOf
import org.maplibre.spatialk.geojson.dsl.lineStringOf
import org.maplibre.spatialk.geojson.dsl.multiPointOf
import org.maplibre.spatialk.geojson.utils.DELTA
import org.maplibre.spatialk.geojson.utils.assertJsonEquals

class GeometryCollectionTest {

    @Test
    fun sanity() {
        val points = listOf(Position(1.0, 2.0), Position(2.0, 3.0))

        val lineString = LineString(points)
        val geometries = listOf(Point(points[0]), lineString)

        val geometryCollection = GeometryCollection(geometries)
        assertNotNull(geometryCollection)
    }

    @Test
    fun bbox_nullWhenNotSet() {
        val points = listOf(Position(1.0, 2.0), Position(2.0, 3.0))

        val lineString = LineString(points)
        val geometries = listOf(Point(points[0]), lineString)

        val geometryCollection = GeometryCollection(geometries)
        assertNull(geometryCollection.bbox)
    }

    @Test
    fun bbox_doesNotSerializeWhenNotPresent() {
        val points = listOf(Position(1.0, 2.0), Position(2.0, 3.0))

        val lineString = LineString(points)
        val geometries = listOf(Point(points[0]), lineString)

        val geometryCollection = GeometryCollection(geometries)

        val actualGeometryCollection = GeometryCollection.fromJson(geometryCollection.toJson())
        val expectedGeometryCollection =
            GeometryCollection.fromJson(
                """
            {
                "type": "GeometryCollection",
                "geometries": [
                    {
                        "type": "Point",
                        "coordinates": [1.0, 2.0]
                    },
                    {
                        "type": "LineString",
                        "coordinates": [
                            [1.0, 2.0],
                            [2.0, 3.0]
                        ]
                    }
                ]
            }
            """
            )
        assertEquals(expectedGeometryCollection, actualGeometryCollection)
    }

    @Test
    fun bbox_returnsCorrectBbox() {
        val points = listOf(Position(1.0, 2.0), Position(2.0, 3.0))

        val lineString = LineString(points)
        val geometries = listOf(Point(points[0]), lineString)

        val bbox = BoundingBox(1.0, 2.0, 3.0, 4.0)
        val geometryCollection = GeometryCollection(geometries, bbox)
        val actualBbox = geometryCollection.bbox
        assertNotNull(actualBbox)
        assertEquals(1.0, actualBbox.west, DELTA)
        assertEquals(2.0, actualBbox.south, DELTA)
        assertEquals(3.0, actualBbox.east, DELTA)
        assertEquals(4.0, actualBbox.north, DELTA)
    }

    @Test
    fun passingInSingleGeometry_doesHandleCorrectly() {
        val geometry = Point(1.0, 2.0)
        val collection = geometryCollectionOf(geometry)
        assertNotNull(collection)
        assertEquals(1, collection.geometries.size)
        assertEquals(2.0, collection.geometries.first().coordinates.latitude, DELTA)
    }

    @Test
    fun bbox_doesSerializeWhenPresent() {
        val points = listOf(Position(1.0, 2.0), Position(2.0, 3.0))

        val lineString = LineString(points)
        val geometries = listOf(Point(points[0]), lineString)

        val bbox = BoundingBox(1.0, 2.0, 3.0, 4.0)
        val geometryCollection = GeometryCollection(geometries, bbox)

        val actualGeometryCollection = GeometryCollection.fromJson(geometryCollection.toJson())
        val expectedGeometryCollection =
            GeometryCollection.fromJson(
                """
            {
                "type": "GeometryCollection",
                "bbox": [1.0, 2.0, 3.0, 4.0],
                "geometries": [
                    {
                        "type": "Point",
                        "coordinates": [1.0, 2.0]
                    },
                    {
                        "type": "LineString",
                        "coordinates": [
                            [1.0, 2.0],
                            [2.0, 3.0]
                        ]
                    }
                ]
            }
            """
            )
        assertEquals(expectedGeometryCollection, actualGeometryCollection)
    }

    @Test
    fun fromJson() {
        val json =
            """
            {
                "type": "GeometryCollection",
                "geometries": [
                    {
                        "type": "Point",
                        "coordinates": [100, 0],
                        "bbox": [110, 30, -110, -30]
                    },
                    {
                        "type": "LineString",
                        "coordinates": [
                            [101, 0],
                            [102, 1]
                        ],
                        "bbox": [110, 30, -110, -30]
                    }
                ],
                "bbox": [ 120, 40, -120, -40]
            }
            """
                .trimIndent()
        val geo = GeometryCollection.fromJson(json)
        assertTrue(geo.geometries.first() is Point)
        assertTrue(geo.geometries[1] is LineString)
    }

    @Test
    fun toJson() {
        val jsonOriginal =
            """
            {
                "type": "GeometryCollection",
                "bbox": [-120.0, -40.0, 120.0, 40.0],
                "geometries": [
                    {
                        "type": "Point",
                        "bbox": [-110.0, -30.0, 110.0, 30.0],
                        "coordinates": [100.0, 0.0]
                    },
                    {
                        "type": "LineString",
                        "bbox": [-110.0, -30.0, 110.0, 30.0],
                        "coordinates": [
                            [101.0, 0.0],
                            [102.0, 1.0]
                        ]
                    }
                ]
            }
            """
        val geometries =
            listOf(
                Point(100.0, 0.0, bbox = BoundingBox(-110.0, -30.0, 110.0, 30.0)),
                LineString(
                    listOf(Position(101.0, 0.0), Position(102.0, 1.0)),
                    BoundingBox(-110.0, -30.0, 110.0, 30.0),
                ),
            )

        val geometryCollection =
            GeometryCollection(geometries, BoundingBox(-120.0, -40.0, 120.0, 40.0))

        val actualGeometryCollection = GeometryCollection.fromJson(geometryCollection.toJson())
        val expectedGeometryCollection = GeometryCollection.fromJson(jsonOriginal)
        assertEquals(expectedGeometryCollection, actualGeometryCollection)
    }

    @Test
    fun wrongType() {
        assertNull(
            GeometryCollection.fromJsonOrNull(
                """
            {
                "type": "FakeGeometryCollection",
                "geometries": [
                    {
                        "type": "Point",
                        "coordinates": [1.0, 2.0]
                    },
                    {
                        "type": "LineString",
                        "coordinates": [
                            [1.0, 2.0],
                            [2.0, 3.0]
                        ]
                    }
                ]
            }
            """
            )
        )
    }

    @Test
    fun missingType() {
        assertNull(
            GeometryCollection.fromJsonOrNull(
                """
            {
                "geometries": [
                    {
                        "type": "Point",
                        "coordinates": [1.0, 2.0]
                    },
                    {
                        "type": "LineString",
                        "coordinates": [
                            [1.0, 2.0],
                            [2.0, 3.0]
                        ]
                    }
                ]
            }
            """
            )
        )
    }

    @Test
    fun testEmptyCollection() {
        val json = "{\"type\": \"GeometryCollection\", \"geometries\": []}"
        val gc = geometryCollectionOf<Geometry>()
        assertEquals(gc, GeometryCollection.fromJsonOrNull<Geometry>(json))
        assertJsonEquals(json, gc.toJson())
    }

    @Test
    fun testMixedCollection() {
        val json =
            """
            {
                "type": "GeometryCollection",
                "geometries": [
                    {"type": "Point", "coordinates": [1.1, 2.2]},
                    {"type": "LineString", "coordinates": [[1.1, 2.2], [3.3, 4.4]]}
                ]
            }
            """

        val gc = buildGeometryCollection {
            add(Point(1.1, 2.2))
            add(lineStringOf(Position(1.1, 2.2), Position(3.3, 4.4)))
        }

        assertEquals(gc, GeometryCollection.fromJsonOrNull<Geometry>(json))
        assertNull(GeometryCollection.fromJsonOrNull<Point>(json))

        assertJsonEquals(json, gc.toJson())
    }

    @Test
    fun testHomogenousCollection() {
        val json =
            """
            {
                "type": "GeometryCollection",
                "geometries": [
                    {"type": "MultiPoint", "coordinates": [[1.1, 2.2], [1.1, 2.2]]},
                    {"type": "MultiPoint", "coordinates": [[3.3, 4.4], [3.3, 4.4]]}
                ]
            }
            """

        val gc = buildGeometryCollection {
            add(multiPointOf(Position(1.1, 2.2), Position(1.1, 2.2)))
            add(multiPointOf(Position(3.3, 4.4), Position(3.3, 4.4)))
        }

        assertEquals(gc, GeometryCollection.fromJsonOrNull<Geometry>(json))
        assertEquals(gc, GeometryCollection.fromJsonOrNull<MultiPoint>(json))
        assertNull(GeometryCollection.fromJsonOrNull<LineString>(json))

        assertJsonEquals(json, gc.toJson())
    }
}
