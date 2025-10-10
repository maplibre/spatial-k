package org.maplibre.spatialk.geojson

import kotlin.jvm.JvmName
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic
import kotlin.jvm.JvmSynthetic
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import org.intellij.lang.annotations.Language
import org.maplibre.spatialk.geojson.serialization.FeatureSerializer

/**
 * A feature object represents a spatially bounded thing.
 *
 * @property geometry A [Geometry] object contained within the feature.
 * @property properties Additional properties about this feature. When serialized, any non-simple
 *   types will be serialized into JSON objects.
 * @property id An optionally included string that commonly identifies this feature.
 * @see <a href="https://tools.ietf.org/html/rfc7946#section-3.2">
 *   https://tools.ietf.org/html/rfc7946#section-3.2</a>
 * @see FeatureCollection
 */
@Serializable(with = FeatureSerializer::class)
public data class Feature<out T : Geometry?, out P : @Serializable Any?>
@JvmOverloads
constructor(
    public val geometry: T,
    public val properties: P,
    public val id: String? = null,
    override val bbox: BoundingBox? = null,
) : GeoJsonObject {

    public companion object {
        @JvmSynthetic
        @JvmName("inlineFromJson")
        public inline fun <reified T : Geometry?, reified P : @Serializable Any?> fromJson(
            @Language("json") json: String
        ): Feature<T, P> = GeoJson.decodeFromString(json)

        @JvmSynthetic
        @JvmName("inlineFromJsonOrNull")
        public inline fun <reified T : Geometry?, reified P : @Serializable Any?> fromJsonOrNull(
            @Language("json") json: String
        ): Feature<T, P>? = GeoJson.decodeFromStringOrNull(json)

        // Publish for Java; Kotlin should use the inline reified version
        @PublishedApi
        @JvmStatic
        internal fun fromJson(json: String): Feature<*, JsonObject?> =
            GeoJson.decodeFromString<Feature<Geometry?, JsonObject?>>(json)

        // Publish for Java; Kotlin should use the inline reified version
        @PublishedApi
        @JvmStatic
        internal fun fromJsonOrNull(json: String): Feature<*, JsonObject?>? =
            GeoJson.decodeFromStringOrNull<Feature<Geometry?, JsonObject?>>(json)

        @JvmStatic
        public fun Feature<*, JsonObject?>.containsProperty(key: String): Boolean =
            properties?.containsKey(key) ?: false

        @JvmStatic
        public fun Feature<*, JsonObject?>.getStringProperty(key: String): String? =
            properties?.get(key)?.let { Json.decodeFromJsonElement(it) }

        @JvmStatic
        public fun Feature<*, JsonObject?>.getDoubleProperty(key: String): Double? =
            properties?.get(key)?.let { Json.decodeFromJsonElement(it) }

        @JvmStatic
        public fun Feature<*, JsonObject?>.getIntProperty(key: String): Int? =
            properties?.get(key)?.let { Json.decodeFromJsonElement(it) }

        @JvmStatic
        public fun Feature<*, JsonObject?>.getBooleanProperty(key: String): Boolean? =
            properties?.get(key)?.let { Json.decodeFromJsonElement(it) }
    }
}
