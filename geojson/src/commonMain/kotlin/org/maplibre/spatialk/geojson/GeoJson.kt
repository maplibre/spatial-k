package org.maplibre.spatialk.geojson

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.ClassDiscriminatorMode
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.intellij.lang.annotations.Language

public data object GeoJson {
    /**
     * The default Json configuration for GeoJson objects.
     *
     * Using the [Json] methods directly can bypass some type safety with generics; prefer the
     * `toJson` and `fromJson` methods defined on all [GeoJsonObject] types.
     */
    @SensitiveGeoJsonApi
    public val jsonFormat: Json = Json {
        @OptIn(ExperimentalSerializationApi::class)
        classDiscriminatorMode = ClassDiscriminatorMode.NONE
        ignoreUnknownKeys = true
    }

    /**
     * Decode a GeoJSON [string] into a [GeoJsonObject].
     *
     * Warning: If `T` is a generic type (like [Feature]), this call will result in an unchecked
     * cast. For safe deserialization, prefer to use the `.fromJson` companion method of the type
     * you wish to deserialize (for example, [Feature.fromJson]).
     */
    @SensitiveGeoJsonApi
    public inline fun <reified T : GeoJsonObject> decodeFromString(
        @Language("json") string: String
    ): T =
        jsonFormat.decodeFromString(serializer<GeoJsonObject>(), string) as? T
            ?: throw SerializationException("Object is not a ${T::class.simpleName}")

    /**
     * @return null if the [string] could not be deserialized into [T]
     * @see [decodeFromString]
     */
    @SensitiveGeoJsonApi
    public inline fun <reified T : GeoJsonObject> decodeFromStringOrNull(
        @Language("json") string: String
    ): T? =
        try {
            decodeFromString<T>(string)
        } catch (_: IllegalArgumentException) {
            null
        }

    /** Encode [value] into a GeoJSON [String]. */
    @OptIn(SensitiveGeoJsonApi::class)
    public inline fun <reified T : GeoJsonObject> encodeToString(value: T): String =
        jsonFormat.encodeToString(serializer<T>(), value)

    @OptIn(SensitiveGeoJsonApi::class)
    public inline fun <reified T : GeoJsonElement?> decodeFromString2(
        @Language("json") string: String
    ): T = jsonFormat.decodeFromString(serializer<T>(), string)

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
