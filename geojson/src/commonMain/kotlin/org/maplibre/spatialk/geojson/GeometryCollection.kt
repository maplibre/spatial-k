package org.maplibre.spatialk.geojson

import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic
import kotlinx.serialization.Serializable
import org.intellij.lang.annotations.Language
import org.maplibre.spatialk.geojson.serialization.GeometryCollectionSerializer

/**
 * @see <a href="https://tools.ietf.org/html/rfc7946#section-3.1.8">
 *   https://tools.ietf.org/html/rfc7946#section-3.1.8</a>
 * @see Geometry
 */
@Serializable(with = GeometryCollectionSerializer::class)
public data class GeometryCollection
@JvmOverloads
constructor(public val geometries: List<Geometry>, override val bbox: BoundingBox? = null) :
    Geometry, Collection<Geometry> by geometries {

    @JvmOverloads
    public constructor(
        vararg geometries: Geometry,
        bbox: BoundingBox? = null,
    ) : this(geometries.toList(), bbox)

    public override fun toJson(): String = GeoJson.encodeToString(this)

    public companion object {
        @JvmStatic
        public fun fromJson(@Language("json") json: String): GeometryCollection =
            GeoJson.decodeFromString(json)

        @JvmStatic
        public fun fromJsonOrNull(@Language("json") json: String): GeometryCollection? =
            GeoJson.decodeFromStringOrNull(json)
    }
}
