package org.maplibre.spatialk.geojson

import kotlin.jvm.JvmName
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic
import kotlin.jvm.JvmSynthetic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import org.intellij.lang.annotations.Language
import org.maplibre.spatialk.geojson.serialization.FeatureGeometrySerializer

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
@Serializable
@SerialName("Feature")
public data class Feature<out T : Geometry?>
@JvmOverloads
constructor(
    @Suppress("SERIALIZER_TYPE_INCOMPATIBLE")
    @Serializable(with = FeatureGeometrySerializer::class)
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

    public companion object {
        @JvmSynthetic // See below for Java-facing API
        @JvmName("__fromJson") // Prevent clash with Java-facing API
        @OptIn(SensitiveGeoJsonApi::class)
        public inline fun <reified T : Geometry?> fromJson(
            @Language("json") json: String
        ): Feature<T> {
            @Suppress("UNCHECKED_CAST") // checked in `.also` block
            return GeoJson.decodeFromString<Feature<*>>(json).also {
                if (it.geometry !is T)
                    throw SerializationException("Object is not a Feature<${T::class.simpleName}>")
            } as Feature<T>
        }

        @JvmSynthetic // See below for Java-facing API
        @JvmName("__fromJsonOrNull") // Prevent clash with Java-facing API
        @OptIn(SensitiveGeoJsonApi::class)
        public inline fun <reified T : Geometry?> fromJsonOrNull(
            @Language("json") json: String
        ): Feature<T>? {
            @Suppress("UNCHECKED_CAST") // checked in `.also` block
            return GeoJson.decodeFromStringOrNull<Feature<*>>(json).also {
                if (it?.geometry !is T) return null
            } as Feature<T>?
        }

        @PublishedApi // Publish for Java; Kotlin should use the inline reified version
        @JvmName("fromJson")
        @JvmStatic
        @Suppress("FunctionName", "Unused")
        @OptIn(SensitiveGeoJsonApi::class)
        internal fun __fromJson(json: String): Feature<*> =
            GeoJson.decodeFromString<Feature<Geometry>>(json)

        @PublishedApi // Publish for Java; Kotlin should use the inline reified version
        @JvmName("fromJsonOrNull")
        @JvmStatic
        @Suppress("FunctionName", "Unused")
        @OptIn(SensitiveGeoJsonApi::class)
        internal fun __fromJsonOrNull(json: String): Feature<*>? =
            GeoJson.decodeFromStringOrNull<Feature<Geometry>>(json)
    }
}
