package org.maplibre.spatialk.geojson.utils

import kotlin.test.assertEquals
import kotlinx.serialization.json.Json
import org.intellij.lang.annotations.Language

const val DELTA: Double = 1E-10

fun assertJsonEquals(@Language("json") expectedJson: String, @Language("json") actualJson: String) {
    // Re-encode so whole-number doubles compare equal across platforms (JS emits `1`, JVM `1.0`).
    // Member order is compared on purpose. Encoders write a stable insertion order, so this
    // does not flake; fixtures should match that order rather than treating objects as unordered.
    val expected = Json.encodeToString(Json.parseToJsonElement(expectedJson))
    val actual = Json.encodeToString(Json.parseToJsonElement(actualJson))
    assertEquals(expected, actual)
}
