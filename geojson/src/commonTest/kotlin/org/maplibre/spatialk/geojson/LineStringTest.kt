package org.maplibre.spatialk.geojson

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.serialization.SerializationException
import org.maplibre.spatialk.geojson.utils.DELTA

class LineStringTest {

    @Test
    fun sanity() {
        val points = listOf(Position(1.0, 1.0), Position(2.0, 2.0), Position(3.0, 3.0))

        val lineString = LineString(points)
        assertNotNull(lineString)
    }

    @Test
    fun bbox_nullWhenNotSet() {
        val points = listOf(Position(1.0, 1.0), Position(2.0, 2.0), Position(3.0, 3.0))

        val lineString = LineString(points)
        assertNull(lineString.bbox)
    }

    @Test
    fun bbox_doesNotSerializeWhenNotPresent() {
        val points = listOf(Position(1.0, 1.0), Position(2.0, 2.0), Position(3.0, 3.0))

        val lineString = LineString(points)

        val actualLineString = LineString.fromJson(lineString.json())
        val expectedLineString =
            LineString.fromJson(
                """
            {
                "type": "LineString",
                "coordinates": [
                    [1.0, 1.0],
                    [2.0, 2.0],
                    [3.0, 3.0]
                ]
            }
            """
                    .trimIndent()
            )
        assertEquals(expectedLineString, actualLineString)
    }

    @Test
    fun bbox_returnsCorrectBbox() {
        val points = listOf(Position(1.0, 1.0), Position(2.0, 2.0), Position(3.0, 3.0))

        val bbox = BoundingBox(1.0, 2.0, 3.0, 4.0)
        val lineString = LineString(points, bbox)

        val actualBbox = lineString.bbox
        assertNotNull(actualBbox)
        assertEquals(1.0, actualBbox.west, DELTA)
        assertEquals(2.0, actualBbox.south, DELTA)
        assertEquals(3.0, actualBbox.east, DELTA)
        assertEquals(4.0, actualBbox.north, DELTA)
    }

    @Test
    fun bbox_doesSerializeWhenPresent() {
        val points = listOf(Position(1.0, 1.0), Position(2.0, 2.0), Position(3.0, 3.0))

        val bbox = BoundingBox(1.0, 2.0, 3.0, 4.0)
        val lineString = LineString(points, bbox)

        val actualLineString = LineString.fromJson(lineString.json())
        val expectedLineString =
            LineString.fromJson(
                """
            {
                "type": "LineString",
                "bbox": [1.0, 2.0, 3.0, 4.0],
                "coordinates": [
                    [1.0, 1.0],
                    [2.0, 2.0],
                    [3.0, 3.0]
                ]
            }
            """
                    .trimIndent()
            )

        assertEquals(expectedLineString, actualLineString)
    }

    @Test
    fun bbox_doesDeserializeWhenPresent() {
        val lineString =
            LineString.fromJson(
                """
            {
                "coordinates": [
                    [1, 2],
                    [2, 3],
                    [3, 4]
                ],
                "type": "LineString",
                "bbox": [1.0, 2.0, 3.0, 4.0]
            }
            """
                    .trimIndent()
            )

        assertNotNull(lineString)
        assertEquals(1.0, lineString.coordinates[0].longitude, DELTA)
        assertEquals(2.0, lineString.coordinates[0].latitude, DELTA)
        assertEquals(2.0, lineString.coordinates[1].longitude, DELTA)
        assertEquals(3.0, lineString.coordinates[1].latitude, DELTA)
        assertEquals(3.0, lineString.coordinates[2].longitude, DELTA)
        assertEquals(4.0, lineString.coordinates[2].latitude, DELTA)

        val bbox = lineString.bbox
        assertNotNull(bbox)
        assertEquals(1.0, bbox.southwest.longitude, DELTA)
        assertEquals(2.0, bbox.southwest.latitude, DELTA)
        assertEquals(3.0, bbox.northeast.longitude, DELTA)
        assertEquals(4.0, bbox.northeast.latitude, DELTA)
    }

    @Test
    fun fromJson() {
        val json =
            """
            {
                "type": "LineString",
                "coordinates": [
                    [100, 0],
                    [101, 1]
                ]
            }
            """
                .trimIndent()
        val geo: LineString = LineString.fromJson(json)
        assertEquals(geo.coordinates.first().longitude, 100.0, 0.0)
        assertEquals(geo.coordinates.first().latitude, 0.0, 0.0)
        assertNull(geo.coordinates.first().altitude)
    }

    @Test
    fun toJson() {
        val json =
            """
            {
                "type": "LineString",
                "coordinates": [
                    [100.0, 0.0],
                    [101.0, 1.0]
                ]
            }
            """
                .trimIndent()
        val geo: LineString = LineString.fromJson(json)
        val geoJsonString = geo.json()

        val actualLineString = LineString.fromJson(geoJsonString)
        val expectedLineString = LineString.fromJson(json)
        assertEquals(expectedLineString, actualLineString)
    }

    @Test
    fun fromJson_coordinatesNotPresent() {
        assertFailsWith(SerializationException::class) {
            LineString.fromJson(
                """
                {
                    "type": "LineString",
                    "coordinates": null
                }
                """
                    .trimIndent()
            )
        }
    }
}
