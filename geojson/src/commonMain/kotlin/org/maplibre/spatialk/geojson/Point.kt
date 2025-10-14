package org.maplibre.spatialk.geojson

import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import org.intellij.lang.annotations.Language
import org.maplibre.spatialk.geojson.serialization.GeoUriParser
import org.maplibre.spatialk.geojson.serialization.PointSerializer

/**
 * A [Point] geometry represents a single [Position] in coordinate space.
 *
 * See [RFC 7946 Section 3.1.2](https://tools.ietf.org/html/rfc7946#section-3.1.2) for the full
 * specification.
 *
 * @see MultiPoint
 */
@Serializable(with = PointSerializer::class)
public data class Point
@JvmOverloads
constructor(
    /** The [Position] of this [Point]. */
    public val coordinates: Position,
    /** The [BoundingBox] of this [Point]. */
    override val bbox: BoundingBox? = null,
) : SingleGeometry, PointGeometry {

    /** The longitude value of this [Point] in degrees. */
    public val longitude: Double
        get() = coordinates.longitude

    /** The latitude value of this [Point] in degrees. */
    public val latitude: Double
        get() = coordinates.latitude

    /**
     * Create a [Point] from individual coordinate components.
     *
     * @param longitude The longitude of the [Point].
     * @param latitude The latitude of the [Point].
     * @param altitude The altitude of the [Point], or null if not specified.
     * @param bbox The [BoundingBox] of this [Point].
     */
    @JvmOverloads
    public constructor(
        longitude: Double,
        latitude: Double,
        altitude: Double? = null,
        bbox: BoundingBox? = null,
    ) : this(Position(longitude, latitude, altitude), bbox)

    /**
     * Converts this [Point] to a `geo` URI of the format `geo:lat,lon` or `geo:lat,lon,alt` as
     * defined in [RFC 7946 Section 9](https://datatracker.ietf.org/doc/html/rfc7946#section-9).
     *
     * @return A geo URI string representing this point.
     * @see fromGeoUri
     */
    public fun toGeoUri(): String =
        if (coordinates.hasAltitude)
            "geo:${coordinates.latitude},${coordinates.longitude},${coordinates.altitude}"
        else "geo:${coordinates.latitude},${coordinates.longitude}"

    /** Factory methods for creating and serializing [Point] objects. */
    public companion object {
        /**
         * Create a [Point] from a `geo` URI string.
         *
         * The `geo` URI format is defined in
         * [RFC 7946 Section 9](https://datatracker.ietf.org/doc/html/rfc7946#section-9).
         *
         * @param uri The geo URI string to parse.
         * @return A [Point] parsed from the URI.
         * @throws IllegalArgumentException if [uri] is not a valid GeoURI.
         * @see toGeoUri
         */
        @JvmStatic
        public fun fromGeoUri(uri: String): Point = Point(GeoUriParser.parsePosition(uri))

        /**
         * Deserialize a [Point] from a JSON string.
         *
         * @param json The JSON string to parse.
         * @return The deserialized [Point].
         * @throws SerializationException if the JSON string is invalid or cannot be deserialized.
         * @throws IllegalArgumentException if the geometry does not meet structural requirements.
         */
        @JvmStatic
        public fun fromJson(@Language("json") json: String): Point = GeoJson.decodeFromString(json)

        /**
         * Deserialize a [Point] from a JSON string, or null if parsing fails.
         *
         * @param json The JSON string to parse.
         * @return The deserialized [Point], or null if parsing fails.
         */
        @JvmStatic
        public fun fromJsonOrNull(@Language("json") json: String): Point? =
            GeoJson.decodeFromStringOrNull(json)

        @PublishedApi @JvmStatic internal fun toJson(point: Point): String = point.toJson()
    }
}
