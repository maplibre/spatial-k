package org.maplibre.spatialk.geojson

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.maplibre.spatialk.geojson.serialization.GeoJson

@Suppress("MagicNumber", "TooManyFunctions")
class SerializationTests {

    @Test
    fun testSerializePosition() {
        val position = Position(-75.1, 45.1)
        val result = GeoJson.encodeToString(Position.serializer(), position)
        assertEquals("[-75.1,45.1]", result)
        assertEquals("[-75.1,45.1]", position.json())

        val altitude = Position(60.2, 23.2354, 100.5)
        val altitudeResult = GeoJson.encodeToString(Position.serializer(), altitude)
        assertEquals("[60.2,23.2354,100.5]", altitudeResult)
        assertEquals("[60.2,23.2354,100.5]", altitude.json())

        val list = listOf(Position(12.3, 45.6), Position(78.9, 12.3))
        val listResult = GeoJson.encodeToString(ListSerializer(Position.serializer()), list)
        assertEquals("[[12.3,45.6],[78.9,12.3]]", listResult)
    }

    @Test
    fun testDeserializePosition() {
        val position = GeoJson.decodeFromString(Position.serializer(), "[32.4,54.1234]")
        assertEquals(Position(32.4, 54.1234), position)

        val altitude = GeoJson.decodeFromString(Position.serializer(), "[60.2,23.2354,100.5]")
        assertEquals(Position(60.2, 23.2354, 100.5), altitude)

        val list =
            Json.decodeFromString(
                ListSerializer(Position.serializer()),
                "[[12.3,45.6],[78.9,12.3]]",
            )
        assertEquals(listOf(Position(12.3, 45.6), Position(78.9, 12.3)), list)
    }

    @Test
    fun testSerializeBoundingBox() {
        val bbox = BoundingBox(Position(-10.5, -10.5), Position(10.5, 10.5))
        val result = GeoJson.encodeToString(BoundingBox.serializer(), bbox)
        assertEquals("[-10.5,-10.5,10.5,10.5]", result)
        assertEquals("[-10.5,-10.5,10.5,10.5]", bbox.json())

        val bbox3D = BoundingBox(Position(-10.5, -10.5, -100.8), Position(10.5, 10.5, 5.5))
        val result3D = GeoJson.encodeToString(BoundingBox.serializer(), bbox3D)
        assertEquals("[-10.5,-10.5,-100.8,10.5,10.5,5.5]", result3D)
        assertEquals("[-10.5,-10.5,-100.8,10.5,10.5,5.5]", bbox3D.json())

        // One altitude unspecified
        val bboxFake3D = BoundingBox(Position(-10.5, -10.5, -100.8), Position(10.5, 10.5))
        val fakeResult = GeoJson.encodeToString(BoundingBox.serializer(), bboxFake3D)
        assertEquals("[-10.5,-10.5,10.5,10.5]", fakeResult)
        assertEquals("[-10.5,-10.5,10.5,10.5]", bboxFake3D.json())
    }

    @Test
    fun testDeserializeBoundingBox() {
        val bbox = GeoJson.decodeFromString(BoundingBox.serializer(), "[-10.5,-10.5,10.5,10.5]")
        assertEquals(BoundingBox(Position(-10.5, -10.5), Position(10.5, 10.5)), bbox)

        val bbox3D =
            Json.decodeFromString(BoundingBox.serializer(), "[-10.5,-10.5,-100.8,10.5,10.5,5.5]")
        assertEquals(BoundingBox(Position(-10.5, -10.5, -100.8), Position(10.5, 10.5, 5.5)), bbox3D)

        assertFailsWith<SerializationException> {
            Json.decodeFromString(BoundingBox.serializer(), "[12.3]")
        }
    }

    // Geometries
    // Point
    @Test
    fun testSerializePoint() {
        val point = Point(Position(12.3, 45.6))
        val json = """{"type":"Point","coordinates":[12.3,45.6]}"""
        assertEquals(json, point.json(), "Point (fast)")
        assertEquals(json, GeoJson.encodeToString(point), "Point (kotlinx)")
    }

    @Test
    fun testDeserializePoint() {
        val json = """{"type":"Point","coordinates":[12.3,45.6]}"""
        assertEquals(Point(Position(12.3, 45.6)), Point.fromJson(json))

        assertNull(Point.fromJsonOrNull("""{"type":"MultiPoint","coordinates":[12.3,45.6]}"""))
        assertNull(
            Point.fromJsonOrNull(
                """{"type":"MultiPoint","coordinates":[[12.3,45.6],[78.9,12.3]]}"""
            )
        )
    }

    // MultiPoint
    @Test
    fun testSerializeMultiPoint() {
        val multiPoint = MultiPoint(Position(12.3, 45.6), Position(78.9, 12.3))
        val json = """{"type":"MultiPoint","coordinates":[[12.3,45.6],[78.9,12.3]]}"""
        assertEquals(json, multiPoint.json(), "MultiPoint (fast)")
        assertEquals(json, GeoJson.encodeToString(multiPoint), "MultiPoint (kotlinx)")
    }

    @Test
    fun testDeserializeMultiPoint() {
        val json = """{"type":"MultiPoint","coordinates":[[12.3,45.6],[78.9,12.3]]}"""
        assertEquals(
            MultiPoint(Position(12.3, 45.6), Position(78.9, 12.3)),
            MultiPoint.fromJson(json),
        )
    }

    // LineString
    @Test
    fun testSerializeLineString() {
        val lineString = LineString(Position(12.3, 45.6), Position(78.9, 12.3))
        val json = """{"type":"LineString","coordinates":[[12.3,45.6],[78.9,12.3]]}"""
        assertEquals(json, lineString.json(), "LineString (fast)")
        assertEquals(json, GeoJson.encodeToString(lineString), "LineString (kotlinx)")
    }

    @Test
    fun testDeserializeLineString() {
        val lineString =
            LineString.fromJson("""{"type":"LineString","coordinates":[[12.3,45.6],[78.9,12.3]]}""")
        assertEquals(
            LineString(Position(12.3, 45.6), Position(78.9, 12.3)),
            lineString,
            "LineString",
        )
    }

    // MultiLineString
    @Test
    fun testSerializeMultiLineString() {
        val multiLineString =
            MultiLineString(
                listOf(Position(12.3, 45.6), Position(78.9, 12.3)),
                listOf(Position(87.6, 54.3), Position(21.9, 56.4)),
            )
        val json =
            """{"type":"MultiLineString","coordinates":[[[12.3,45.6],[78.9,12.3]],[[87.6,54.3],[21.9,56.4]]]}"""
        assertEquals(json, multiLineString.json(), "MultiLineString (fast)")
        assertEquals(json, GeoJson.encodeToString(multiLineString), "MultiLineString (kotlinx)")
    }

    @Test
    @Suppress("MaxLineLength")
    fun testDeserializeMultiLineString() {
        val multiLineString =
            MultiLineString.fromJson(
                """{"type":"MultiLineString","coordinates":[[[12.3,45.6],[78.9,12.3]],[[87.6,54.3],[21.9,56.4]]]}"""
            )
        assertEquals(
            MultiLineString(
                listOf(Position(12.3, 45.6), Position(78.9, 12.3)),
                listOf(Position(87.6, 54.3), Position(21.9, 56.4)),
            ),
            multiLineString,
            "MultiLineString",
        )
    }

    // Polygon
    @Test
    fun testSerializePolygon() {
        val polygon =
            Polygon(
                listOf(
                    Position(-79.87, 43.42),
                    Position(-78.89, 43.49),
                    Position(-79.07, 44.02),
                    Position(-79.95, 43.87),
                    Position(-79.87, 43.42),
                ),
                listOf(
                    Position(-79.75, 43.81),
                    Position(-79.56, 43.85),
                    Position(-79.7, 43.88),
                    Position(-79.75, 43.81),
                ),
            )
        val json =
            """
            |{"type":"Polygon","coordinates":[[[-79.87,43.42],[-78.89,43.49],[-79.07,44.02],[-79.95,43.87],
            |[-79.87,43.42]],[[-79.75,43.81],[-79.56,43.85],[-79.7,43.88],[-79.75,43.81]]]}"""
                .trimMargin()
                .replace("\n", "")

        assertEquals(json, polygon.json(), "Polygon (fast)")
        assertEquals(json, GeoJson.encodeToString(polygon), "Polygon (kotlinx)")
    }

    @Test
    fun testDeserializePolygon() {
        val polygon =
            Polygon.fromJson(
                """
                |{"type":"Polygon","coordinates":[[[-79.87,43.42],[-78.89,43.49],[-79.07,44.02],[-79.95,43.87],
                |[-79.87,43.42]],[[-79.75,43.81],[-79.56,43.85],[-79.7,43.88],[-79.75,43.81]]]}"""
                    .trimMargin()
                    .replace("\n", "")
            )

        assertEquals(
            Polygon(
                listOf(
                    Position(-79.87, 43.42),
                    Position(-78.89, 43.49),
                    Position(-79.07, 44.02),
                    Position(-79.95, 43.87),
                    Position(-79.87, 43.42),
                ),
                listOf(
                    Position(-79.75, 43.81),
                    Position(-79.56, 43.85),
                    Position(-79.7, 43.88),
                    Position(-79.75, 43.81),
                ),
            ),
            polygon,
            "Polygon",
        )
    }

    // MultiPolygon
    @Test
    fun testSerializeMultiPolygon() {
        val multiPolygon =
            MultiPolygon(
                listOf(
                    listOf(
                        Position(-79.87, 43.42),
                        Position(-78.89, 43.49),
                        Position(-79.07, 44.02),
                        Position(-79.95, 43.87),
                        Position(-79.87, 43.42),
                    ),
                    listOf(
                        Position(-79.75, 43.81),
                        Position(-79.56, 43.85),
                        Position(-79.7, 43.88),
                        Position(-79.75, 43.81),
                    ),
                ),
                listOf(
                    listOf(
                        Position(-79.87, 43.42),
                        Position(-78.89, 43.49),
                        Position(-79.07, 44.02),
                        Position(-79.95, 43.87),
                        Position(-79.87, 43.42),
                    ),
                    listOf(
                        Position(-79.75, 43.81),
                        Position(-79.56, 43.85),
                        Position(-79.7, 43.88),
                        Position(-79.75, 43.81),
                    ),
                ),
            )
        val json =
            """
            |{"type":"MultiPolygon","coordinates":[[[[-79.87,43.42],[-78.89,43.49],[-79.07,44.02],[-79.95,43.87],
            |[-79.87,43.42]],[[-79.75,43.81],[-79.56,43.85],[-79.7,43.88],[-79.75,43.81]]],[[[-79.87,43.42],
            |[-78.89,43.49],[-79.07,44.02],[-79.95,43.87],[-79.87,43.42]],[[-79.75,43.81],[-79.56,43.85],[-79.7,43.88],
            |[-79.75,43.81]]]]}"""
                .trimMargin()
                .replace("\n", "")

        assertEquals(json, multiPolygon.json(), "MultiPolygon (fast)")
        assertEquals(json, GeoJson.encodeToString(multiPolygon), "MultiPolygon (kotlinx)")
    }

    @Test
    @Suppress("MaxLineLength")
    fun testDeserializeMultiPolygon() {
        val multiPolygon =
            MultiPolygon.fromJson(
                """
                |{"type":"MultiPolygon","coordinates":[[[[-79.87,43.42],[-78.89,43.49],[-79.07,44.02],[-79.95,43.87],
                |[-79.87,43.42]],[[-79.75,43.81],[-79.56,43.85],[-79.7,43.88],[-79.75,43.81]]],[[[-79.87,43.42],
                |[-78.89,43.49],[-79.07,44.02],[-79.95,43.87],[-79.87,43.42]],[[-79.75,43.81],[-79.56,43.85],[-79.7,43.88],
                |[-79.75,43.81]]]]}"""
                    .trimMargin()
                    .replace("\n", "")
            )

        assertEquals(
            MultiPolygon(
                listOf(
                    listOf(
                        Position(-79.87, 43.42),
                        Position(-78.89, 43.49),
                        Position(-79.07, 44.02),
                        Position(-79.95, 43.87),
                        Position(-79.87, 43.42),
                    ),
                    listOf(
                        Position(-79.75, 43.81),
                        Position(-79.56, 43.85),
                        Position(-79.7, 43.88),
                        Position(-79.75, 43.81),
                    ),
                ),
                listOf(
                    listOf(
                        Position(-79.87, 43.42),
                        Position(-78.89, 43.49),
                        Position(-79.07, 44.02),
                        Position(-79.95, 43.87),
                        Position(-79.87, 43.42),
                    ),
                    listOf(
                        Position(-79.75, 43.81),
                        Position(-79.56, 43.85),
                        Position(-79.7, 43.88),
                        Position(-79.75, 43.81),
                    ),
                ),
            ),
            multiPolygon,
            "MultiPolygon",
        )
    }

    // GeometryCollection
    @Test
    fun testSerializeGeometryCollection() {
        val point = Point(Position(12.3, 45.6))
        val multiPoint = MultiPoint(Position(12.3, 45.6), Position(78.9, 12.3))

        val collection = GeometryCollection(point, multiPoint)
        val json =
            """
            |{"type":"GeometryCollection","geometries":[{"type":"Point","coordinates":[12.3,45.6]},
            |{"type":"MultiPoint","coordinates":[[12.3,45.6],[78.9,12.3]]}]}"""
                .trimMargin()
                .replace("\n", "")

        assertEquals(json, collection.json(), "GeometryCollection")
        assertEquals(json, collection.json(), "GeometryCollection")
    }

    @Test
    fun testDeserializeGeometryCollection() {
        val point = Point(Position(12.3, 45.6))
        val multiPoint = MultiPoint(Position(12.3, 45.6), Position(78.9, 12.3))

        val collection =
            GeometryCollection.fromJson(
                """
                |{"type":"GeometryCollection","geometries":[{"type":"Point","coordinates":[12.3,45.6]},
                |{"type":"MultiPoint","coordinates":[[12.3,45.6],[78.9,12.3]]}]}"""
                    .trimMargin()
                    .replace("\n", "")
            )

        assertEquals(GeometryCollection(point, multiPoint), collection, "GeometryCollection")
    }
}
