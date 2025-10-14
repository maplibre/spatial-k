package org.maplibre.spatialk.units

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.maplibre.spatialk.units.Bearing.Companion.Northwest
import org.maplibre.spatialk.units.extensions.degrees
import org.maplibre.spatialk.units.extensions.meters
import org.maplibre.spatialk.units.extensions.squareMeters

class SerializationTest {

    @Serializable
    data class TestObject(
        val length: Length,
        val area: Area,
        val rotation: Rotation,
        val bearing: Bearing,
    )

    val testObject = TestObject(100.meters, 50.squareMeters, 45.degrees, Northwest)

    val testJson =
        Json.encodeToString(
            buildJsonObject {
                put("length", 100.0)
                put("area", 50.0)
                put("rotation", 45.0)
                put("bearing", 315.0)
            }
        )

    @Test
    fun testSerializeMeasurements() {
        assertEquals(testJson, Json.encodeToString(testObject))
    }

    @Test
    fun testDeserializeMeasurements() {
        assertEquals(testObject, Json.decodeFromString<TestObject>(testJson))
    }
}
