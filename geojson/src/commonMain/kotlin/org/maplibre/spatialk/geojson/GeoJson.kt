package org.maplibre.spatialk.geojson

import kotlinx.serialization.json.Json
import org.intellij.lang.annotations.Language

public data object GeoJson {
    /** The default Json configuration for GeoJson objects. */
    public val jsonFormat: Json = Json { ignoreUnknownKeys = true }

    /** Encode [value] into a GeoJSON [String]. */
    public inline fun <reified T : GeoJsonObject?> encodeToString(value: T): String =
        jsonFormat.encodeToString<T>(value)

    /** Decode a GeoJSON [string] into a serializeable GeoJSON element. */
    public inline fun <reified T : GeoJsonObject?> decodeFromString(
        @Language("json") string: String
    ): T = jsonFormat.decodeFromString<T>(string)

    /**
     * @return the decoded element [T], or null if the [string] could not be deserialized into [T]
     * @see [decodeFromString]
     */
    public inline fun <reified T : GeoJsonObject?> decodeFromStringOrNull(
        @Language("json") string: String
    ): T? =
        try {
            decodeFromString<T>(string)
        } catch (_: IllegalArgumentException) {
            null
        }
}
