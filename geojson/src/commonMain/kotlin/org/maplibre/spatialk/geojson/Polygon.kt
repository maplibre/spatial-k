package org.maplibre.spatialk.geojson

import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import org.intellij.lang.annotations.Language
import org.maplibre.spatialk.geojson.serialization.PolygonSerializer

/**
 * A [Polygon] geometry represents a surface in coordinate space bounded by linear rings.
 *
 * To specify a constraint specific to [Polygon] objects, it is useful to introduce the concept of a
 * linear ring:
 * - A linear ring is a closed [LineString] with four or more [Position] objects.
 * - The first and last [Position] objects are equivalent, and they MUST contain identical values;
 *   their representation SHOULD also be identical.
 * - A linear ring is the boundary of a surface or the boundary of a hole in a surface.
 * - A linear ring MUST follow the right-hand rule with respect to the area it bounds, i.e.,
 *   exterior rings are counterclockwise, and holes are clockwise.
 *
 * See [RFC 7946 Section 3.1.6](https://tools.ietf.org/html/rfc7946#section-3.1.6) for the full
 * specification.
 *
 * @throws IllegalArgumentException if the coordinates are empty or any of the [Position] lists
 *   representing a [LineString] is either not closed or contains fewer than 4 [Position] objects.
 * @see MultiPolygon
 */
@Serializable(with = PolygonSerializer::class)
public data class Polygon
@JvmOverloads
constructor(
    /**
     * The coordinates of this [Polygon]. A list (= polygon rings) of lists of [Position] objects
     * that represent this [Polygon]. The first ring represents the exterior ring while any others
     * are interior rings (= holes).
     */
    public val coordinates: List<List<Position>>,
    /** The [BoundingBox] of this [Polygon]. */
    override val bbox: BoundingBox? = null,
) : SingleGeometry, PolygonGeometry {
    /**
     * Create a [Polygon] by a number of linear rings.
     *
     * @param coordinates The linear rings that make up this [Polygon].
     * @param bbox The [BoundingBox] of this [Polygon].
     * @throws IllegalArgumentException if no coordinates have been specified or any of the
     *   [Position] lists is either not closed or contains fewer than 4 [Position] objects.
     */
    @JvmOverloads
    public constructor(
        vararg coordinates: List<Position>,
        bbox: BoundingBox? = null,
    ) : this(coordinates.toList(), bbox)

    /**
     * Create a [Polygon] by a number of closed [LineString] objects.
     *
     * @param lineStrings The [LineString] objects representing the polygon rings.
     * @param bbox The [BoundingBox] of this [Polygon].
     * @throws IllegalArgumentException if no coordinates have been specified or any of the
     *   [LineString] objects is either not closed or contains fewer than 4 [Position] objects.
     */
    @JvmOverloads
    public constructor(
        vararg lineStrings: LineString,
        bbox: BoundingBox? = null,
    ) : this(lineStrings.map { it.coordinates }, bbox)

    /**
     * Create a [Polygon] by arrays (= polygon rings) of arrays (= [Position] objects) where each
     * [Position] is represented by a [DoubleArray].
     *
     * @param coordinates The array of arrays of double arrays representing polygon rings.
     * @param bbox The [BoundingBox] of this [Polygon].
     * @throws IllegalArgumentException if the outer array is empty, or if any of the inner arrays
     *   does not represent a valid closed [LineString], or if any of the arrays of doubles does not
     *   represent a valid [Position].
     */
    @JvmOverloads
    public constructor(
        coordinates: Array<Array<DoubleArray>>,
        bbox: BoundingBox? = null,
    ) : this(coordinates.map { it.map(::Position) }, bbox)

    init {
        require(coordinates.isNotEmpty()) { "A Polygon must not be empty." }

        coordinates.forEachIndexed { i, ring ->
            require(ring.size >= 4) { "Line string at index $i contains fewer than 4 positions." }
            require(ring.first() == ring.last()) { "Line string at at index $i is not closed." }
        }
    }

    /** Factory methods for creating and serializing [Polygon] objects. */
    public companion object {
        /**
         * Deserialize a [Polygon] from a JSON string.
         *
         * @param json The JSON string to parse.
         * @return The deserialized [Polygon].
         * @throws SerializationException if the JSON string is invalid or cannot be deserialized.
         * @throws IllegalArgumentException if the geometry does not meet structural requirements.
         */
        @JvmStatic
        public fun fromJson(@Language("json") json: String): Polygon =
            GeoJson.decodeFromString(json)

        /**
         * Deserialize a [Polygon] from a JSON string, or null if parsing fails.
         *
         * @param json The JSON string to parse.
         * @return The deserialized [Polygon], or null if parsing fails.
         */
        @JvmStatic
        public fun fromJsonOrNull(@Language("json") json: String): Polygon? =
            GeoJson.decodeFromStringOrNull(json)

        @PublishedApi @JvmStatic internal fun toJson(polygon: Polygon): String = polygon.toJson()
    }
}
