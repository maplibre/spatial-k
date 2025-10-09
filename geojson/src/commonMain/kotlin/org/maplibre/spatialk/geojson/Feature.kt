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
public data class Feature<out T : Geometry?>
@JvmOverloads
constructor(
    public val geometry: T,
    public val properties: JsonObject? = null,
    public val id: String? = null,
    override val bbox: BoundingBox? = null,
) : GeoJsonObject {

    public fun containsProperty(key: String): Boolean = properties?.containsKey(key) ?: false

    public fun getStringProperty(key: String): String? =
        properties?.get(key)?.let { Json.decodeFromJsonElement(it) }

    public fun getDoubleProperty(key: String): Double? =
        properties?.get(key)?.let { Json.decodeFromJsonElement(it) }

    public fun getIntProperty(key: String): Int? =
        properties?.get(key)?.let { Json.decodeFromJsonElement(it) }

    public fun getBooleanProperty(key: String): Boolean? =
        properties?.get(key)?.let { Json.decodeFromJsonElement(it) }

    public override fun toJson(): String = GeoJson.encodeToString<Feature<Geometry?>>(this)

    public companion object {
        @JvmSynthetic
        @JvmName("fromJsonAsT")
        public inline fun <reified T : Geometry?> fromJson(
            @Language("json") json: String
        ): Feature<T> = GeoJson.decodeFromString(json)

        @JvmSynthetic
        @JvmName("fromJsonOrNullAsT")
        public inline fun <reified T : Geometry?> fromJsonOrNull(
            @Language("json") json: String
        ): Feature<T>? = GeoJson.decodeFromStringOrNull(json)

        @PublishedApi // Publish for Java; Kotlin should use the inline reified version
        @JvmStatic
        @Suppress("Unused")
        internal fun fromJson(json: String): Feature<*> =
            GeoJson.decodeFromString<Feature<Geometry?>>(json)

        @PublishedApi // Publish for Java; Kotlin should use the inline reified version
        @JvmStatic
        @Suppress("Unused")
        internal fun fromJsonOrNull(json: String): Feature<*>? =
            GeoJson.decodeFromStringOrNull<Feature<Geometry?>>(json)
    }
}
