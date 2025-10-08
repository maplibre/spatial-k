package org.maplibre.spatialk.geojson

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.serialization.SerializationException
import org.maplibre.spatialk.geojson.utils.DELTA

class MultiPointTest {

    @Test
    fun sanity() {
        val points = listOf(Position(1.0, 2.0), Position(2.0, 3.0))

        val multiPoint = MultiPoint(points)
        assertNotNull(multiPoint)
    }

    @Test
    fun throwsInvalidPositionException() {
        assertFailsWith(IllegalArgumentException::class) { MultiPoint(arrayOf(doubleArrayOf(1.0))) }
    }

    @Test
    fun bbox_nullWhenNotSet() {
        val points = listOf(Position(1.0, 2.0), Position(2.0, 3.0))

        val multiPoint = MultiPoint(points)
        assertNull(multiPoint.bbox)
    }

    @Test
    fun bbox_doesNotSerializeWhenNotPresent() {
        val points = listOf(Position(1.0, 2.0), Position(2.0, 3.0))

        val multiPoint = MultiPoint(points)

        val actualMultiPoint = MultiPoint.fromJson(multiPoint.toJson())
        val expectedMultiPoint =
            MultiPoint.fromJson(
                """
                {
                    "coordinates": [
                        [1.0, 2.0],
                        [2.0, 3.0]
                    ],
                    "type": "MultiPoint"
                }
                """
                    .trimIndent()
            )
        assertEquals(expectedMultiPoint, actualMultiPoint)
    }

    @Test
    fun bbox_returnsCorrectBbox() {
        val points = listOf(Position(1.0, 2.0), Position(2.0, 3.0))

        val bbox = BoundingBox(1.0, 2.0, 3.0, 4.0)
        val multiPoint = MultiPoint(points, bbox)
        assertNotNull(multiPoint.bbox)
        assertEquals(1.0, multiPoint.bbox.west, DELTA)
        assertEquals(2.0, multiPoint.bbox.south, DELTA)
        assertEquals(3.0, multiPoint.bbox.east, DELTA)
        assertEquals(4.0, multiPoint.bbox.north, DELTA)
    }

    @Test
    fun bbox_doesSerializeWhenPresent() {
        val points = listOf(Position(1.0, 2.0), Position(2.0, 3.0))

        val bbox = BoundingBox(1.0, 2.0, 3.0, 4.0)
        val multiPoint = MultiPoint(points, bbox)

        val actualMultiPoint = MultiPoint.fromJson(multiPoint.toJson())
        val expectedMultiPoint =
            MultiPoint.fromJson(
                """
                {
                    "coordinates": [
                        [1.0, 2.0],
                        [2.0, 3.0]
                    ],
                    "type": "MultiPoint",
                    "bbox": [1.0, 2.0, 3.0, 4.0]
                }
                """
                    .trimIndent()
            )
        assertEquals(expectedMultiPoint, actualMultiPoint)
    }

    @Test
    fun fromJson() {
        val json =
            """
                {
                    "type": "MultiPoint",
                    "coordinates": [
                        [100, 0],
                        [101, 1]
                    ]
                }
            """
                .trimIndent()
        val geo: MultiPoint = MultiPoint.fromJson(json)
        assertEquals(geo.coordinates.first().longitude, 100.0, DELTA)
        assertEquals(geo.coordinates.first().latitude, 0.0, DELTA)
        assertEquals(geo.coordinates[1].longitude, 101.0, DELTA)
        assertEquals(geo.coordinates[1].latitude, 1.0, DELTA)
        assertNull(geo.coordinates.first().altitude)
    }

    @Test
    fun toJson() {
        val json =
            """
            {
                "type": "MultiPoint",
                "coordinates": [
                    [100.0, 0.0],
                    [101.0, 1.0]
                ]
            }
            """
                .trimIndent()
        val geo: MultiPoint = MultiPoint.fromJson(json)

        val actualMultiPoint = MultiPoint.fromJson(geo.toJson())
        val expectedMultiPoint = MultiPoint.fromJson(json)
        assertEquals(expectedMultiPoint, actualMultiPoint)
    }

    @Test
    fun fromJson_coordinatesPresent() {
        assertFailsWith(SerializationException::class) {
            MultiPoint.fromJson(
                """
                {
                    "type": "MultiPoint",
                    "coordinates": null
                }
                """
                    .trimIndent()
            )
        }
    }

    @Test
    fun wrongType() {
        assertIs<MultiPoint>(
            MultiPoint.fromJsonOrNull("""{"type":"MultiPoint","coordinates":[[1.0,2.0]]}""")
        )
        assertNull(MultiPoint.fromJsonOrNull("""{"type":"LineString","coordinates":[[1.0,2.0]]}"""))
    }

    @Test
    fun missingType() {
        assertIs<MultiPoint>(
            MultiPoint.fromJsonOrNull("""{"type":"MultiPoint","coordinates":[[1.0,2.0]]}""")
        )
        assertNull(MultiPoint.fromJsonOrNull("""{"coordinates":[[1.0,2.0]]}"""))
    }
}
