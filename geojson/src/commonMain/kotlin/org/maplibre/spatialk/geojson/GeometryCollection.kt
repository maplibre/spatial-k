package org.maplibre.spatialk.geojson

import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.maplibre.spatialk.geojson.serialization.GeoJson

/**
 * @see <a href="https://tools.ietf.org/html/rfc7946#section-3.1.8">
 *   https://tools.ietf.org/html/rfc7946#section-3.1.8</a>
 * @see Geometry
 */
@Serializable
@SerialName("GeometryCollection")
public class GeometryCollection
@JvmOverloads
constructor(public val geometries: List<Geometry>, override val bbox: BoundingBox? = null) :
    Geometry(), Collection<Geometry> by geometries {
    @JvmOverloads
    public constructor(
        vararg geometries: Geometry,
        bbox: BoundingBox? = null,
    ) : this(geometries.toList(), bbox)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as GeometryCollection

        if (geometries != other.geometries) return false
        if (bbox != other.bbox) return false

        return true
    }

    override fun hashCode(): Int {
        var result = geometries.hashCode()
        result = 31 * result + (bbox?.hashCode() ?: 0)
        return result
    }

    override fun json(): String = GeoJson.encodeToString(this)

    public companion object {
        @JvmStatic
        public fun fromJson(json: String): GeometryCollection = fromJson<GeometryCollection>(json)

        @JvmStatic
        public fun fromJsonOrNull(json: String): GeometryCollection? =
            try {
                fromJson(json)
            } catch (_: IllegalArgumentException) {
                null
            }
    }
}
