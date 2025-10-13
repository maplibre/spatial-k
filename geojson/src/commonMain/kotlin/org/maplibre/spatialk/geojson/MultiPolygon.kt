package org.maplibre.spatialk.geojson

import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import org.intellij.lang.annotations.Language
import org.maplibre.spatialk.geojson.serialization.MultiPolygonSerializer

/**
 * A [MultiPolygon] geometry represents multiple surfaces in coordinate space.
 *
 * See [RFC 7946 Section 3.1.7](https://tools.ietf.org/html/rfc7946#section-3.1.7) for the full
 * specification.
 *
 * @throws IllegalArgumentException if any of the lists does not represent a valid [Polygon]
 * @see Polygon
 */
@Serializable(with = MultiPolygonSerializer::class)
public data class MultiPolygon
@JvmOverloads
constructor(
    /** The coordinates of this geometry. */
    public val coordinates: List<List<List<Position>>>,
    /** The bounding box of this geometry. */
    override val bbox: BoundingBox? = null,
) : MultiGeometry, PolygonGeometry, Collection<Polygon> {

    /**
     * Create a [MultiPolygon] by a number of lists (= polygon rings) of lists (= [Position]
     * objects).
     *
     * @param coordinates The lists of polygon rings that make up the [Polygon] objects.
     * @param bbox The [BoundingBox] of this geometry.
     * @throws IllegalArgumentException if any list does not represent a valid [Polygon]
     */
    @JvmOverloads
    public constructor(
        vararg coordinates: List<List<Position>>,
        bbox: BoundingBox? = null,
    ) : this(coordinates.toList(), bbox)

    /**
     * Create a [MultiPolygon] by a number of [Polygon] objects.
     *
     * @param polygons The [Polygon] objects that make up this multi-polygon.
     * @param bbox The [BoundingBox] of this geometry.
     * @throws IllegalArgumentException if any of the [Polygon] objects does not represent a valid
     *   polygon (e.g., empty or rings not closed or fewer than 4 positions per ring).
     */
    @JvmOverloads
    public constructor(
        vararg polygons: Polygon,
        bbox: BoundingBox? = null,
    ) : this(polygons.map { it.coordinates }, bbox)

    /**
     * Create a [MultiPolygon] by an array (= [Polygon] objects) of arrays (= polygon rings) of
     * arrays (= [Position] objects) where each [Position] is represented by a [DoubleArray].
     *
     * @param coordinates The array of arrays of arrays of double arrays representing [Polygon]
     *   objects.
     * @param bbox The [BoundingBox] of this geometry.
     * @throws IllegalArgumentException if the array does not represent a valid [MultiPolygon]
     */
    @JvmOverloads
    public constructor(
        coordinates: Array<Array<Array<DoubleArray>>>,
        bbox: BoundingBox? = null,
    ) : this(coordinates.map { ring -> ring.map { it.map(::Position) } }, bbox)

    init {
        coordinates.forEachIndexed { polygonIndex, polygon ->
            require(polygon.isNotEmpty()) { "Polygon at index $polygonIndex must not be empty." }

            polygon.forEachIndexed { ringIndex, ring ->
                require(ring.size >= 4) {
                    "Line string at index $ringIndex of polygon at index $polygonIndex contains " +
                        "fewer than 4 positions."
                }
                require(ring.first() == ring.last()) {
                    "Line string at at index $ringIndex of polygon at index $polygonIndex is " +
                        "not closed."
                }
            }
        }
    }

    override val size: Int
        get() = coordinates.size

    override fun isEmpty(): Boolean = coordinates.isEmpty()

    override fun contains(element: Polygon): Boolean = coordinates.contains(element.coordinates)

    override fun iterator(): Iterator<Polygon> =
        coordinates.asSequence().map { Polygon(it) }.iterator()

    override fun containsAll(elements: Collection<Polygon>): Boolean =
        coordinates.containsAll(elements.map { it.coordinates })

    /**
     * Get the polygon at the specified index.
     *
     * @param index The index of the polygon to retrieve.
     * @return The polygon at the specified index.
     */
    public operator fun get(index: Int): Polygon = Polygon(coordinates[index])

    /** Factory methods for creating and serializing [MultiPolygon] objects. */
    public companion object {
        /**
         * Deserialize a [MultiPolygon] from a JSON string.
         *
         * @param json The JSON string to parse.
         * @return The deserialized [MultiPolygon].
         * @throws SerializationException if the JSON string is invalid or cannot be deserialized.
         * @throws IllegalArgumentException if the geometry does not meet structural requirements.
         */
        @JvmStatic
        public fun fromJson(@Language("json") json: String): MultiPolygon =
            GeoJson.decodeFromString(json)

        /**
         * Deserialize a [MultiPolygon] from a JSON string, or null if parsing fails.
         *
         * @param json The JSON string to parse.
         * @return The deserialized [MultiPolygon], or null if parsing fails.
         */
        @JvmStatic
        public fun fromJsonOrNull(@Language("json") json: String): MultiPolygon? =
            GeoJson.decodeFromStringOrNull(json)

        @PublishedApi
        @JvmStatic
        internal fun toJson(multiPolygon: MultiPolygon): String = multiPolygon.toJson()
    }
}
