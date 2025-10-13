package org.maplibre.spatialk.geojson

import kotlin.jvm.JvmName
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic
import kotlin.jvm.JvmSynthetic
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import org.intellij.lang.annotations.Language
import org.maplibre.spatialk.geojson.serialization.FeatureSerializer

/**
 * A [Feature] object represents a spatially bounded thing.
 *
 * See [RFC 7946 Section 3.2](https://tools.ietf.org/html/rfc7946#section-3.2) for the full
 * specification.
 *
 * @param G The type of [Geometry] contained in this [Feature].
 * @param P The type of properties. This can be any type that serializes to a JSON object. For
 *   dynamic or unknown property schemas, use [JsonObject]. For known schemas, use a [Serializable]
 *   data class.
 * @property geometry A [Geometry] object contained within the [Feature].
 * @property properties Additional properties about this [Feature]. It should be serializable into a
 *   [JsonObject].
 * @property id An optionally included string that commonly identifies this [Feature].
 * @see FeatureCollection
 */
@Serializable(with = FeatureSerializer::class)
public data class Feature<out G : Geometry?, out P : @Serializable Any?>
@JvmOverloads
constructor(
    public val geometry: G,
    public val properties: P,
    public val id: String? = null,
    override val bbox: BoundingBox? = null,
) : GeoJsonObject {

    /** Factory methods for creating and serializing [Feature] objects. */
    public companion object {
        /**
         * Deserializes a [Feature] from a JSON string.
         *
         * @param json The JSON string to deserialize.
         * @return The deserialized [Feature].
         * @throws SerializationException if the JSON string is invalid or cannot be deserialized.
         * @throws IllegalArgumentException if the JSON contains an invalid [Feature].
         */
        @JvmSynthetic
        @JvmName("inlineFromJson")
        public inline fun <reified G : Geometry?, reified P : @Serializable Any?> fromJson(
            @Language("json") json: String
        ): Feature<G, P> = GeoJson.decodeFromString(json)

        /**
         * Deserializes a [Feature] from a JSON string, or returns null on failure.
         *
         * @param json The JSON string to deserialize.
         * @return The deserialized [Feature], or null if deserialization fails.
         */
        @JvmSynthetic
        @JvmName("inlineFromJsonOrNull")
        public inline fun <reified G : Geometry?, reified P : @Serializable Any?> fromJsonOrNull(
            @Language("json") json: String
        ): Feature<G, P>? = GeoJson.decodeFromStringOrNull(json)

        // Publish for Java below; Kotlin should use the inline reified versions above

        @PublishedApi
        @JvmStatic
        internal fun fromJson(json: String): Feature<Geometry?, JsonObject?> =
            GeoJson.decodeFromString<Feature<Geometry?, JsonObject?>>(json)

        @PublishedApi
        @JvmStatic
        internal fun fromJsonOrNull(json: String): Feature<Geometry?, JsonObject?>? =
            GeoJson.decodeFromStringOrNull<Feature<Geometry?, JsonObject?>>(json)

        @PublishedApi
        @JvmStatic
        internal fun toJson(feature: Feature<Geometry?, JsonObject?>): String = feature.toJson()

        @PublishedApi
        @JvmStatic
        internal fun <T> toJson(
            feature: Feature<Geometry?, T>,
            propertiesSerializer: KSerializer<T>,
        ): String =
            GeoJson.jsonFormat.encodeToString(
                serializer(Geometry.serializer().nullable, propertiesSerializer),
                feature,
            )

        // JsonObject property accessors

        /**
         * Checks if a property exists in the feature's [JsonObject] properties.
         *
         * @param key The property key to check.
         * @return True if the property exists, false otherwise.
         * @receiver The feature to check.
         */
        @JvmStatic
        public fun Feature<*, JsonObject?>.containsProperty(key: String): Boolean =
            properties?.containsKey(key) ?: false

        /**
         * Gets a string property from the feature's [JsonObject] properties.
         *
         * @param key The property key.
         * @return The string value, or null if the property doesn't exist or isn't a string.
         * @receiver The feature to get the property from.
         */
        @JvmStatic
        public fun Feature<*, JsonObject?>.getStringProperty(key: String): String? =
            properties?.get(key)?.let { Json.decodeFromJsonElement(it) }

        /**
         * Gets a double property from the feature's [JsonObject] properties.
         *
         * @param key The property key.
         * @return The double value, or null if the property doesn't exist or isn't a number.
         * @receiver The feature to get the property from.
         */
        @JvmStatic
        public fun Feature<*, JsonObject?>.getDoubleProperty(key: String): Double? =
            properties?.get(key)?.let { Json.decodeFromJsonElement(it) }

        /**
         * Gets an integer property from the feature's [JsonObject] properties.
         *
         * @param key The property key.
         * @return The integer value, or null if the property doesn't exist or isn't a number.
         * @receiver The feature to get the property from.
         */
        @JvmStatic
        public fun Feature<*, JsonObject?>.getIntProperty(key: String): Int? =
            properties?.get(key)?.let { Json.decodeFromJsonElement(it) }

        /**
         * Gets a boolean property from the feature's [JsonObject] properties.
         *
         * @param key The property key.
         * @return The boolean value, or null if the property doesn't exist or isn't a boolean.
         * @receiver The feature to get the property from.
         */
        @JvmStatic
        public fun Feature<*, JsonObject?>.getBooleanProperty(key: String): Boolean? =
            properties?.get(key)?.let { Json.decodeFromJsonElement(it) }
    }
}
