package org.maplibre.spatialk.geojson

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.maplibre.spatialk.geojson.dsl.addFeature
import org.maplibre.spatialk.geojson.dsl.buildFeatureCollection
import org.maplibre.spatialk.geojson.dsl.featureCollectionOf
import org.maplibre.spatialk.geojson.dsl.lineStringOf
import org.maplibre.spatialk.geojson.dsl.multiPointOf
import org.maplibre.spatialk.geojson.utils.DELTA
import org.maplibre.spatialk.geojson.utils.assertJsonEquals
import org.maplibre.spatialk.testutil.readResourceFile

class FeatureCollectionTest {

    @Serializable private data class NameProp(val name: String)

    @Test
    fun sanity() {
        val features = listOf(Feature(null, null), Feature(null, null))

        val featureCollection = FeatureCollection(features)
        assertNotNull(featureCollection)
    }

    @Test
    fun bbox_nullWhenNotSet() {
        val features = listOf(Feature(null, null), Feature(null, null))

        val featureCollection = FeatureCollection(features)
        assertNull(featureCollection.bbox)
    }

    @Test
    fun bbox_doesNotSerializeWhenNotPresent() {
        val points = listOf(Position(1.0, 2.0), Position(2.0, 3.0))

        val lineString = LineString(points)
        val feature = Feature(lineString, null)

        val features = listOf(feature, feature)

        val actualFeatureCollection =
            FeatureCollection.fromJson(FeatureCollection(features).toJson())
        val expectedFeatureCollection =
            FeatureCollection.fromJson(
                """
                {
                    "type": "FeatureCollection",
                    "features": [
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
                        },
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
                    ]
                }
                """
                    .trimIndent()
            )

        assertEquals(expectedFeatureCollection, actualFeatureCollection)
    }

    @Test
    fun bbox_returnsCorrectBbox() {
        val features = listOf(Feature(null, null), Feature(null, null))

        val bbox = BoundingBox(1.0, 2.0, 3.0, 4.0)
        val featureCollection = FeatureCollection(features, bbox)
        val actualBbox = featureCollection.bbox
        assertNotNull(actualBbox)
        assertEquals(1.0, actualBbox.southwest.longitude)
        assertEquals(2.0, actualBbox.southwest.latitude)
        assertEquals(3.0, actualBbox.northeast.longitude)
        assertEquals(4.0, actualBbox.northeast.latitude)
    }

    @Test
    fun bbox_doesSerializeWhenPresent() {
        val points = listOf(Position(1.0, 2.0), Position(2.0, 3.0))
        val lineString = LineString(points)
        val feature = Feature(lineString, null)

        val features = listOf(feature, feature)
        val bbox = BoundingBox(1.0, 2.0, 3.0, 4.0)

        val actualFeatureCollection =
            FeatureCollection.fromJson(FeatureCollection(features, bbox).toJson())
        val expectedFeatureCollection =
            FeatureCollection.fromJson(
                """
                {
                    "type": "FeatureCollection",
                    "bbox": [1.0, 2.0, 3.0, 4.0],
                    "features": [
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
                        },
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
                    ]
                }
                """
                    .trimIndent()
            )

        assertEquals(expectedFeatureCollection, actualFeatureCollection)
    }

    @Test
    fun passingInSingleFeature_doesHandleCorrectly() {
        val point = Point(1.0, 2.0)
        val feature = Feature(point, null)
        val geo = FeatureCollection(listOf(feature))
        assertNotNull(geo.features)
        assertEquals(1, geo.features.size)
        assertEquals(2.0, geo.features.first().geometry.coordinates.latitude, DELTA)
    }

    @Test
    fun fromJson() {
        val json = readResourceFile(SAMPLE_FEATURECOLLECTION)
        val geo = FeatureCollection.fromJson(json)
        assertEquals(geo.features.size, 3)
        assertTrue(geo.features[0].geometry is Point)
        assertTrue(geo.features[0].geometry is Point)
        assertTrue(geo.features[1].geometry is LineString)
        assertTrue(geo.features[2].geometry is Polygon)
    }

    @Test
    fun toJson() {
        val json = readResourceFile(SAMPLE_FEATURECOLLECTION_BBOX)
        val expectedFeatureCollection = FeatureCollection.fromJson(json)
        val actualFeatureCollection =
            FeatureCollection.fromJson(FeatureCollection.fromJson(json).toJson())
        assertEquals(expectedFeatureCollection, actualFeatureCollection)
    }

    @Test
    fun testMissingType() {
        assertNull(Feature.fromJsonOrNull<Geometry?, JsonObject?>("{\"features\": []}"))
    }

    @Test
    fun testEmptyCollection() {
        val json = "{\"type\": \"FeatureCollection\", \"features\": []}"
        val fc = featureCollectionOf<Geometry?, JsonObject?>()
        assertEquals(fc, FeatureCollection.fromJson<Geometry?, JsonObject?>(json))
        assertJsonEquals(json, fc.toJson())
    }

    @Test
    fun testMixedCollection() {
        val json =
            """
            {
                "type": "FeatureCollection",
                "features": [
                    {"type": "Feature", "geometry": {"type": "Point", "coordinates": [1.1, 2.2]}, "properties": null},
                    {"type": "Feature", "geometry": {"type": "LineString", "coordinates": [[1.1, 2.2], [3.3, 4.4]]}, "properties": null}
                ]
            }
            """

        val fc = buildFeatureCollection {
            addFeature(geometry = Point(1.1, 2.2), properties = null)
            addFeature(
                geometry = lineStringOf(Position(1.1, 2.2), Position(3.3, 4.4)),
                properties = null,
            )
        }

        assertEquals(fc, FeatureCollection.fromJson<Geometry?, Nothing?>(json))
        assertNull(FeatureCollection.fromJsonOrNull<Point, Nothing?>(json))

        assertJsonEquals(json, fc.toJson())
    }

    @Test
    fun testHomogenousCollection() {
        val json =
            """
            {
                "type": "FeatureCollection",
                "features": [
                    {"type": "Feature", "geometry": {"type": "MultiPoint", "coordinates": [[1.1, 2.2], [1.1, 2.2]]}, "properties": null},
                    {"type": "Feature", "geometry": {"type": "MultiPoint", "coordinates": [[3.3, 4.4], [3.3, 4.4]]}, "properties": null}
                ]
            }
            """

        val fc = buildFeatureCollection {
            addFeature(geometry = multiPointOf(Position(1.1, 2.2), Position(1.1, 2.2)), null)
            addFeature(geometry = multiPointOf(Position(3.3, 4.4), Position(3.3, 4.4)), null)
        }

        assertEquals(fc, FeatureCollection.fromJson<Geometry?, Nothing?>(json))
        assertEquals<FeatureCollection<*, *>>(
            fc,
            FeatureCollection.fromJson<MultiPoint, Nothing?>(json),
        )
        assertNull(FeatureCollection.fromJsonOrNull<LineString, Nothing?>(json))

        assertJsonEquals(json, fc.toJson())
    }

    @Test
    fun testNullableCollection() {
        val json =
            """
            {
                "type": "FeatureCollection",
                "features": [
                    {"type": "Feature", "geometry": null, "properties": null},
                    {"type": "Feature", "geometry": {"type": "Point", "coordinates": [1.1, 2.2]}, "properties": null}
                ]
            }
            """

        val fc = buildFeatureCollection {
            addFeature(null, null)
            addFeature(geometry = Point(1.1, 2.2))
        }

        assertEquals(fc, FeatureCollection.fromJson<Point?, Nothing?>(json))
        assertNull(FeatureCollection.fromJsonOrNull<Point, Nothing?>(json))

        assertJsonEquals(json, fc.toJson())
    }

    @Test
    fun testUnlocatedCollection() {
        val json =
            """
            {
                "type": "FeatureCollection",
                "features": [
                    {"type": "Feature", "geometry": null, "properties": null},
                    {"type": "Feature", "geometry": null, "properties": null}
                ]
            }
            """

        val fc = featureCollectionOf(Feature(null, null), Feature(null, null))

        assertEquals(fc, FeatureCollection.fromJson<Nothing?, Nothing?>(json))
        assertNull(FeatureCollection.fromJsonOrNull<Point, Nothing?>(json))

        assertJsonEquals(json, fc.toJson())
    }

    @Test
    fun testCollectionWithTypedProperties() {
        val json =
            """
            {
                "type": "FeatureCollection",
                "features": [
                    {"type": "Feature", "geometry": null, "properties": {"key1": "value1", "key2": "value2"}},
                    {"type": "Feature", "geometry": null, "properties": {"key3": "value3", "key4": "value4"}}
                ]
            }
            """

        val fc = buildFeatureCollection {
            addFeature(null, mapOf("key1" to "value1", "key2" to "value2"))
            addFeature(null, mapOf("key3" to "value3", "key4" to "value4"))
        }

        assertEquals(fc, FeatureCollection.fromJson<Geometry?, Map<String, String>>(json))
        assertNotNull(FeatureCollection.fromJsonOrNull<Nothing?, JsonObject>(json))
        assertNull(FeatureCollection.fromJsonOrNull<Geometry?, Map<String, Int>>(json))
        assertJsonEquals(json, fc.toJson())
    }

    @Test
    fun testCollectionWithNullableProperties() {
        val json =
            """
            {
                "type": "FeatureCollection",
                "features": [
                    {"type": "Feature", "geometry": null, "properties": null},
                    {"type": "Feature", "geometry": null, "properties": {"key": "value"}}
                ]
            }
            """

        val fc = buildFeatureCollection {
            addFeature<Nothing?, JsonObject?>(null, null)
            addFeature(null, JsonObject(mapOf("key" to JsonPrimitive("value"))))
        }

        assertEquals(fc, FeatureCollection.fromJson<Nothing?, JsonObject?>(json))
        assertNull(FeatureCollection.fromJsonOrNull<Nothing?, JsonObject>(json))

        assertJsonEquals(json, fc.toJson())
    }

    @Test
    fun testCollectionWithAllNullProperties() {
        val json =
            """
            {
                "type": "FeatureCollection",
                "features": [
                    {"type": "Feature", "geometry": null, "properties": null},
                    {"type": "Feature", "geometry": null, "properties": null}
                ]
            }
            """

        val fc = featureCollectionOf(Feature(null, null), Feature(null, null))

        assertEquals(fc, FeatureCollection.fromJson<Nothing?, Nothing?>(json))
        assertNull(FeatureCollection.fromJsonOrNull<Nothing?, JsonObject>(json))

        assertJsonEquals(json, fc.toJson())
    }

    @Test
    fun testCollectionWithDataClassProperties() {
        val json =
            """
            {
                "type": "FeatureCollection",
                "features": [
                    {"type": "Feature", "geometry": null, "properties": {"name": "First Feature"}},
                    {"type": "Feature", "geometry": null, "properties": {"name": "Second Feature"}}
                ]
            }
            """

        val fc = buildFeatureCollection {
            addFeature(null, NameProp("First Feature"))
            addFeature(null, NameProp("Second Feature"))
        }

        assertEquals(fc, FeatureCollection.fromJson<Nothing?, NameProp>(json))
        assertEquals("First Feature", fc.features[0].properties.name)
        assertEquals("Second Feature", fc.features[1].properties.name)

        assertJsonEquals(json, fc.toJson())
    }

    @Test
    fun testCollectionWithInvalidPropertyType() {
        // String type is not a valid properties type (must be an object)
        val stringPropsJson =
            """
            {
                "type": "FeatureCollection",
                "features": [
                    {"type": "Feature", "geometry": null, "properties": "not an object"}
                ]
            }
            """
        assertFailsWith<SerializationException> {
            FeatureCollection.fromJson<Nothing?, String>(stringPropsJson)
        }
    }

    @Test
    fun emptyStarProjection() {
        // should fall back to JsonObject properties
        val features: FeatureCollection<*, *> = featureCollectionOf()
        val json =
            """
                {
                  "type": "FeatureCollection",
                  "features": []
                }
            """
        assertJsonEquals(json, features.toJson())
    }

    companion object {
        private const val SAMPLE_FEATURECOLLECTION = "sample-featurecollection.json"
        private const val SAMPLE_FEATURECOLLECTION_BBOX = "sample-feature-collection-with-bbox.json"
    }
}
