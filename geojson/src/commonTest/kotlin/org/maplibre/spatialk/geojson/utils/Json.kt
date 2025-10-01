package org.maplibre.spatialk.geojson.utils

import kotlin.test.assertEquals
import kotlinx.serialization.json.Json

const val DELTA: Double = 1E-10

fun assertJsonEquals(expectedJson: String, actualJson: String) {
    assertEquals(Json.parseToJsonElement(expectedJson), Json.parseToJsonElement(actualJson))
}
