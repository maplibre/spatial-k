package org.maplibre.spatialk.geojson

import kotlin.jvm.JvmName
import kotlin.jvm.JvmStatic
import kotlin.math.min
import kotlinx.serialization.Serializable
import org.intellij.lang.annotations.Language
import org.maplibre.spatialk.geojson.serialization.BoundingBoxSerializer

/**
 * Represents an area bounded by a [northeast] and [southwest] [Position].
 *
 * A GeoJSON object MAY have a member named "bbox" to include information on the coordinate range
 * for its Geometries, Features, or FeatureCollections.
 *
 * When serialized, a BoundingBox is represented as an array of length 2*n where n is the number of
 * dimensions represented in the contained geometries, with all axes of the most southwesterly point
 * followed by all axes of the northeasterly point. The axes order of a BoundingBox follows the axes
 * order of geometries.
 *
 * For the BoundingBox to be serialized in 3D form, both Positions must have a defined altitude.
 *
 * @property northeast The northeastern corner of the BoundingBox
 * @property southwest The southwestern corner of the BoundingBox
 * @property coordinates The GeoJSON bounding box coordinate array
 * @see <a href="https://tools.ietf.org/html/rfc7946#section-5">
 *   https://tools.ietf.org/html/rfc7946#section-5</a>
 */
@Serializable(with = BoundingBoxSerializer::class)
public class BoundingBox internal constructor(internal val coordinates: DoubleArray) :
    GeoJsonElement, Iterable<Double> {
    init {
        require(coordinates.size >= 4 && coordinates.size % 2 == 0) {
            "Bounding Box coordinates must have at least 4 and an even number of values"
        }
    }

    public constructor(
        west: Double,
        south: Double,
        east: Double,
        north: Double,
    ) : this(doubleArrayOf(west, south, east, north))

    public constructor(
        west: Double,
        south: Double,
        minAltitude: Double,
        east: Double,
        north: Double,
        maxAltitude: Double,
    ) : this(doubleArrayOf(west, south, minAltitude, east, north, maxAltitude))

    /**
     * Construct a [BoundingBox] from two [Position]s that represent the southwest corner and
     * northeast corners.
     *
     * If one corner has more elements than the other, the extra elements are ignored.
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
     * `altitude`) per corner. Such additional axes are discouraged but allowed by the GeoJson
     * specification:
     * > Implementations SHOULD NOT extend positions beyond three elements because the semantics of
     * > extra elements are unspecified and ambiguous. Historically, some implementations have used
     * > a fourth element to carry a linear referencing measure (sometimes denoted as "M") or a
     * > numerical timestamp, but in most situations a parser will not be able to properly interpret
     * > these values.
     *
     * @param additionalElements must contain an even number of elements
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc7946#section-3.1.1">
     *   https://datatracker.ietf.org/doc/html/rfc7946#section-3.1.1</a>
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

    public val west: Double
        get() = coordinates[0]

    public val south: Double
        get() = coordinates[1]

    public val minAltitude: Double?
        get() = if (hasAltitude) coordinates[2] else null

    public val east: Double
        get() = if (hasAltitude) coordinates[3] else coordinates[2]

    public val north: Double
        get() = if (hasAltitude) coordinates[4] else coordinates[3]

    public val maxAltitude: Double?
        get() = if (hasAltitude) coordinates[5] else null

    /** @return the coordinate at the given index. */
    public operator fun get(index: Int): Double = coordinates[index]

    /** @return the coordinate at the given index or null if the index is out of range. */
    public fun getOrNull(index: Int): Double? = coordinates.getOrNull(index)

    /** @return the number of elements in the coordinates array. */
    public val size: Int
        get() = coordinates.size

    @get:JvmName("hasAltitude")
    public val hasAltitude: Boolean
        get() = coordinates.size >= 6

    override fun iterator(): Iterator<Double> = coordinates.iterator()

    /** @return [southwest] */
    public operator fun component1(): Position = southwest

    /** @return [northeast] */
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

    public override fun toJson(): String = GeoJson.jsonFormat.encodeToString(this)

    public companion object {
        @JvmStatic
        public fun fromJson(@Language("json") json: String): BoundingBox =
            GeoJson.jsonFormat.decodeFromString(json)

        @JvmStatic
        public fun fromJsonOrNull(@Language("json") json: String): BoundingBox? =
            try {
                GeoJson.jsonFormat.decodeFromString(json)
            } catch (_: IllegalArgumentException) {
                null
            }
    }
}
