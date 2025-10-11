package org.maplibre.spatialk.geojson

import kotlin.jvm.JvmName
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic
import kotlin.jvm.JvmSynthetic
import kotlinx.serialization.Serializable
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
public data class FeatureCollection<out T : Geometry?>
@JvmOverloads
constructor(
    public val features: List<Feature<T>> = emptyList(),
    override val bbox: BoundingBox? = null,
) : Collection<Feature<T>> by features, GeoJsonObject {
    @JvmOverloads
    public constructor(
        vararg features: Feature<T>,
        bbox: BoundingBox? = null,
    ) : this(features.toMutableList(), bbox)

    public override fun toJson(): String =
        GeoJson.encodeToString<FeatureCollection<Geometry?>>(this)

    public companion object {
        @JvmSynthetic
        @JvmName("inlineFromJson")
        public inline fun <reified T : Geometry?> fromJson(
            @Language("json") json: String
        ): FeatureCollection<T> = GeoJson.decodeFromString(json)

        @JvmSynthetic
        @JvmName("inlineFromJsonOrNull")
        public inline fun <reified T : Geometry?> fromJsonOrNull(
            @Language("json") json: String
        ): FeatureCollection<T>? = GeoJson.decodeFromStringOrNull(json)

        // Publish for Java; Kotlin should use the inline reified version
        @PublishedApi
        @JvmStatic
        internal fun fromJson(json: String): FeatureCollection<*> =
            GeoJson.decodeFromString<FeatureCollection<Geometry?>>(json)

        // Publish for Java; Kotlin should use the inline reified version
        @PublishedApi
        @JvmStatic
        internal fun fromJsonOrNull(json: String): FeatureCollection<*>? =
            GeoJson.decodeFromStringOrNull<FeatureCollection<Geometry?>>(json)
    }
}
