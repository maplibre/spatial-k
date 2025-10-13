package org.maplibre.spatialk.geojson

import kotlin.jvm.JvmName
import kotlin.jvm.JvmStatic
import kotlin.math.min
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import org.intellij.lang.annotations.Language
import org.maplibre.spatialk.geojson.serialization.BoundingBoxSerializer

/**
 * Represents an area bounded by a [northeast] and [southwest] [Position].
 *
 * A [GeoJsonObject] MAY have a member named "bbox" to include information on the coordinate range
 * for its [Geometry] objects, [Feature] objects, or [FeatureCollection] objects.
 *
 * When serialized, a [BoundingBox] is represented as an array of length 2*n where n is the number
 * of dimensions represented in the contained geometries, with all axes of the most southwesterly
 * point followed by all axes of the northeasterly point. The axes order of a [BoundingBox] follows
 * the axes order of geometries.
 *
 * For the [BoundingBox] to be serialized in 3D form, both [Position] objects must have a defined
 * altitude.
 *
 * See [RFC 7946 Section 5](https://tools.ietf.org/html/rfc7946#section-5) for the full
 * specification.
 *
 * @property northeast The northeastern corner of the [BoundingBox]
 * @property southwest The southwestern corner of the [BoundingBox]
 * @property coordinates The GeoJSON bounding box coordinate array
 */
@Serializable(with = BoundingBoxSerializer::class)
public class BoundingBox internal constructor(internal val coordinates: DoubleArray) :
    Iterable<Double> {
    init {
        require(coordinates.size >= 4 && coordinates.size % 2 == 0) {
            "Bounding Box coordinates must have at least 4 and an even number of values"
        }
    }

    /**
     * Construct a 2D [BoundingBox] from west, south, east, and north coordinates.
     *
     * @param west The western longitude boundary.
     * @param south The southern latitude boundary.
     * @param east The eastern longitude boundary.
     * @param north The northern latitude boundary.
     */
    public constructor(
        west: Double,
        south: Double,
        east: Double,
        north: Double,
    ) : this(doubleArrayOf(west, south, east, north))

    /**
     * Construct a 3D [BoundingBox] from west, south, east, north coordinates with altitude bounds.
     *
     * @param west The western longitude boundary.
     * @param south The southern latitude boundary.
     * @param minAltitude The minimum altitude boundary.
     * @param east The eastern longitude boundary.
     * @param north The northern latitude boundary.
     * @param maxAltitude The maximum altitude boundary.
     */
    public constructor(
        west: Double,
        south: Double,
        minAltitude: Double,
        east: Double,
        north: Double,
        maxAltitude: Double,
    ) : this(doubleArrayOf(west, south, minAltitude, east, north, maxAltitude))

    /**
     * Construct a [BoundingBox] from two [Position] objects that represent the southwest corner and
     * northeast corners.
     *
     * If one corner has more elements than the other, the extra elements are ignored.
     *
     * @param southwest The southwestern corner [Position].
     * @param northeast The northeastern corner [Position].
     */
    public constructor(
        southwest: Position,
        northeast: Position,
    ) : this(
        min(southwest.size, northeast.size).let { size ->
            southwest.coordinates.sliceArray(0..<size) + northeast.coordinates.sliceArray(0..<size)
        }
    )

    /**
     * Construct a [BoundingBox] with more than the standard three axes (`longitude`, `latitude`,
     * `altitude`) per corner.
     *
     * As noted in
     * [RFC 7946 Section 3.1.1](https://datatracker.ietf.org/doc/html/rfc7946#section-3.1.1), such
     * additional axes are discouraged but allowed by the GeoJSON specification:
     * > Implementations SHOULD NOT extend positions beyond three elements because the semantics of
     * > extra elements are unspecified and ambiguous. Historically, some implementations have used
     * > a fourth element to carry a linear referencing measure (sometimes denoted as "M") or a
     * > numerical timestamp, but in most situations a parser will not be able to properly interpret
     * > these values.
     *
     * @param west The western longitude boundary.
     * @param south The southern latitude boundary.
     * @param minAltitude The minimum altitude boundary.
     * @param east The eastern longitude boundary.
     * @param north The northern latitude boundary.
     * @param maxAltitude The maximum altitude boundary.
     * @param additionalElements Additional coordinate elements beyond the standard three axes per
     *   corner (must contain an even number of elements).
     * @throws IllegalArgumentException if [additionalElements] contains an odd number of elements.
     */
    @SensitiveGeoJsonApi
    public constructor(
        west: Double,
        south: Double,
        minAltitude: Double,
        east: Double,
        north: Double,
        maxAltitude: Double,
        vararg additionalElements: Double,
    ) : this(doubleArrayOf(west, south, minAltitude, east, north, maxAltitude, *additionalElements))

    public val southwest: Position
        get() = Position(coordinates.sliceArray(0..<(coordinates.size / 2)))

    public val northeast: Position
        get() = Position(coordinates.sliceArray((coordinates.size / 2)..<coordinates.size))

    /** The western longitude boundary. */
    public val west: Double
        get() = coordinates[0]

    /** The southern latitude boundary. */
    public val south: Double
        get() = coordinates[1]

    /** The minimum altitude boundary, or null if this bounding box is 2D. */
    public val minAltitude: Double?
        get() = if (hasAltitude) coordinates[2] else null

    /** The eastern longitude boundary. */
    public val east: Double
        get() = if (hasAltitude) coordinates[3] else coordinates[2]

    /** The northern latitude boundary. */
    public val north: Double
        get() = if (hasAltitude) coordinates[4] else coordinates[3]

    /** The maximum altitude boundary, or null if this bounding box is 2D. */
    public val maxAltitude: Double?
        get() = if (hasAltitude) coordinates[5] else null

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

    /** Whether this bounding box has altitude bounds. */
    @get:JvmName("hasAltitude")
    public val hasAltitude: Boolean
        get() = coordinates.size >= 6

    override fun iterator(): Iterator<Double> = coordinates.iterator()

    /**
     * Destructuring component for [southwest].
     *
     * @return The southwestern corner [Position].
     */
    public operator fun component1(): Position = southwest

    /**
     * Destructuring component for [northeast].
     *
     * @return The northeastern corner [Position].
     */
    public operator fun component2(): Position = northeast

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as BoundingBox

        return coordinates.contentEquals(other.coordinates)
    }

    override fun hashCode(): Int {
        return coordinates.contentHashCode()
    }

    override fun toString(): String {
        return "BoundingBox(southwest=$southwest, northeast=$northeast)"
    }

    /**
     * Serialize this bounding box to a JSON string.
     *
     * @return A JSON string representation of this bounding box.
     */
    public fun toJson(): String = GeoJson.jsonFormat.encodeToString(this)

    /** Factory methods for creating and serializing [BoundingBox] objects. */
    public companion object {
        /**
         * Deserialize a [BoundingBox] from a JSON string.
         *
         * @param json A JSON string representing a bounding box.
         * @return The deserialized bounding box.
         * @throws SerializationException if the JSON string is invalid or cannot be deserialized.
         * @throws IllegalArgumentException if the JSON contains an invalid [BoundingBox].
         */
        @JvmStatic
        public fun fromJson(@Language("json") json: String): BoundingBox =
            GeoJson.jsonFormat.decodeFromString(json)

        /**
         * Deserialize a [BoundingBox] from a JSON string, returning null if the JSON is invalid.
         *
         * @param json A JSON string representing a bounding box.
         * @return The deserialized bounding box, or null if the JSON is invalid.
         */
        @JvmStatic
        public fun fromJsonOrNull(@Language("json") json: String): BoundingBox? =
            try {
                GeoJson.jsonFormat.decodeFromString(json)
            } catch (_: IllegalArgumentException) {
                null
            }
    }
}
