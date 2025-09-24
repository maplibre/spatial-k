package org.maplibre.spatialk.geojson.utils

import kotlinx.serialization.json.Json
import kotlin.test.assertEquals


const val DELTA: Double = 1E-10

fun compareJson(expectedJson: String, actualJson: String) {
    val json = Json { isLenient = true }
    assertEquals(
        json.parseToJsonElement(expectedJson),
        json.parseToJsonElement(actualJson)
    )
}
