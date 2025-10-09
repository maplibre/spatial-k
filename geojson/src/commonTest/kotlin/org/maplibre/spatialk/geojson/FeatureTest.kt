package org.maplibre.spatialk.geojson

import kotlin.test.*
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.*
import org.maplibre.spatialk.geojson.utils.DELTA
import org.maplibre.spatialk.geojson.utils.assertJsonEquals

class FeatureTest {

    @Test
    fun sanity() {
        val points = listOf(Position(1.0, 2.0), Position(2.0, 3.0))

        val lineString = LineString(points)
        val feature = Feature(lineString)
        assertNotNull(feature)
    }

    @Test
    fun bbox_nullWhenNotSet() {
        val points = listOf(Position(1.0, 2.0), Position(2.0, 3.0))

        val lineString = LineString(points)
        val feature = Feature(lineString)
        assertNull(feature.bbox)
    }

    @Test
    fun bbox_doesNotSerializeWhenNotPresent() {
        val points = listOf(Position(1.0, 2.0), Position(2.0, 3.0))

        val lineString = LineString(points)
        val feature = Feature(lineString)

        val actualFeature = Feature.fromJson<Geometry>(feature.toJson())
        val expectedFeature =
            Feature.fromJson<Geometry>(
                """
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "LineString",
                        "coordinates": [
                            [1.0, 2.0],
                            [2.0, 3.0]
                        ]
                    },
                    "properties": null
                }
                """
                    .trimIndent()
            )

        assertEquals(expectedFeature, actualFeature)
    }

    @Test
    fun bbox_returnsCorrectBbox() {
        val points = listOf(Position(1.0, 2.0), Position(2.0, 3.0))

        val lineString = LineString(points)

        val bbox = BoundingBox(1.0, 2.0, 3.0, 4.0)
        val feature = Feature(lineString, bbox = bbox)
        val actualBbox = feature.bbox!!
        assertNotNull(actualBbox)
        assertEquals(1.0, actualBbox.west, DELTA)
        assertEquals(2.0, actualBbox.south, DELTA)
        assertEquals(3.0, actualBbox.east, DELTA)
        assertEquals(4.0, actualBbox.north, DELTA)
    }

    @Test
    fun bbox_doesSerializeWhenPresent() {
        val points = listOf(Position(1.0, 2.0), Position(2.0, 3.0))

        val lineString = LineString(points)

        val bbox = BoundingBox(1.0, 2.0, 3.0, 4.0)
        val feature = Feature(lineString, bbox = bbox)

        val actualFeature = Feature.fromJson<Geometry>(feature.toJson())
        val expectedFeature =
            Feature.fromJson<Geometry>(
                """
                {
                    "type": "Feature",
                    "bbox": [1.0, 2.0, 3.0, 4.0],
                    "geometry": {
                        "type": "LineString",
                        "coordinates": [
                            [1.0, 2.0],
                            [2.0, 3.0]
                        ]
                    },
                    "properties": null
                }
                """
                    .trimIndent()
            )

        assertEquals(expectedFeature, actualFeature)
    }

    @Test
    fun point_feature_fromJson() {
        val json =
            """
            {
                "type": "Feature",
                "geometry": {
                    "type": "Point",
                    "coordinates": [125.6, 10.1]
                },
                "properties": {
                    "name": "Dinagat Islands"
                }
            }
            """
                .trimIndent()
        val feature = Feature.fromJson<Point>(json)
        assertEquals(feature.geometry.coordinates.longitude, 125.6, DELTA)
        assertEquals(feature.geometry.coordinates.latitude, 10.1, DELTA)
        assertEquals(feature.properties!!["name"]!!.jsonPrimitive.content, "Dinagat Islands")
    }

    @Test
    fun linestring_feature_fromJson() {
        val json =
            """
            {
                "type": "Feature",
                "geometry": {
                    "type": "LineString",
                    "coordinates": [
                        [102.0, 20.0],
                        [103.0, 3.0],
                        [104.0, 4.0],
                        [105.0, 5.0]
                    ]
                },
                "properties": {
                    "name": "line name"
                }
            }
            """
                .trimIndent()
        val feature = Feature.fromJson<LineString>(json)
        val points = feature.geometry.coordinates
        assertNotNull(points)
        assertEquals(4, points.size.toLong())
        assertEquals(105.0, points[3].longitude, DELTA)
        assertEquals(5.0, points[3].latitude, DELTA)
        assertEquals("line name", feature.properties!!["name"]!!.jsonPrimitive.content)
    }

    @Test
    fun point_feature_toJson() {
        val properties = JsonObject(content = mapOf("name" to JsonPrimitive("Dinagat Islands")))
        val geo = Feature(Point(125.6, 10.1), properties = properties)
        val geoJsonString = geo.toJson()

        val expectedJson =
            """
            {
                "type": "Feature",
                "geometry": {
                    "type": "Point",
                    "coordinates": [125.6, 10.1]
                },
                "properties": {
                    "name": "Dinagat Islands"
                }
            }
            """
                .trimIndent()

        assertJsonEquals(expectedJson, geoJsonString)
    }

    @Test
    fun linestring_feature_toJson() {
        val properties = JsonObject(content = mapOf("name" to JsonPrimitive("Dinagat Islands")))

        val points = listOf(Position(1.0, 1.0), Position(2.0, 2.0), Position(3.0, 3.0))

        val lineString = LineString(points)

        val geo = Feature(lineString, properties)

        val actualFeature = Feature.fromJson<Geometry>(geo.toJson())
        val expectedFeature =
            Feature.fromJson<Geometry>(
                """
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "LineString",
                        "coordinates": [
                            [1.0, 1.0],
                            [2.0, 2.0],
                            [3.0, 3.0]
                        ]
                    },
                    "properties": {
                        "name": "Dinagat Islands"
                    }
                }
                """
                    .trimIndent()
            )

        assertEquals(expectedFeature, actualFeature)
    }

    @Test
    fun testNonNullProperties() {
        val points = listOf(Position(0.1, 2.3), Position(4.5, 6.7))

        val line = LineString(points)
        val properties = JsonObject(content = mapOf("key" to JsonPrimitive("value")))

        val feature = Feature(line, properties)
        val jsonString = feature.toJson()
        assertTrue(jsonString.contains("\"properties\":{\"key\":\"value\"}"))

        // Feature (non-empty Properties) -> Json (non-empty Properties) -> Equavalent Feature
        assertEquals(Feature.fromJson(jsonString), feature)
    }

    @Test
    fun testNullPropertiesJson() {
        val jsonString =
            """
            {
                "type": "Feature",
                "bbox": [1.0, 2.0, 3.0, 4.0],
                "geometry": {
                    "type": "LineString",
                    "coordinates": [
                        [1.0, 2.0],
                        [2.0, 3.0]
                    ]
                },
                "properties": null
            }
            """
                .trimIndent()

        val actualFeature =
            Feature.fromJson<Geometry>(Feature.fromJson<Geometry>(jsonString).toJson())
        val expectedFeature = Feature.fromJson<Geometry>(jsonString)
        assertEquals(expectedFeature, actualFeature)
    }

    @Test
    fun pointFeature_fromJson_toJson() {
        val jsonString =
            """
            {
                "bbox": [-120.0, -60.0, 120.0, 60.0],
                "geometry": {
                    "bbox": [-110.0, -50.0, 110.0, 50.0],
                    "coordinates": [100.0, 0.0],
                    "type": "Point"
                },
                "type": "Feature",
                "properties": {
                    "prop0": "value0",
                    "prop1": "value1"
                }
            }
            """
                .trimIndent()

        val actualFeature =
            Feature.fromJson<Geometry>(Feature.fromJson<Geometry>(jsonString).toJson())
        val expectedFeature = Feature.fromJson<Geometry>(jsonString)
        assertEquals(expectedFeature, actualFeature)
    }

    @Test
    fun testMissingType() {
        assertNull(
            Feature.fromJsonOrNull<Geometry>(
                """
            {
                "geometry": {
                    "type": "Point",
                    "coordinates": [125.6, 10.1]
                },
                "properties": null
            }
            """
            )
        )
    }

    @Test
    fun testNullGeometry() {
        val nullJson = """{"type": "Feature", "geometry": null, "properties": null}"""
        assertNull(Feature.fromJson<Geometry?>(nullJson).geometry)
        assertNull(Feature.fromJson<MultiPoint?>(nullJson).geometry)
        assertNull(Feature.fromJson<Nothing?>(nullJson).geometry)
        assertFailsWith<SerializationException> { Feature.fromJson<Geometry>(nullJson) }
        assertFailsWith<SerializationException> { Feature.fromJson<MultiPoint>(nullJson) }

        val json = Json.decodeFromString<JsonObject>(Feature.fromJson<Geometry?>(nullJson).toJson())
        assertTrue(json.containsKey("geometry"))
        assertEquals(JsonNull, json["geometry"])
    }

    @OptIn(SensitiveGeoJsonApi::class)
    @Test
    fun testIncorrectGeometry() {
        val multipointJson =
            """
            {
                "type": "Feature",
                "geometry": {
                    "type": "MultiPoint",
                    "coordinates": [[125.6, 10.1], [125.6, 10.1]]
                },
                "properties": null
            }
            """

        assertIs<MultiPoint>(Feature.fromJson<Geometry?>(multipointJson).geometry)
        assertIs<MultiPoint>(Feature.fromJson<Geometry>(multipointJson).geometry)
        assertIs<MultiPoint>(Feature.fromJson<MultiPoint?>(multipointJson).geometry)
        assertFailsWith<SerializationException> { Feature.fromJson<LineString>(multipointJson) }
        assertFailsWith<SerializationException> { Feature.fromJson<Nothing?>(multipointJson) }
    }
}
