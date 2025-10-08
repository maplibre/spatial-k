package org.maplibre.spatialk.geojson

import kotlinx.serialization.json.Json
import org.intellij.lang.annotations.Language

public data object GeoJson {
    /** The default Json configuration for GeoJson objects. */
    @SensitiveGeoJsonApi public val jsonFormat: Json = Json { ignoreUnknownKeys = true }

    public inline fun <reified T : GeoJsonObject> decodeFromString(
        @Language("json") string: String
    ): T = TODO()

    public inline fun <reified T : GeoJsonObject> decodeFromStringOrNull(
        @Language("json") string: String
    ): T = TODO()

    /** Encode [value] into a GeoJSON [String]. */
    @OptIn(SensitiveGeoJsonApi::class)
    public inline fun <reified T : GeoJsonElement?> encodeToString(value: T): String =
        jsonFormat.encodeToString<T>(value)

    /** Decode a GeoJSON [string] into a serializeable GeoJSON element. */
    @OptIn(SensitiveGeoJsonApi::class)
    public inline fun <reified T : GeoJsonElement?> decodeFromString2(
        @Language("json") string: String
    ): T = jsonFormat.decodeFromString<T>(string)

    /**
     * @return the decoded element [T], or null if the [string] could not be deserialized into [T]
     * @see [decodeFromString]
     */
    @OptIn(SensitiveGeoJsonApi::class)
    public inline fun <reified T : GeoJsonElement?> decodeFromStringOrNull2(
        @Language("json") string: String
    ): T? =
        try {
            decodeFromString2<T>(string)
        } catch (_: IllegalArgumentException) {
            null
        }
}
