package org.maplibre.spatialk.geojson.utils

import kotlin.test.assertEquals
import kotlinx.serialization.json.Json

const val DELTA: Double = 1E-10

fun compareJson(expectedJson: String, actualJson: String) {
    val json = Json { isLenient = true }
    assertEquals(json.parseToJsonElement(expectedJson), json.parseToJsonElement(actualJson))
}
