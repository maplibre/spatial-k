package org.maplibre.spatialk.geojson

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.serialization.SerializationException
import org.maplibre.spatialk.geojson.utils.DELTA

class MultiLineStringTest {

    @Test
    fun sanity() {
        val points = arrayOf(Position(1.0, 2.0), Position(2.0, 3.0))

        val lineStrings = arrayOf(LineString(*points), LineString(*points))

        val multiLineString = MultiLineString(*lineStrings)
        assertNotNull(multiLineString)
    }

    @Test
    fun throwsInvalidLineStringException() {
        assertFailsWith(IllegalArgumentException::class) {
            MultiLineString(listOf(Position(1.0, 1.0)))
        }
    }

    @Test
    fun throwsInvalidPositionException() {
        assertFailsWith(IllegalArgumentException::class) {
            MultiLineString(
                arrayOf(
                    arrayOf(
                        doubleArrayOf(1.0, 1.0),
                        doubleArrayOf(1.0), // !
                    )
                )
            )
        }
    }

    @Test
    fun bbox_nullWhenNotSet() {
        val points = listOf(Position(1.0, 2.0), Position(2.0, 3.0))

        val lineStrings = arrayOf(LineString(points), LineString(points))

        val multiLineString = MultiLineString(*lineStrings)
        assertNull(multiLineString.bbox)
    }

    @Test
    fun bbox_doesNotSerializeWhenNotPresent() {
        val points = listOf(Position(1.0, 2.0), Position(2.0, 3.0))

        val lineStrings = arrayOf(LineString(points), LineString(points))

        val multiLineString = MultiLineString(*lineStrings)

        val actualMultiLineString = MultiLineString.fromJson(multiLineString.toJson())
        val expectedMultiLineString =
            MultiLineString.fromJson(
                """
                {
                    "type": "MultiLineString",
                    "coordinates": [
                        [
                            [1.0, 2.0],
                            [2.0, 3.0]
                        ],
                        [
                            [1.0, 2.0],
                            [2.0, 3.0]
                        ]
                    ]
                }
                """
                    .trimIndent()
            )
        assertEquals(expectedMultiLineString, actualMultiLineString)
    }

    @Test
    fun bbox_returnsCorrectBbox() {
        val points = listOf(Position(1.0, 2.0), Position(2.0, 3.0))

        val bbox = BoundingBox(1.0, 2.0, 3.0, 4.0)

        val lineStrings = arrayOf(LineString(points), LineString(points))

        val multiLineString = MultiLineString(lineStrings = lineStrings, bbox)
        val actualBbox = multiLineString.bbox
        assertNotNull(actualBbox)
        assertEquals(1.0, actualBbox.west, DELTA)
        assertEquals(2.0, actualBbox.south, DELTA)
        assertEquals(3.0, actualBbox.east, DELTA)
        assertEquals(4.0, actualBbox.north, DELTA)
    }

    @Test
    fun passingInSingleLineString_doesHandleCorrectly() {
        val points = listOf(Position(1.0, 2.0), Position(3.0, 4.0))

        val geometry = LineString(points)
        val multiLineString = MultiLineString(geometry)

        assertNotNull(multiLineString)
        assertEquals(1, multiLineString.coordinates.size)
        assertEquals(1.0, multiLineString.coordinates[0][0].longitude, DELTA)
        assertEquals(2.0, multiLineString.coordinates[0][0].latitude, DELTA)
        assertEquals(3.0, multiLineString.coordinates[0][1].longitude, DELTA)
        assertEquals(4.0, multiLineString.coordinates[0][1].latitude, DELTA)
    }

    @Test
    fun bbox_doesSerializeWhenPresent() {
        val points = listOf(Position(1.0, 2.0), Position(2.0, 3.0))
        val bbox = BoundingBox(1.0, 2.0, 3.0, 4.0)

        val lineStrings = arrayOf(LineString(points), LineString(points))

        val multiLineString = MultiLineString(lineStrings = lineStrings, bbox)

        val actualMultiLineString = MultiLineString.fromJson(multiLineString.toJson())
        val expectedMultiLineString =
            MultiLineString.fromJson(
                """
                {
                    "type": "MultiLineString",
                    "bbox": [1.0, 2.0, 3.0, 4.0],
                    "coordinates": [
                        [
                            [1.0, 2.0],
                            [2.0, 3.0]
                        ],
                        [
                            [1.0, 2.0],
                            [2.0, 3.0]
                        ]
                    ]
                }
                """
                    .trimIndent()
            )
        assertEquals(expectedMultiLineString, actualMultiLineString)
    }

    @Test
    fun fromJson() {
        val json =
            """
            {
                "type": "MultiLineString",
                "coordinates": [
                    [
                        [100.0, 0.0],
                        [101.0, 1.0]
                    ],
                    [
                        [102.0, 2.0],
                        [103.0, 3.0]
                    ]
                ]
            }
            """
                .trimIndent()

        val geo: MultiLineString = MultiLineString.fromJson(json)
        assertEquals(geo.coordinates[0][0].longitude, 100.0, DELTA)
        assertEquals(geo.coordinates[0][0].latitude, 0.0, DELTA)
        assertNull(geo.coordinates[0][0].altitude)
    }

    @Test
    fun toJson() {
        val json =
            """
            {
                "type": "MultiLineString",
                "coordinates": [
                    [
                        [100.0, 0.0],
                        [101.0, 1.0]
                    ],
                    [
                        [102.0, 2.0],
                        [103.0, 3.0]
                    ]
                ]
            }
            """
                .trimIndent()
        val geo = MultiLineString.fromJson(json)

        val actualMultiLineString = MultiLineString.fromJson(geo.toJson())
        val expectedMultiLineString = MultiLineString.fromJson(json)
        assertEquals(expectedMultiLineString, actualMultiLineString)
    }

    @Test
    fun fromJson_coordinatesPresent() {
        assertFailsWith(SerializationException::class) {
            MultiLineString.fromJson(
                """
                {
                    "type": "MultiLineString",
                    "coordinates": null
                }
                """
                    .trimIndent()
            )
        }
    }

    @Test
    fun wrongType() {
        assertIs<MultiLineString>(
            MultiLineString.fromJsonOrNull(
                """{"type":"MultiLineString","coordinates":[[[1.0,2.0],[3.0,4.0]]]}"""
            )
        )
        assertNull(
            MultiLineString.fromJsonOrNull(
                """{"type":"LineString","coordinates":[[[1.0,2.0],[3.0,4.0]]]}"""
            )
        )
    }

    @Test
    fun missingType() {
        assertIs<MultiLineString>(
            MultiLineString.fromJsonOrNull(
                """{"type":"MultiLineString","coordinates":[[[1.0,2.0],[3.0,4.0]]]}"""
            )
        )
        assertNull(MultiLineString.fromJsonOrNull("""{"coordinates":[[[1.0,2.0],[3.0,4.0]]]}"""))
    }
}
