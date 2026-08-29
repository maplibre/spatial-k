package org.maplibre.spatialk.geohash

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SerializationTest {
    @Serializable private data class Holder(val cell: Geohash, val shortlink: OsmShortlink)

    @Test
    fun `round-trips cells and shortlinks as JSON strings`() {
        val cell = Geohash.parse("ezs42")
        val shortlink = OsmShortlink.parse("0EEQjE--")

        assertEquals("\"ezs42\"", Json.encodeToString(cell))
        assertEquals("\"0EEQjE--\"", Json.encodeToString(shortlink))
        assertEquals(cell, Json.decodeFromString<Geohash>("\"EZS42\""))
        assertEquals(shortlink, Json.decodeFromString<OsmShortlink>("\"0EEQjE--\""))

        val holder = Holder(cell, shortlink)
        val json = """{"cell":"ezs42","shortlink":"0EEQjE--"}"""
        assertEquals(json, Json.encodeToString(holder))
        assertEquals(holder, Json.decodeFromString<Holder>(json))
    }

    @Test
    fun `rejects invalid JSON strings`() {
        assertFailsWith<SerializationException> { Json.decodeFromString<Geohash>("\"ail\"") }
        assertFailsWith<SerializationException> {
            Json.decodeFromString<OsmShortlink>("\"ABC+--\"")
        }
    }
}
