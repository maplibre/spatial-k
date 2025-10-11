package org.maplibre.spatialk.geojson

import kotlin.jvm.JvmName
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic
import kotlin.jvm.JvmSynthetic
import kotlinx.serialization.Serializable
import org.intellij.lang.annotations.Language
import org.maplibre.spatialk.geojson.serialization.GeometryCollectionSerializer

/**
 * A collection of multiple, heterogeneous, [Geometry] objects.
 *
 * @see <a href="https://tools.ietf.org/html/rfc7946#section-3.1.8">
 *   https://tools.ietf.org/html/rfc7946#section-3.1.8</a>
 */
@Serializable(with = GeometryCollectionSerializer::class)
public data class GeometryCollection<out T : Geometry>
@JvmOverloads
constructor(public val geometries: List<T>, override val bbox: BoundingBox? = null) :
    Geometry, Collection<T> by geometries {

    @JvmOverloads
    public constructor(
        vararg geometries: T,
        bbox: BoundingBox? = null,
    ) : this(geometries.toList(), bbox)

    public override fun toJson(): String =
        GeoJson.encodeToString<GeometryCollection<Geometry>>(this)

    public companion object {
        @JvmSynthetic
        @JvmName("inlineFromJson")
        public inline fun <reified T : Geometry> fromJson(
            @Language("json") json: String
        ): GeometryCollection<T> = GeoJson.decodeFromString(json)

        @JvmSynthetic
        @JvmName("inlineFromJsonOrNull")
        public inline fun <reified T : Geometry> fromJsonOrNull(
            @Language("json") json: String
        ): GeometryCollection<T>? = GeoJson.decodeFromStringOrNull(json)

        // Publish below for Java; Kotlin should use the inline reified version

        @PublishedApi
        @JvmStatic
        internal fun fromJson(json: String): GeometryCollection<*> =
            GeoJson.decodeFromString<GeometryCollection<Geometry>>(json)

        @PublishedApi
        @JvmStatic
        internal fun fromJsonOrNull(json: String): GeometryCollection<*>? =
            GeoJson.decodeFromStringOrNull<GeometryCollection<Geometry>>(json)
    }
}
