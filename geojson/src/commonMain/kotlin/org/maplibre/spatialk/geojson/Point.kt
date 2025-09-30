package org.maplibre.spatialk.geojson

import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.intellij.lang.annotations.Language
import org.maplibre.spatialk.geojson.serialization.GeoJson

/**
 * @see <a href="https://tools.ietf.org/html/rfc7946#section-3.1.2">
 *   https://tools.ietf.org/html/rfc7946#section-3.1.2</a>
 * @see MultiPoint
 */
@Serializable
@SerialName("Point")
public data class Point
@JvmOverloads
constructor(public val coordinates: Position, override val bbox: BoundingBox? = null) : Geometry() {
    @JvmOverloads
    public constructor(
        coordinates: DoubleArray,
        bbox: BoundingBox? = null,
    ) : this(Position(coordinates), bbox)

    public constructor(
        longitude: Double,
        latitude: Double,
        altitude: Double? = null,
        bbox: BoundingBox? = null,
    ) : this(Position(longitude, latitude, altitude), bbox)

    override fun json(): String = GeoJson.encodeToString(this)

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
        public fun fromJson(@Language("json") json: String): Point = fromJson<Point>(json)

        @JvmStatic
        public fun fromJsonOrNull(@Language("json") json: String): Point? =
            try {
                fromJson(json)
            } catch (_: IllegalArgumentException) {
                null
            }

        @JvmStatic
        public fun fromGeoUri(uri: String): Point = Point(GeoUriParser.parsePosition(uri))
    }
}
