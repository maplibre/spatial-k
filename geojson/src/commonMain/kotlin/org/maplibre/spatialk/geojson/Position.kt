package org.maplibre.spatialk.geojson

import kotlin.jvm.JvmName
import kotlin.jvm.JvmStatic
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import org.intellij.lang.annotations.Language
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
 * Position(longitude = -75.0, latitude = 45.0)
 * ```
 *
 * Will be serialized as
 *
 * ```json
 * [-75.0,45.0]
 * ```
 *
 * See [RFC 7946 Section 3.1.1](https://tools.ietf.org/html/rfc7946#section-3.1.1) for the full
 * specification.
 *
 * @property latitude The latitude value of this position (or northing value for projected
 *   coordinates) in degrees.
 * @property longitude The longitude value of this position (or easting value for projected
 *   coordinates) in degrees.
 * @property altitude Optionally, an altitude or elevation for this position in meters above or
 *   below the [WGS84](https://en.wikipedia.org/wiki/World_Geodetic_System#WGS_84) reference
 *   ellipsoid.
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

    /**
     * Construct a [Position] with longitude and latitude.
     *
     * @param longitude The longitude value in degrees.
     * @param latitude The latitude value in degrees.
     */
    public constructor(
        longitude: Double,
        latitude: Double,
    ) : this(doubleArrayOf(longitude, latitude))

    /**
     * Construct a [Position] with longitude, latitude, and altitude.
     *
     * @param longitude The longitude value in degrees.
     * @param latitude The latitude value in degrees.
     * @param altitude The altitude value in meters.
     */
    public constructor(
        longitude: Double,
        latitude: Double,
        altitude: Double,
    ) : this(doubleArrayOf(longitude, latitude, altitude))

    /**
     * Construct a [Position] with longitude, latitude, and optional altitude.
     *
     * @param longitude The longitude value in degrees.
     * @param latitude The latitude value in degrees.
     * @param altitude The altitude value in meters, or null if not specified.
     */
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
     *
     * As noted in
     * [RFC 7946 Section 3.1.1](https://datatracker.ietf.org/doc/html/rfc7946#section-3.1.1):
     * > Implementations SHOULD NOT extend positions beyond three elements because the semantics of
     * > extra elements are unspecified and ambiguous. Historically, some implementations have used
     * > a fourth element to carry a linear referencing measure (sometimes denoted as "M") or a
     * > numerical timestamp, but in most situations a parser will not be able to properly interpret
     * > these values.
     *
     * @param longitude The longitude value in degrees.
     * @param latitude The latitude value in degrees.
     * @param altitude The altitude value in meters.
     * @param additionalElements Additional coordinate elements beyond the standard three axes.
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

    /**
     * Get the coordinate at the given index.
     *
     * @param index The index of the coordinate to retrieve.
     * @return The coordinate at the given index.
     */
    public operator fun get(index: Int): Double = coordinates[index]

    /** @return the coordinate at the given index or null if the index is out of range. */
    public fun getOrNull(index: Int): Double? = coordinates.getOrNull(index)

    /** The number of elements in the coordinates array. */
    public val size: Int
        get() = coordinates.size

    /** Whether this position has an altitude component. */
    @get:JvmName("hasAltitude")
    public val hasAltitude: Boolean
        get() = size >= 3

    public override fun iterator(): Iterator<Double> = coordinates.iterator()

    /**
     * Destructuring component for [longitude].
     *
     * @return The longitude value.
     */
    public operator fun component1(): Double = longitude

    /**
     * Destructuring component for [latitude].
     *
     * @return The latitude value.
     */
    public operator fun component2(): Double = latitude

    /**
     * Destructuring component for [altitude].
     *
     * @return The altitude value, or null if not present.
     */
    public operator fun component3(): Double? = altitude

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
        return "Position(longitude=$longitude, latitude=$latitude, altitude=$altitude)"
    }

    /**
     * Serialize this position to a JSON string.
     *
     * @return A JSON string representation of this position.
     */
    public fun toJson(): String = GeoJson.jsonFormat.encodeToString(this)

    /** Factory methods for creating and serializing [Position] objects. */
    public companion object {
        /**
         * Deserialize a [Position] from a JSON string.
         *
         * @param json A JSON string representing a position.
         * @return The deserialized position.
         * @throws SerializationException if the JSON string is invalid or cannot be deserialized.
         * @throws IllegalArgumentException if the JSON contains an invalid [Position].
         */
        @JvmStatic
        public fun fromJson(@Language("json") json: String): Position =
            GeoJson.jsonFormat.decodeFromString(json)

        /**
         * Deserialize a [Position] from a JSON string, returning null if the JSON is invalid.
         *
         * @param json A JSON string representing a position.
         * @return The deserialized position, or null if the JSON is invalid.
         */
        @JvmStatic
        public fun fromJsonOrNull(@Language("json") json: String): Position? =
            try {
                GeoJson.jsonFormat.decodeFromString(json)
            } catch (_: IllegalArgumentException) {
                null
            }
    }
}
