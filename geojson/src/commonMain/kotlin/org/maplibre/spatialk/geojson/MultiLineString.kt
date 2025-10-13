package org.maplibre.spatialk.geojson

import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import org.intellij.lang.annotations.Language
import org.maplibre.spatialk.geojson.serialization.MultiLineStringSerializer

/**
 * A [MultiLineString] geometry represents multiple curves in coordinate space.
 *
 * See [RFC 7946 Section 3.1.5](https://tools.ietf.org/html/rfc7946#section-3.1.5) for the full
 * specification.
 *
 * @throws IllegalArgumentException if any of the [Position] lists is not a valid [LineString]
 * @see LineString
 */
@Serializable(with = MultiLineStringSerializer::class)
public data class MultiLineString
@JvmOverloads
constructor(
    /** The coordinates of this geometry. */
    public val coordinates: List<List<Position>>,
    /** The bounding box of this geometry. */
    override val bbox: BoundingBox? = null,
) : MultiGeometry, LineStringGeometry, Collection<LineString> {

    /**
     * Create a [MultiLineString] by a number of lists of [Position] objects.
     *
     * @param coordinates The lists of [Position] objects that make up the [LineString] objects.
     * @param bbox The [BoundingBox] of this geometry.
     * @throws IllegalArgumentException if any of the [Position] lists is not a valid [LineString]
     */
    @JvmOverloads
    public constructor(
        vararg coordinates: List<Position>,
        bbox: BoundingBox? = null,
    ) : this(coordinates.toList(), bbox)

    /**
     * Create a [MultiLineString] by a number of [LineString] objects.
     *
     * @param lineStrings The [LineString] objects that make up this multi-line string.
     * @param bbox The [BoundingBox] of this geometry.
     * @throws IllegalArgumentException if any of the [LineString] objects contains fewer than 2
     *   [Position] objects.
     */
    @JvmOverloads
    public constructor(
        vararg lineStrings: LineString,
        bbox: BoundingBox? = null,
    ) : this(lineStrings.map { it.coordinates }, bbox)

    /**
     * Create a [MultiLineString] by an array (= [LineString] objects) of arrays (= [Position]
     * objects) where each [Position] is represented by a [DoubleArray].
     *
     * @param coordinates The array of arrays of double arrays representing [LineString] objects.
     * @param bbox The [BoundingBox] of this geometry.
     * @throws IllegalArgumentException if any of the [Position] lists is not a valid [LineString]
     *   or any of the arrays of doubles does not represent a valid [Position].
     */
    @JvmOverloads
    public constructor(
        coordinates: Array<Array<DoubleArray>>,
        bbox: BoundingBox? = null,
    ) : this(coordinates.map { it.map(::Position) }, bbox)

    init {
        coordinates.forEachIndexed { index, line ->
            require(line.size >= 2) {
                "LineString at index $index contains fewer than 2 positions."
            }
        }
    }

    override val size: Int
        get() = coordinates.size

    override fun isEmpty(): Boolean = coordinates.isEmpty()

    override fun contains(element: LineString): Boolean = coordinates.contains(element.coordinates)

    override fun iterator(): Iterator<LineString> =
        coordinates.asSequence().map { LineString(it) }.iterator()

    override fun containsAll(elements: Collection<LineString>): Boolean =
        coordinates.containsAll(elements.map { it.coordinates })

    /**
     * Get the line string at the specified index.
     *
     * @param index The index of the line string to retrieve.
     * @return The line string at the specified index.
     */
    public operator fun get(index: Int): LineString = LineString(coordinates[index])

    /** Factory methods for creating and serializing [MultiLineString] objects. */
    public companion object {
        /**
         * Deserialize a [MultiLineString] from a JSON string.
         *
         * @param json The JSON string to parse.
         * @return The deserialized [MultiLineString].
         * @throws SerializationException if the JSON string is invalid or cannot be deserialized.
         * @throws IllegalArgumentException if the geometry does not meet structural requirements.
         */
        @JvmStatic
        public fun fromJson(@Language("json") json: String): MultiLineString =
            GeoJson.decodeFromString(json)

        /**
         * Deserialize a [MultiLineString] from a JSON string, or null if parsing fails.
         *
         * @param json The JSON string to parse.
         * @return The deserialized [MultiLineString], or null if parsing fails.
         */
        @JvmStatic
        public fun fromJsonOrNull(@Language("json") json: String): MultiLineString? =
            GeoJson.decodeFromStringOrNull(json)

        @PublishedApi
        @JvmStatic
        internal fun toJson(multiLineString: MultiLineString): String = multiLineString.toJson()
    }
}
