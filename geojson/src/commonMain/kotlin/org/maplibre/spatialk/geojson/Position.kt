package org.maplibre.spatialk.geojson

import kotlin.jvm.JvmSynthetic
import kotlinx.serialization.Serializable
import org.maplibre.spatialk.geojson.serialization.GeoJson
import org.maplibre.spatialk.geojson.serialization.PositionSerializer

/**
 * A [Position] is the fundamental geometry construct.
 *
 * In JSON, a position is an array of numbers. There MUST be two or more elements. The first two
 * elements are [longitude] and [latitude], or easting and northing, precisely in that order using
 * decimal numbers. [Altitude][altitude] or elevation MAY be included as an optional third element.
 *
 * When serialized, the [latitude], [longitude], and [altitude] (if present) will be represented as
 * an array.
 *
 * ```kotlin
 * LngLat(longitude = -75.0, latitude = 45.0)
 * ```
 *
 * Will be serialized as
 *
 * ```json
 * [-75.0,45.0]
 * ```
 *
 * @property latitude The latitude value of this position (or northing value for projected
 *   coordinates) in degrees.
 * @property longitude The longitude value of this position (or easting value for projected
 *   coordinates) in degrees.
 * @property altitude Optionally, an altitude or elevation for this position in meters above or
 *   below the [WGS84](https://en.wikipedia.org/wiki/World_Geodetic_System#WGS_84) reference
 *   ellipsoid.
 * @see <a href="https://tools.ietf.org/html/rfc7946#section-3.1.1">
 *   https://tools.ietf.org/html/rfc7946#section-3.1.1</a>
 * @see PositionSerializer
 */
@Serializable(with = PositionSerializer::class)
public class Position(public val coordinates: DoubleArray) {
    init {
        require(coordinates.size >= 2) { "At least two coordinates must be provided" }
    }

    public constructor(
        longitude: Double,
        latitude: Double,
    ) : this(doubleArrayOf(longitude, latitude))

    public constructor(
        longitude: Double,
        latitude: Double,
        altitude: Double,
    ) : this(doubleArrayOf(longitude, latitude, altitude))

    public constructor(
        longitude: Double,
        latitude: Double,
        altitude: Double?,
    ) : this(
        when (altitude) {
            null -> doubleArrayOf(longitude, latitude)
            else -> doubleArrayOf(longitude, latitude, altitude)
        }
    )

    public val longitude: Double
        get() = coordinates[0]

    public val latitude: Double
        get() = coordinates[1]

    public val altitude: Double?
        get() = coordinates.getOrNull(2)

    /**
     * Component function for getting the [longitude]
     *
     * @return [longitude]
     */
    @JvmSynthetic public operator fun component1(): Double = longitude

    /**
     * Component function for getting the [latitude]
     *
     * @return [latitude]
     */
    @JvmSynthetic public operator fun component2(): Double = latitude

    /**
     * Component function for getting the [altitude]
     *
     * @return [altitude]
     */
    @JvmSynthetic public operator fun component3(): Double? = altitude

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Position

        return coordinates.contentEquals(other.coordinates)
    }

    override fun hashCode(): Int {
        return coordinates.contentHashCode()
    }

    override fun toString(): String {
        return "LngLat(longitude=$longitude, latitude=$latitude, altitude=$altitude)"
    }

    public fun json(): String = GeoJson.encodeToString(coordinates)
}

public val Position.hasAltitude: Boolean
    get() = coordinates.size >= 3
