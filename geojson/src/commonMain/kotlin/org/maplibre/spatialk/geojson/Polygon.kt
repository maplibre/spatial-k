package org.maplibre.spatialk.geojson

import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.intellij.lang.annotations.Language
import org.maplibre.spatialk.geojson.serialization.GeoJson

/**
 * To specify a constraint specific to [Polygon]s, it is useful to introduce the concept of a linear
 * ring:
 * - A linear ring is a closed LineString with four or more positions.
 * - The first and last positions are equivalent, and they MUST contain identical values; their
 *   representation SHOULD also be identical.
 * - A linear ring is the boundary of a surface or the boundary of a hole in a surface.
 * - A linear ring MUST follow the right-hand rule with respect to the area it bounds, i.e.,
 *   exterior rings are counterclockwise, and holes are clockwise.
 *
 * @throws IllegalArgumentException if the coordinates are empty or any of the position lists
 *   representing a line string is either not closed or contains fewer than 4 positions.
 * @see <a href="https://tools.ietf.org/html/rfc7946#section-3.1.6">
 *   https://tools.ietf.org/html/rfc7946#section-3.1.6</a>
 * @see [MultiPolygon]
 */
@Serializable
@SerialName("Polygon")
public data class Polygon
@JvmOverloads
constructor(
    /**
     * A list (= polygon rings) of lists of [Position]s that represent this polygon. The first ring
     * represents the exterior ring while any others are interior rings (= holes).
     */
    public val coordinates: List<List<Position>>,
    /** a bounding box */
    override val bbox: BoundingBox? = null,
) : Geometry() {
    @JvmOverloads
    public constructor(
        vararg coordinates: List<Position>,
        bbox: BoundingBox? = null,
    ) : this(coordinates.toList(), bbox)

    /**
     * Create a Polygon by a number of closed [LineString]s.
     *
     * @throws IllegalArgumentException if no coordinates have been specified or any of the
     *   [LineString]s is either not closed or contains fewer than 4 positions.
     */
    @JvmOverloads
    public constructor(
        vararg lineStrings: LineString,
        bbox: BoundingBox? = null,
    ) : this(lineStrings.map { it.coordinates }, bbox)

    /**
     * Create a Polygon by arrays (= polygon rings) of arrays (= positions) where each position is
     * represented by a [DoubleArray].
     *
     * @throws IllegalArgumentException if the outer array is empty, or if any of the inner arrays
     *   does not represent a valid closed line string, or if any of the arrays of doubles does not
     *   represent a valid position.
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

    override fun json(): String = GeoJson.encodeToString(this)

    public companion object {
        @JvmStatic
        public fun fromJson(@Language("json") json: String): Polygon = fromJson<Polygon>(json)

        @JvmStatic
        public fun fromJsonOrNull(@Language("json") json: String): Polygon? =
            try {
                fromJson(json)
            } catch (_: IllegalArgumentException) {
                null
            }
    }
}
