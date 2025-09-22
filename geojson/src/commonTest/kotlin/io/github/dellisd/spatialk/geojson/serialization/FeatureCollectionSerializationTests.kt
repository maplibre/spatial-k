package io.github.dellisd.spatialk.geojson.serialization

import io.github.dellisd.spatialk.geojson.Feature
import io.github.dellisd.spatialk.geojson.FeatureCollection
import io.github.dellisd.spatialk.geojson.LineString
import io.github.dellisd.spatialk.geojson.Position
import io.github.dellisd.spatialk.geojson.Point
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals

@Suppress("EXPERIMENTAL_API_USAGE", "MagicNumber")
class FeatureCollectionSerializationTests {

    @Test
    fun testSerializeFeatureCollection() {
        val geometry = Point(Position(12.3, 45.6))
        val feature = Feature(
            geometry, mapOf(
                "size" to JsonPrimitive(45.1),
                "name" to JsonPrimitive("Nowhere")
            )
        )
        val collection = FeatureCollection(feature, feature)

        val json =
            """{"type":"FeatureCollection","features":[{"type":"Feature","geometry":{"type":"Point","coordinates":
                |[12.3,45.6]},"properties":{"size":45.1,"name":"Nowhere"}},{"type":"Feature","geometry":{"type":"Point",
                |"coordinates":[12.3,45.6]},"properties":{"size":45.1,"name":"Nowhere"}}]}"""
                .trimMargin()
                .replace("\n", "")

        assertEquals(json, collection.json(), "FeatureCollection (fast)")
        assertEquals(json, Json.encodeToString(collection), "FeatureCollection (kotlinx)")
    }

    @Test
    fun testSerializePolymorphicFeatureCollection() {
        val position = Position(12.3, 45.6)
        val point = Point(position)
        val featurePoint = Feature(
            point, mapOf(
                "size" to JsonPrimitive(45.1),
                "name" to JsonPrimitive("Nowhere")
            )
        )
        val line = LineString(coordinates = listOf(position, Position(position.component2(), position.component1())))
        val featureLine = Feature(line)
        val collection = FeatureCollection(featurePoint, featureLine)
        val json =
            """{"type":"FeatureCollection","features":[{"type":"Feature","geometry":{"type":"Point","coordinates":
            |[12.3,45.6]},"properties":{"size":45.1,"name":"Nowhere"}},{"type":"Feature","geometry":{"type":
            |"LineString","coordinates":[[12.3,45.6],[45.6,12.3]]},"properties":{}}]}"""
                .trimMargin()
                .replace("\n", "")
        assertEquals(json, collection.json(), "FeatureCollection (fast)")
        assertEquals(json, Json.encodeToString(collection), "FeatureCollection (kotlinx)")
    }


    @Test
    fun testDeserializeFeatureCollection() {
        val geometry = Point(Position(12.3, 45.6))
        val feature = Feature(
            geometry, properties = mapOf(
                "size" to JsonPrimitive(45.1),
                "name" to JsonPrimitive("Nowhere")
            )
        )
        val collection = FeatureCollection(feature, feature)

        assertEquals(
            collection,
            FeatureCollection.fromJson(
                """{"type":"FeatureCollection","features":[{"type":"Feature","geometry":{"type":"Point","coordinates":
                |[12.3,45.6]},"properties":{"size":45.1,"name":"Nowhere"}},{"type":"Feature","geometry":{"type":"Point",
                |"coordinates":[12.3,45.6]},"properties":{"size":45.1,"name":"Nowhere"}}]}"""
                    .trimMargin()
                    .replace("\n", "")
            )
        )
    }

    @Test
    fun testDeerializePolymorphicFeatureCollection() {
        val position = Position(12.3, 45.6)
        val point = Point(position)
        val featurePoint = Feature(
            point, mapOf(
                "size" to JsonPrimitive(45.1),
                "name" to JsonPrimitive("Nowhere")
            )
        )
        val line = LineString(coordinates = listOf(position, Position(position.component2(), position.component1())))
        val featureLine = Feature(line)
        val collection = FeatureCollection(featurePoint, featureLine)
        assertEquals(
            collection,
            FeatureCollection.fromJson(
                """{"type":"FeatureCollection","features":[{"type":"Feature","geometry":{"type":"Point","coordinates":
                |[12.3,45.6]},"properties":{"size":45.1,"name":"Nowhere"}},{"type":"Feature","geometry":{"type":
                |"LineString","coordinates":[[12.3,45.6],[45.6,12.3]]},"properties":{}}]}"""
                    .trimMargin()
                    .replace("\n", "")
            )
        )
    }
}
