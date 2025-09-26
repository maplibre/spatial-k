package org.maplibre.spatialk.geojson.utils

import kotlin.test.assertEquals
import org.maplibre.spatialk.geojson.serialization.GeoJson

const val DELTA: Double = 1E-10

fun compareJson(expectedJson: String, actualJson: String) {
    assertEquals(GeoJson.parseToJsonElement(expectedJson), GeoJson.parseToJsonElement(actualJson))
}
