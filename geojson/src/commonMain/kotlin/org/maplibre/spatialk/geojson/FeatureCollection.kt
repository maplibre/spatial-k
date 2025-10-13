package org.maplibre.spatialk.geojson

import kotlin.jvm.JvmName
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic
import kotlin.jvm.JvmSynthetic
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.json.JsonObject
import org.intellij.lang.annotations.Language
import org.maplibre.spatialk.geojson.serialization.FeatureCollectionSerializer

/**
 * A [FeatureCollection] object is a collection of [Feature] objects. This class implements the
 * [Collection] interface and can be used as a collection directly. The list of [Feature] objects
 * contained in this [FeatureCollection] is also accessible through the [features] property.
 *
 * See [RFC 7946 Section 3.3](https://tools.ietf.org/html/rfc7946#section-3.3) for the full
 * specification.
 *
 * @property features The collection of [Feature] objects stored in this [FeatureCollection]
 */
@Serializable(with = FeatureCollectionSerializer::class)
public data class FeatureCollection<out G : Geometry?, out P : @Serializable Any?>
@JvmOverloads
constructor(
    public val features: List<Feature<G, P>> = emptyList(),
    override val bbox: BoundingBox? = null,
) : Collection<Feature<G, P>> by features, GeoJsonObject {
    /**
     * Constructs a [FeatureCollection] from a vararg of [Feature] objects.
     *
     * @param features The [Feature] objects to include in this [FeatureCollection].
     * @param bbox The [BoundingBox] for this [FeatureCollection].
     */
    @JvmOverloads
    public constructor(
        vararg features: Feature<G, P>,
        bbox: BoundingBox? = null,
    ) : this(features.toMutableList(), bbox)

    /** Factory methods for creating and serializing [FeatureCollection] objects. */
    public companion object {
        /**
         * Deserializes a [FeatureCollection] from a JSON string.
         *
         * @param json The JSON string to deserialize.
         * @return The deserialized [FeatureCollection].
         * @throws SerializationException if the JSON string is invalid or cannot be deserialized.
         * @throws IllegalArgumentException if the JSON contains an invalid [FeatureCollection].
         */
        @JvmSynthetic
        @JvmName("inlineFromJson")
        public inline fun <reified G : Geometry?, reified P : @Serializable Any?> fromJson(
            @Language("json") json: String
        ): FeatureCollection<G, P> = GeoJson.decodeFromString(json)

        /**
         * Deserializes a [FeatureCollection] from a JSON string, or returns null on failure.
         *
         * @param json The JSON string to deserialize.
         * @return The deserialized [FeatureCollection], or null if deserialization fails.
         */
        @JvmSynthetic
        @JvmName("inlineFromJsonOrNull")
        public inline fun <reified G : Geometry?, reified P : @Serializable Any?> fromJsonOrNull(
            @Language("json") json: String
        ): FeatureCollection<G, P>? = GeoJson.decodeFromStringOrNull(json)

        // Publish for Java below; Kotlin should use the inline reified versions above

        @PublishedApi
        @JvmStatic
        internal fun fromJson(json: String): FeatureCollection<Geometry?, JsonObject?> =
            GeoJson.decodeFromString<FeatureCollection<Geometry?, JsonObject?>>(json)

        @PublishedApi
        @JvmStatic
        internal fun fromJsonOrNull(json: String): FeatureCollection<Geometry?, JsonObject?>? =
            GeoJson.decodeFromStringOrNull<FeatureCollection<Geometry?, JsonObject?>>(json)

        @PublishedApi
        @JvmStatic
        internal fun toJson(featureCollection: FeatureCollection<Geometry?, JsonObject?>): String =
            featureCollection.toJson()

        @PublishedApi
        @JvmStatic
        internal fun <T> toJson(
            featureCollection: FeatureCollection<Geometry?, T>,
            propertiesSerializer: KSerializer<T>,
        ): String =
            GeoJson.jsonFormat.encodeToString(
                serializer(Geometry.serializer().nullable, propertiesSerializer),
                featureCollection,
            )
    }
}
