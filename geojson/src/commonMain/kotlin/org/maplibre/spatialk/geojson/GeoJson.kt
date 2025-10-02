package org.maplibre.spatialk.geojson

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.ClassDiscriminatorMode
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
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
        classDiscriminatorMode = ClassDiscriminatorMode.ALL_JSON_OBJECTS
        ignoreUnknownKeys = true
        serializersModule = SerializersModule {
            polymorphic(Geometry::class) {
                subclass(Point::class)
                subclass(MultiPoint::class)
                subclass(LineString::class)
                subclass(MultiLineString::class)
                subclass(Polygon::class)
                subclass(MultiPolygon::class)
                subclass(GeometryCollection::class)
            }
        }
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
}
