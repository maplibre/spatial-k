package org.maplibre.spatialk.geojson

import org.maplibre.spatialk.geojson.utils.DELTA
import org.maplibre.spatialk.testutil.readResourceFile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue


class FeatureCollectionTest {

    @Test
    fun sanity() {
        val features = listOf(
            Feature(null),
            Feature(null)
        )

        val featureCollection = FeatureCollection(features)
        assertNotNull(featureCollection)
    }

    @Test
    fun bbox_nullWhenNotSet() {
        val features = listOf(
            Feature(null),
            Feature(null)
        )

        val featureCollection = FeatureCollection(features)
        assertNull(featureCollection.bbox)
    }

    @Test
    fun bbox_doesNotSerializeWhenNotPresent() {
        val points = listOf(
            Position(1.0, 2.0),
            Position(2.0, 3.0)
        )

        val lineString = LineString(points)
        val feature = Feature(lineString)

        val features = listOf(feature, feature)

        val actualFeatureCollection =
            FeatureCollection.fromJson(FeatureCollection(features).json())
        val expectedFeatureCollection = FeatureCollection.fromJson(
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
                                }
                            },
                            {
                                "type": "Feature",
                                "geometry": {
                                    "type": "LineString",
                                    "coordinates": [
                                        [1.0, 2.0],
                                        [2.0, 3.0]
                                    ]
                                }
                            }
                        ]
                    }
                    """.trimIndent()
        )

        assertEquals(expectedFeatureCollection, actualFeatureCollection)
    }

    @Test
    fun bbox_returnsCorrectBbox() {
        val features = listOf(
            Feature(null),
            Feature(null)
        )

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
        val points = listOf(
            Position(1.0, 2.0),
            Position(2.0, 3.0)
        )
        val lineString = LineString(points)
        val feature = Feature(lineString)

        val features = listOf(feature, feature)
        val bbox = BoundingBox(1.0, 2.0, 3.0, 4.0)

        val actualFeatureCollection =
            FeatureCollection.fromJson(FeatureCollection(features, bbox).json())
        val expectedFeatureCollection = FeatureCollection.fromJson(
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
                                }
                            },
                            {
                                "type": "Feature",
                                "geometry": {
                                    "type": "LineString",
                                    "coordinates": [
                                        [1.0, 2.0],
                                        [2.0, 3.0]
                                    ]
                                }
                            }
                        ]
                    }
                    """.trimIndent()
        )

        assertEquals(expectedFeatureCollection, actualFeatureCollection)
    }

    @Test
    fun passingInSingleFeature_doesHandleCorrectly() {
        val point = Point(1.0, 2.0)
        val feature = Feature(point)
        val geo = FeatureCollection(listOf(feature))
        assertNotNull(geo.features)
        assertEquals(1, geo.features.size)
        assertEquals(2.0, (geo.features.first().geometry as Point).coordinates.latitude, DELTA)
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
            FeatureCollection.fromJson(FeatureCollection.fromJson(json).json())
        assertEquals(expectedFeatureCollection, actualFeatureCollection)
    }

    companion object {
        private const val SAMPLE_FEATURECOLLECTION = "sample-featurecollection.json"
        private const val SAMPLE_FEATURECOLLECTION_BBOX = "sample-feature-collection-with-bbox.json"
    }
}
