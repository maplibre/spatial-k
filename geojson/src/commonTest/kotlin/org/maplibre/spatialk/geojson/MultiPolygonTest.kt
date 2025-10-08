package org.maplibre.spatialk.geojson

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.serialization.SerializationException
import org.maplibre.spatialk.geojson.utils.DELTA

class MultiPolygonTest {

    @Test
    fun sanity() {
        val points =
            listOf(Position(1.0, 2.0), Position(2.0, 3.0), Position(3.0, 4.0), Position(1.0, 2.0))

        val outer = LineString(points)
        val polygons = arrayOf(Polygon(outer.coordinates), Polygon(outer.coordinates))
        val multiPolygon = MultiPolygon(*polygons)
        assertNotNull(multiPolygon)
    }

    @Test
    fun throwsInvalidPositionException() {
        assertFailsWith(IllegalArgumentException::class) {
            MultiPolygon(
                arrayOf(
                    arrayOf(
                        arrayOf(
                            doubleArrayOf(5.0, 2.0),
                            doubleArrayOf(1.0), // !
                            doubleArrayOf(4.0, 3.0),
                            doubleArrayOf(5.0, 2.0),
                        )
                    )
                )
            )
        }
    }

    @Test
    fun throwsEmptyPolygonException() {
        assertFailsWith(IllegalArgumentException::class) {
            MultiPolygon(listOf(listOf<List<Position>>()))
        }
    }

    @Test
    fun throwsNoRingException() {
        assertFailsWith(IllegalArgumentException::class) {
            MultiPolygon(
                listOf(listOf(Position(10.0, 2.0), Position(5.0, 2.0), Position(3.0, 2.0)))
            )
        }
    }

    @Test
    fun throwsRingNotClosedException() {
        assertFailsWith(IllegalArgumentException::class) {
            MultiPolygon(
                listOf(
                    listOf(
                        Position(10.0, 2.0),
                        Position(5.0, 2.0),
                        Position(3.0, 2.0),
                        Position(5.0, 2.0),
                    )
                )
            )
        }
    }

    @Test
    fun bbox_nullWhenNotSet() {
        val points =
            listOf(Position(1.0, 2.0), Position(2.0, 3.0), Position(3.0, 4.0), Position(1.0, 2.0))

        val outer = LineString(points)
        val polygons = arrayOf(Polygon(outer.coordinates), Polygon(outer.coordinates))
        val multiPolygon = MultiPolygon(*polygons)
        assertNull(multiPolygon.bbox)
    }

    @Test
    fun bbox_doesNotSerializeWhenNotPresent() {
        val points =
            listOf(Position(1.0, 2.0), Position(2.0, 3.0), Position(3.0, 4.0), Position(1.0, 2.0))

        val outer = LineString(points)
        val polygons = arrayOf(Polygon(outer.coordinates), Polygon(outer.coordinates))
        val multiPolygon = MultiPolygon(*polygons)

        val actualMultiPolygon = MultiPolygon.fromJson(multiPolygon.toJson())
        val expectedMultiPolygon =
            MultiPolygon.fromJson(
                """
                {
                    "type": "MultiPolygon",
                    "coordinates": [
                        [
                            [
                                [1.0, 2.0],
                                [2.0, 3.0],
                                [3.0, 4.0],
                                [1.0, 2.0]
                            ]
                        ],
                        [
                            [
                                [1.0, 2.0],
                                [2.0, 3.0],
                                [3.0, 4.0],
                                [1.0, 2.0]
                            ]
                        ]
                    ]
                }
                """
                    .trimIndent()
            )
        assertEquals(expectedMultiPolygon, actualMultiPolygon)
    }

    @Test
    fun bbox_returnsCorrectBbox() {
        val points =
            listOf(Position(1.0, 2.0), Position(2.0, 3.0), Position(3.0, 4.0), Position(1.0, 2.0))

        val outer = LineString(points)
        val polygons = arrayOf(Polygon(outer.coordinates), Polygon(outer.coordinates))
        val bbox = BoundingBox(1.0, 2.0, 3.0, 4.0)
        val multiPolygon = MultiPolygon(polygons = polygons, bbox)
        assertNotNull(multiPolygon.bbox)
        assertEquals(1.0, multiPolygon.bbox.west, DELTA)
        assertEquals(2.0, multiPolygon.bbox.south, DELTA)
        assertEquals(3.0, multiPolygon.bbox.east, DELTA)
        assertEquals(4.0, multiPolygon.bbox.north, DELTA)
    }

    @Test
    fun passingInSinglePolygon_doesHandleCorrectly() {
        val points =
            listOf(Position(1.0, 2.0), Position(3.0, 4.0), Position(5.0, 6.0), Position(1.0, 2.0))

        val polygon = Polygon(points)
        val multiPolygon = MultiPolygon(polygon)
        assertNotNull(multiPolygon)
        assertEquals(1, multiPolygon.coordinates.size)
        assertEquals(2.0, multiPolygon.coordinates.first().first().first().latitude, DELTA)
    }

    @Test
    fun bbox_doesSerializeWhenPresent() {
        val points =
            listOf(Position(1.0, 2.0), Position(2.0, 3.0), Position(3.0, 4.0), Position(1.0, 2.0))

        val outer = LineString(points)
        val polygons = arrayOf(Polygon(outer.coordinates), Polygon(outer.coordinates))
        val bbox = BoundingBox(1.0, 2.0, 3.0, 4.0)
        val multiPolygon = MultiPolygon(polygons = polygons, bbox)

        val actualMultiPolygon = MultiPolygon.fromJson(multiPolygon.toJson())
        val expectedMultiPolygon =
            MultiPolygon.fromJson(
                """
                {
                    "type": "MultiPolygon",
                    "bbox": [1.0, 2.0, 3.0, 4.0],
                    "coordinates": [
                        [
                            [
                                [1.0, 2.0],
                                [2.0, 3.0],
                                [3.0, 4.0],
                                [1.0, 2.0]
                            ]
                        ],
                        [
                            [
                                [1.0, 2.0],
                                [2.0, 3.0],
                                [3.0, 4.0],
                                [1.0, 2.0]
                            ]
                        ]
                    ]
                }
                """
                    .trimIndent()
            )
        assertEquals(expectedMultiPolygon, actualMultiPolygon)
    }

    @Test
    fun fromJson() {
        val json =
            """
            {
                "type": "MultiPolygon",
                "coordinates": [
                    [
                        [
                            [102, 2],
                            [103, 2],
                            [103, 3],
                            [102, 3],
                            [102, 2]
                        ]
                    ],
                    [
                        [
                            [100, 0],
                            [101, 0],
                            [101, 1],
                            [100, 1],
                            [100, 0]
                        ],
                        [
                            [100.2, 0.2],
                            [100.2, 0.8],
                            [100.8, 0.8],
                            [100.8, 0.2],
                            [100.2, 0.2]
                        ]
                    ]
                ]
            }
            """
                .trimIndent()
        val geo = MultiPolygon.fromJson(json)
        assertEquals(geo.coordinates.first().first().first().longitude, 102.0, DELTA)
        assertEquals(geo.coordinates.first().first().first().latitude, 2.0, DELTA)
        assertNull(geo.coordinates.first().first().first().altitude)
    }

    @Test
    fun toJson() {
        val json =
            """
            {
                "type": "MultiPolygon",
                "coordinates": [
                    [
                        [
                            [102.0, 2.0],
                            [103.0, 2.0],
                            [103.0, 3.0],
                            [102.0, 3.0],
                            [102.0, 2.0]
                        ]
                    ],
                    [
                        [
                            [100.0, 0.0],
                            [101.0, 0.0],
                            [101.0, 1.0],
                            [100.0, 1.0],
                            [100.0, 0.0]
                        ],
                        [
                            [100.2, 0.2],
                            [100.2, 0.8],
                            [100.8, 0.8],
                            [100.8, 0.2],
                            [100.2, 0.2]
                        ]
                    ]
                ]
            }
            """
                .trimIndent()

        val multiPolygon = MultiPolygon.fromJson(json)

        val actualMultiPolygon = MultiPolygon.fromJson(multiPolygon.toJson())
        val expectedMultiPolygon = MultiPolygon.fromJson(json)
        assertEquals(expectedMultiPolygon, actualMultiPolygon)
    }

    @Test
    fun fromJson_coordinatesPresent() {
        assertFailsWith(SerializationException::class) {
            MultiPolygon.fromJson(
                """
                {
                    "type": "MultiPolygon",
                    "coordinates": null
                }
                """
                    .trimIndent()
            )
        }
    }

    @Test
    fun wrongType() {
        assertIs<MultiPolygon>(
            MultiPolygon.fromJsonOrNull(
                """{"type":"MultiPolygon","coordinates":[[[[102.0,2.0],[103.0,2.0],[103.0,3.0],[102.0,3.0],[102.0,2.0]]]]}"""
            )
        )
        assertNull(
            MultiPolygon.fromJsonOrNull(
                """{"type":"Polygon","coordinates":[[[[102.0,2.0],[103.0,2.0],[103.0,3.0],[102.0,3.0],[102.0,2.0]]]]}"""
            )
        )
    }

    @Test
    fun missingType() {
        assertIs<MultiPolygon>(
            MultiPolygon.fromJsonOrNull(
                """{"type":"MultiPolygon","coordinates":[[[[102.0,2.0],[103.0,2.0],[103.0,3.0],[102.0,3.0],[102.0,2.0]]]]}"""
            )
        )
        assertNull(
            MultiPolygon.fromJsonOrNull(
                """{"coordinates":[[[[102.0,2.0],[103.0,2.0],[103.0,3.0],[102.0,3.0],[102.0,2.0]]]]}"""
            )
        )
    }
}
