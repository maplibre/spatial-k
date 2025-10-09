package org.maplibre.spatialk.geojson.utils

import kotlin.test.assertEquals
import kotlinx.serialization.json.Json
import org.intellij.lang.annotations.Language

const val DELTA: Double = 1E-10

fun assertJsonEquals(@Language("json") expectedJson: String, @Language("json") actualJson: String) {
    assertEquals(Json.parseToJsonElement(expectedJson), Json.parseToJsonElement(actualJson))
}
