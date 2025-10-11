package org.maplibre.spatialk.geojson

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.maplibre.spatialk.geojson.dsl.buildFeature
import org.maplibre.spatialk.geojson.utils.assertJsonEquals

class GeoJsonObjectTest {
    private val feature: GeoJsonObject =
        buildFeature(geometry = Point(1.1, 2.2)) {
            properties = buildJsonObject { put("test", "value") }
        }
    private val json = // language=json
        """
            {
                "type": "Feature",
                "geometry": {
                    "type": "Point",
                    "coordinates": [1.1, 2.2]
                },
                "properties": {
                    "test": "value"
                }
            }
        """

    @Test
    fun deserializeHelper() {
        assertEquals(feature, GeoJsonObject.fromJson(json))
    }

    @Test
    fun serializeHelper() {
        assertJsonEquals(json, feature.toJson())
    }

    @Test
    fun deserializeGeoJson() {
        assertEquals(feature, GeoJson.decodeFromString<GeoJsonObject>(json))
    }

    @Test
    fun serializeGeoJson() {
        assertJsonEquals(json, GeoJson.encodeToString<GeoJsonObject>(feature))
    }
}
