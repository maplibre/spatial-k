package org.maplibre.spatialk.geojson

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.serialization.SerializationException
import org.maplibre.spatialk.geojson.utils.DELTA

class PointTest {

    @Test
    fun sanity() {
        val point = Point(1.0, 2.0)
        assertNotNull(point)
    }

    @Test
    fun altitude_returnsIsOptional() {
        val point = Point(1.0, 2.0)
        assertNull(point.coordinates.altitude)
    }

    @Test
    fun longitude_doesReturnCorrectValue() {
        val point = Point(arrayOf(1.0, 2.0, 5.0).toDoubleArray())
        assertEquals(1.0, point.coordinates.longitude, DELTA)
    }

    @Test
    fun latitude_doesReturnCorrectValue() {
        val point = Point(arrayOf(1.0, 2.0, 5.0).toDoubleArray())
        assertEquals(2.0, point.coordinates.latitude, DELTA)
    }

    @Test
    fun bbox_nullWhenNotSet() {
        val point = Point(1.0, 2.0)
        assertNull(point.bbox)
    }

    @Test
    fun bbox_doesSerializeWhenNotPresent() {
        val point = Point(1.0, 2.0)

        val actualPoint = Point.fromJson(point.json())
        val expectedPoint =
            Point.fromJson(
                """
                {
                    "type": "Point",
                    "coordinates": [1.0, 2.0]
                }
            """
            )
        assertEquals(expectedPoint, actualPoint)
    }

    @Test
    fun bbox_returnsCorrectBbox() {
        val points = listOf(Position(1.0, 1.0), Position(2.0, 2.0), Position(3.0, 3.0))

        val bbox = BoundingBox(1.0, 2.0, 3.0, 4.0)
        val lineString = LineString(points, bbox)
        assertNotNull(lineString.bbox)
        assertEquals(1.0, lineString.bbox!!.west, DELTA)
        assertEquals(2.0, lineString.bbox!!.south, DELTA)
        assertEquals(3.0, lineString.bbox!!.east, DELTA)
        assertEquals(4.0, lineString.bbox!!.north, DELTA)
    }

    @Test
    fun bbox_doesSerializeWhenPresent() {
        val bbox = BoundingBox(1.0, 2.0, 3.0, 4.0)
        val point = Point(2.0, 2.0, bbox = bbox)

        val actualPoint = Point.fromJson(point.json())
        val expectedPoint =
            Point.fromJson(
                """
                        {
                            "coordinates": [2.0, 2.0],
                            "type": "Point",
                            "bbox": [1.0, 2.0, 3.0, 4.0]
                        }
                    """
            )
        assertEquals(expectedPoint, actualPoint)
    }

    @Test
    fun bbox_doesDeserializeWhenPresent() {
        val point: Point =
            Point.fromJson(
                """
                    {
                        "coordinates": [2, 3],
                        "type": "Point",
                        "bbox": [1.0, 2.0, 3.0, 4.0]
                    }
                    """
            )

        assertNotNull(point)
        assertEquals(2.0, point.coordinates.longitude, DELTA)
        assertEquals(3.0, point.coordinates.latitude, DELTA)
        assertNotNull(point.bbox)
        assertEquals(1.0, point.bbox!!.southwest.longitude, DELTA)
        assertEquals(2.0, point.bbox!!.southwest.latitude, DELTA)
        assertEquals(3.0, point.bbox!!.northeast.longitude, DELTA)
        assertEquals(4.0, point.bbox!!.northeast.latitude, DELTA)
    }

    @Test
    fun fromJson() {
        val json =
            """
                    {
                        "type": "Point",
                        "coordinates": [100, 0]
                    }
                    """
                .trimIndent()
        val geo: Point = Point.fromJson(json)

        assertEquals(geo.coordinates.longitude, 100.0, DELTA)
        assertEquals(geo.coordinates.latitude, 0.0, DELTA)
        assertNull(geo.coordinates.altitude)
    }

    @Test
    fun toJson() {
        val json =
            """
            {
                "type": "Point",
                "coordinates": [100.0, 0.0]
            }
            """
                .trimIndent()

        val actualPoint = Point.fromJson(Point.fromJson(json).json())
        val expectedPoint = Point.fromJson(json)
        assertEquals(expectedPoint, actualPoint)
    }

    @Test
    fun fromJson_coordinatesPresent() {
        assertFailsWith(SerializationException::class) {
            Point.fromJson(
                """
                    {
                        "type": "Point",
                        "coordinates": null
                    }
                """
            )
        }
    }
}
