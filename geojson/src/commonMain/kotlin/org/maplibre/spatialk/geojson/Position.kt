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
public class Position internal constructor(internal val coordinates: DoubleArray) :
    Iterable<Double> {
    init {
        require(coordinates.size >= 2) { "At least two coordinates must be provided" }
    }

    // We need to manually write our overloads to prevent Position(0.0, 0.0, 0.0) from calling the
    // sensitive constructor with zero varargs.

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
        altitude: Double? = null,
    ) : this(
        if (altitude == null) doubleArrayOf(longitude, latitude)
        else doubleArrayOf(longitude, latitude, altitude)
    )

    /**
     * Construct a [Position] with more than the standard three axes ([longitude], [latitude],
     * [altitude]).
     * > Implementations SHOULD NOT extend positions beyond three elements because the semantics of
     * > extra elements are unspecified and ambiguous. Historically, some implementations have used
     * > a fourth element to carry a linear referencing measure (sometimes denoted as "M") or a
     * > numerical timestamp, but in most situations a parser will not be able to properly interpret
     * > these values.
     *
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc7946#section-3.1.1">
     *   https://datatracker.ietf.org/doc/html/rfc7946#section-3.1.1</a>
     */
    @SensitiveGeoJsonApi
    public constructor(
        longitude: Double,
        latitude: Double,
        altitude: Double,
        vararg additionalElements: Double,
    ) : this(doubleArrayOf(longitude, latitude, altitude, *additionalElements))

    public val longitude: Double
        get() = coordinates[0]

    public val latitude: Double
        get() = coordinates[1]

    public val altitude: Double?
        get() = if (hasAltitude) coordinates[2] else null

    /** @return the coordinate at the given index. */
    public operator fun get(index: Int): Double = coordinates[index]

    /** @return the coordinate at the given index or null if the index is out of range. */
    public fun getOrNull(index: Int): Double? = coordinates.getOrNull(index)

    /** @return the number of elements in the coordinates array. */
    public val size: Int
        get() = coordinates.size

    public val hasAltitude: Boolean
        get() = size >= 3

    public override fun iterator(): Iterator<Double> = coordinates.iterator()

    /** @return [longitude] */
    @JvmSynthetic public operator fun component1(): Double = longitude

    /** @return [latitude] */
    @JvmSynthetic public operator fun component2(): Double = latitude

    /** @return [altitude] */
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
