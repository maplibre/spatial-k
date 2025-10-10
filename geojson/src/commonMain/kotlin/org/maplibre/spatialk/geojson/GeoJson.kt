package org.maplibre.spatialk.geojson

import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import org.intellij.lang.annotations.Language

public data object GeoJson {
    /** The default Json configuration for GeoJson objects. */
    public val jsonFormat: Json = Json {
        ignoreUnknownKeys = true
        serializersModule = SerializersModule {
            polymorphicDefaultSerializer(GeoJsonObject::class) {
                val serializer =
                    when (it) {
                        is Point -> Point.serializer()
                        is MultiPoint -> MultiPoint.serializer()
                        is LineString -> LineString.serializer()
                        is MultiLineString -> MultiLineString.serializer()
                        is Polygon -> Polygon.serializer()
                        is MultiPolygon -> MultiPolygon.serializer()
                        is GeometryCollection -> GeometryCollection.serializer()
                        is Feature<*> -> Feature.serializer(Geometry.serializer().nullable)
                        is FeatureCollection -> FeatureCollection.serializer()
                    }
                @Suppress("UNCHECKED_CAST")
                serializer as SerializationStrategy<GeoJsonObject>
            }
        }
    }

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
