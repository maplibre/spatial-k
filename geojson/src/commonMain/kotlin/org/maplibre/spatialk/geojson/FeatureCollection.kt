package org.maplibre.spatialk.geojson

import kotlin.jvm.JvmName
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic
import kotlin.jvm.JvmSynthetic
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.json.JsonObject
import org.intellij.lang.annotations.Language
import org.maplibre.spatialk.geojson.serialization.FeatureCollectionSerializer

/**
 * A FeatureCollection object is a collection of [Feature] objects. This class implements the
 * [Collection] interface and can be used as a Collection directly. The list of features contained
 * in this collection is also accessible through the [features] property.
 *
 * @property features The collection of [Feature] objects stored in this collection
 * @see <a href="https://tools.ietf.org/html/rfc7946#section-3.3">
 *   https://tools.ietf.org/html/rfc7946#section-3.2</a>
 */
@Serializable(with = FeatureCollectionSerializer::class)
public data class FeatureCollection<out T : Geometry?, out P : @Serializable Any?>
@JvmOverloads
constructor(
    public val features: List<Feature<T, P>> = emptyList(),
    override val bbox: BoundingBox? = null,
) : Collection<Feature<T, P>> by features, GeoJsonObject {
    @JvmOverloads
    public constructor(
        vararg features: Feature<T, P>,
        bbox: BoundingBox? = null,
    ) : this(features.toMutableList(), bbox)

    public companion object {
        @JvmSynthetic
        @JvmName("inlineFromJson")
        public inline fun <reified T : Geometry?, reified P : @Serializable Any?> fromJson(
            @Language("json") json: String
        ): FeatureCollection<T, P> = GeoJson.decodeFromString(json)

        @JvmSynthetic
        @JvmName("inlineFromJsonOrNull")
        public inline fun <reified T : Geometry?, reified P : @Serializable Any?> fromJsonOrNull(
            @Language("json") json: String
        ): FeatureCollection<T, P>? = GeoJson.decodeFromStringOrNull(json)

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
