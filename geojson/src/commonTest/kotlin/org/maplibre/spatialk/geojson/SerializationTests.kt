package org.maplibre.spatialk.geojson

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlinx.serialization.SerializationException

class SerializationTests {

    @Test
    fun testSerializePosition() {
        val position = Position(-75.1, 45.1)
        val result = position.toJson()
        assertEquals("[-75.1,45.1]", result)
        assertEquals("[-75.1,45.1]", position.toJson())

        val altitude = Position(60.2, 23.2354, 100.5)
        val altitudeResult = altitude.toJson()
        assertEquals("[60.2,23.2354,100.5]", altitudeResult)
        assertEquals("[60.2,23.2354,100.5]", altitude.toJson())

        val list = listOf(Position(12.3, 45.6), Position(78.9, 12.3))
        val listResult = GeoJson.jsonFormat.encodeToString(list)
        assertEquals("[[12.3,45.6],[78.9,12.3]]", listResult)
    }

    @Test
    fun testDeserializePosition() {
        val position = Position.fromJson("[32.4,54.1234]")
        assertEquals(Position(32.4, 54.1234), position)

        val withAltitude = Position.fromJson("[60.2,23.2354,100.5]")
        assertEquals(Position(60.2, 23.2354, 100.5), withAltitude)

        val list = GeoJson.jsonFormat.decodeFromString<List<Position>>("[[12.3,45.6],[78.9,12.3]]")

        assertEquals(listOf(Position(12.3, 45.6), Position(78.9, 12.3)), list)
    }

    @Test
    fun testSerializeBoundingBox() {
        val bbox = BoundingBox(Position(-10.5, -10.5), Position(10.5, 10.5))
        val result = bbox.toJson()
        assertEquals("[-10.5,-10.5,10.5,10.5]", result)
        assertEquals("[-10.5,-10.5,10.5,10.5]", bbox.toJson())

        val bbox3D = BoundingBox(Position(-10.5, -10.5, -100.8), Position(10.5, 10.5, 5.5))
        val result3D = bbox3D.toJson()
        assertEquals("[-10.5,-10.5,-100.8,10.5,10.5,5.5]", result3D)
        assertEquals("[-10.5,-10.5,-100.8,10.5,10.5,5.5]", bbox3D.toJson())

        // One altitude unspecified
        val bboxFake3D = BoundingBox(Position(-10.5, -10.5, -100.8), Position(10.5, 10.5))
        val fakeResult = bboxFake3D.toJson()
        assertEquals("[-10.5,-10.5,10.5,10.5]", fakeResult)
        assertEquals("[-10.5,-10.5,10.5,10.5]", bboxFake3D.toJson())
    }

    @Test
    fun testDeserializeBoundingBox() {
        val bbox = BoundingBox.fromJson("[-10.5,-10.5,10.5,10.5]")
        assertEquals(BoundingBox(Position(-10.5, -10.5), Position(10.5, 10.5)), bbox)

        val bbox3D = BoundingBox.fromJson("[-10.5,-10.5,-100.8,10.5,10.5,5.5]")
        assertEquals(BoundingBox(Position(-10.5, -10.5, -100.8), Position(10.5, 10.5, 5.5)), bbox3D)

        assertFailsWith<SerializationException> { BoundingBox.fromJson("[12.3]") }
    }

    // Geometries
    // Point
    @Test
    fun testSerializePoint() {
        val point = Point(Position(12.3, 45.6))
        val json = """{"type":"Point","coordinates":[12.3,45.6]}"""
        assertEquals(json, point.toJson(), "Point (fast)")
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
        assertEquals(json, multiPoint.toJson(), "MultiPoint (fast)")
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
        assertEquals(json, lineString.toJson(), "LineString (fast)")
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
        assertEquals(json, multiLineString.toJson(), "MultiLineString (fast)")
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

        assertEquals(json, polygon.toJson(), "Polygon (fast)")
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

        assertEquals(json, multiPolygon.toJson(), "MultiPolygon (fast)")
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

        assertEquals(json, collection.toJson(), "GeometryCollection")
        assertEquals(json, collection.toJson(), "GeometryCollection")
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
