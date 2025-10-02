package org.maplibre.spatialk.geojson.serialization

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position

class FeatureSerializationTests {

    @Test
    fun testSerializeFeature() {
        val geometry = Point(Position(12.3, 45.6))
        val feature =
            Feature(
                geometry,
                buildJsonObject {
                    put("size", 45.1)
                    put("name", "Nowhere")
                },
                "001",
                BoundingBox(11.6, 45.1, 12.7, 45.7),
            )

        val json =
            """
            |{"type":"Feature","geometry":{"type":"Point","coordinates":[12.3,45.6]},
            |"properties":{"size":45.1,"name":"Nowhere"},"id":"001","bbox":[11.6,45.1,12.7,45.7]}"""
                .trimMargin()
                .replace("\n", "")

        assertEquals(json, feature.toJson(), "Feature (fast)")
        assertEquals(json, feature.toJson(), "Feature (kotlinx)")
    }

    @Test
    fun testDeserializeFeature() {
        val geometry = Point(Position(12.3, 45.6))
        val feature =
            Feature(
                geometry,
                properties =
                    buildJsonObject {
                        put("size", 45.1)
                        put("name", "Nowhere")
                    },
                id = "001",
                bbox = BoundingBox(11.6, 45.1, 12.7, 45.7),
            )

        assertEquals(
            feature,
            Feature.fromJson(
                """
                |{"type":"Feature",
                |"geometry":{
                |"type":"Point",
                |"coordinates":[12.3,45.6]},
                |"id":"001",
                |"properties":{
                |"size":45.1,
                |"name":"Nowhere"},
                |"bbox":[11.6,45.1,12.7,45.7]
                |}"""
                    .trimMargin()
                    .replace("\n", "")
            ),
        )
    }

    @Test
    fun testDeserializeIncorrectType() {
        val feature = Feature(geometry = Point(longitude = 12.3, latitude = 45.6))
        val json = feature.toJson()
        assertEquals(feature, Feature.fromJson<Point>(json))
        assertFailsWith<SerializationException> { Feature.fromJson<LineString>(json) }
    }
}
