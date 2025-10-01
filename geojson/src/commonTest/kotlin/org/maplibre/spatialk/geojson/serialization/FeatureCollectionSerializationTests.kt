package org.maplibre.spatialk.geojson.serialization

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position

@Suppress("EXPERIMENTAL_API_USAGE", "MagicNumber")
class FeatureCollectionSerializationTests {

    @Test
    fun testSerializeFeatureCollection() {
        val geometry = Point(Position(12.3, 45.6))
        val feature =
            Feature(
                geometry,
                buildJsonObject {
                    put("size", 45.1)
                    put("name", "Nowhere")
                },
            )
        val collection = FeatureCollection(feature, feature)

        val json =
            """
            |{"type":"FeatureCollection","features":[{"type":"Feature","geometry":{"type":"Point","coordinates":
            |[12.3,45.6]},"properties":{"size":45.1,"name":"Nowhere"}},{"type":"Feature","geometry":{"type":"Point",
            |"coordinates":[12.3,45.6]},"properties":{"size":45.1,"name":"Nowhere"}}]}"""
                .trimMargin()
                .replace("\n", "")

        assertEquals(json, collection.toJson(), "FeatureCollection (fast)")
        assertEquals(json, collection.toJson(), "FeatureCollection (kotlinx)")
    }

    @Test
    fun testDeserializeFeatureCollection() {
        val geometry = Point(Position(12.3, 45.6))
        val feature =
            Feature(
                geometry,
                properties =
                    buildJsonObject {
                        put("size", 45.1)
                        put("name", "Nowhere")
                    },
            )
        val collection = FeatureCollection(feature, feature)

        assertEquals(
            collection,
            FeatureCollection.fromJson(
                """
                |{"type":"FeatureCollection","features":[{"type":"Feature","geometry":{"type":"Point","coordinates":
                |[12.3,45.6]},"properties":{"size":45.1,"name":"Nowhere"}},{"type":"Feature","geometry":{"type":"Point",
                |"coordinates":[12.3,45.6]},"properties":{"size":45.1,"name":"Nowhere"}}]}"""
                    .trimMargin()
                    .replace("\n", "")
            ),
        )
    }
}
