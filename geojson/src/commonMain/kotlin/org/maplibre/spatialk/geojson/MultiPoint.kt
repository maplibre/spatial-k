package org.maplibre.spatialk.geojson

import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import org.intellij.lang.annotations.Language
import org.maplibre.spatialk.geojson.serialization.MultiPointSerializer

/**
 * A [MultiPoint] geometry represents multiple points in coordinate space.
 *
 * See [RFC 7946 Section 3.1.3](https://tools.ietf.org/html/rfc7946#section-3.1.3) for the full
 * specification.
 *
 * @see Point
 */
@Serializable(with = MultiPointSerializer::class)
public data class MultiPoint
@JvmOverloads
constructor(
    /** The coordinates of this geometry. */
    public val coordinates: List<Position>,
    /** The bounding box of this geometry. */
    override val bbox: BoundingBox? = null,
) : MultiGeometry, PointGeometry, Collection<Point> {

    /**
     * Create a [MultiPoint] by a number of [Position] objects.
     *
     * @param coordinates The [Position] objects that make up this multi-point.
     * @param bbox The [BoundingBox] of this geometry.
     */
    @JvmOverloads
    public constructor(
        vararg coordinates: Position,
        bbox: BoundingBox? = null,
    ) : this(coordinates.toList(), bbox)

    /**
     * Create a [MultiPoint] by a number of [Point] objects.
     *
     * @param points The [Point] objects that make up this multi-point.
     * @param bbox The [BoundingBox] of this geometry.
     */
    @JvmOverloads
    public constructor(
        vararg points: Point,
        bbox: BoundingBox? = null,
    ) : this(points.map { it.coordinates }, bbox)

    /**
     * Create a [MultiPoint] by an array of [DoubleArray] objects that each represent a [Position].
     *
     * @param coordinates The array of double arrays representing [Position] objects.
     * @param bbox The [BoundingBox] of this geometry.
     * @throws IllegalArgumentException if any array of doubles does not represent a valid
     *   [Position]
     */
    @JvmOverloads
    public constructor(
        coordinates: Array<DoubleArray>,
        bbox: BoundingBox? = null,
    ) : this(coordinates.map(::Position), bbox)

    override val size: Int
        get() = coordinates.size

    override fun isEmpty(): Boolean = coordinates.isEmpty()

    override fun contains(element: Point): Boolean = coordinates.contains(element.coordinates)

    override fun iterator(): Iterator<Point> = coordinates.asSequence().map { Point(it) }.iterator()

    override fun containsAll(elements: Collection<Point>): Boolean =
        coordinates.containsAll(elements.map { it.coordinates })

    /**
     * Get the point at the specified index.
     *
     * @param index The index of the point to retrieve.
     * @return The point at the specified index.
     */
    public operator fun get(index: Int): Point = Point(coordinates[index])

    /** Factory methods for creating and serializing [MultiPoint] objects. */
    public companion object {
        /**
         * Deserialize a [MultiPoint] from a JSON string.
         *
         * @param json The JSON string to parse.
         * @return The deserialized [MultiPoint].
         * @throws SerializationException if the JSON string is invalid or cannot be deserialized.
         * @throws IllegalArgumentException if the geometry does not meet structural requirements.
         */
        @JvmStatic
        public fun fromJson(@Language("json") json: String): MultiPoint =
            GeoJson.decodeFromString(json)

        /**
         * Deserialize a [MultiPoint] from a JSON string, or null if parsing fails.
         *
         * @param json The JSON string to parse.
         * @return The deserialized [MultiPoint], or null if parsing fails.
         */
        @JvmStatic
        public fun fromJsonOrNull(@Language("json") json: String): MultiPoint? =
            GeoJson.decodeFromStringOrNull(json)

        @PublishedApi
        @JvmStatic
        internal fun toJson(multiPoint: MultiPoint): String = multiPoint.toJson()
    }
}
