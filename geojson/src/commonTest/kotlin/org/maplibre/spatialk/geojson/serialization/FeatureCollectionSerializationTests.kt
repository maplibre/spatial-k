package org.maplibre.spatialk.geojson.serialization

import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.geojson.Point
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
}
