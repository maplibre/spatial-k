package org.maplibre.spatialk.geojson

import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic
import kotlinx.serialization.Serializable
import org.intellij.lang.annotations.Language
import org.maplibre.spatialk.geojson.serialization.GeoUriParser
import org.maplibre.spatialk.geojson.serialization.PointSerializer

/**
 * @see <a href="https://tools.ietf.org/html/rfc7946#section-3.1.2">
 *   https://tools.ietf.org/html/rfc7946#section-3.1.2</a>
 * @see MultiPoint
 */
@Serializable(with = PointSerializer::class)
public data class Point
@JvmOverloads
constructor(public val coordinates: Position, override val bbox: BoundingBox? = null) : Geometry {
    public constructor(
        longitude: Double,
        latitude: Double,
        altitude: Double? = null,
        bbox: BoundingBox? = null,
    ) : this(Position(longitude, latitude, altitude), bbox)

    public override fun toJson(): String = GeoJson.encodeToString(this)

    /**
     * Converts this [Point] to a `geo` URI of the format `geo:lat,lon` or `geo:lat,lon,alt`.
     *
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc7946#section-9">
     *   https://datatracker.ietf.org/doc/html/rfc7946#section-9
     */
    public fun toGeoUri(): String =
        if (coordinates.hasAltitude)
            "geo:${coordinates.latitude},${coordinates.longitude},${coordinates.altitude}"
        else "geo:${coordinates.latitude},${coordinates.longitude}"

    public companion object {
        @JvmStatic
        public fun fromGeoUri(uri: String): Point = Point(GeoUriParser.parsePosition(uri))

        @JvmStatic
        public fun fromJson(@Language("json") json: String): Point = GeoJson.decodeFromString(json)

        @JvmStatic
        public fun fromJsonOrNull(@Language("json") json: String): Point? =
            GeoJson.decodeFromStringOrNull(json)
    }
}
