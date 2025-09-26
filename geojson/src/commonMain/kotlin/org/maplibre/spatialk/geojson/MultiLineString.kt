package org.maplibre.spatialk.geojson

import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys
import org.maplibre.spatialk.geojson.serialization.GeoJson

/**
 * @throws IllegalArgumentException if any of the position lists is not a valid line string
 * @see <a href="https://tools.ietf.org/html/rfc7946#section-3.1.5">
 *   https://tools.ietf.org/html/rfc7946#section-3.1.5</a>
 * @see LineString
 */
@Serializable
@SerialName("MultiLineString")
@JsonIgnoreUnknownKeys
@Suppress("SERIALIZER_TYPE_INCOMPATIBLE")
public class MultiLineString
@JvmOverloads
constructor(public val coordinates: List<List<Position>>, override val bbox: BoundingBox? = null) :
    Geometry() {

    /**
     * Create a MultiLineString by a number of lists of [Position]s.
     *
     * @throws IllegalArgumentException if any of the position lists is not a valid line string
     */
    @JvmOverloads
    public constructor(
        vararg coordinates: List<Position>,
        bbox: BoundingBox? = null,
    ) : this(coordinates.toList(), bbox)

    /** Create a MultiLineString by a number of [LineString]s. */
    @JvmOverloads
    public constructor(
        vararg lineStrings: LineString,
        bbox: BoundingBox? = null,
    ) : this(lineStrings.map { it.coordinates }, bbox)

    /**
     * Create a MultiLineString by an array (= line strings) of arrays (= positions) where each
     * position is represented by a [DoubleArray].
     *
     * @throws IllegalArgumentException if any of the position lists is not a valid line string or
     *   any of the arrays of doubles does not represent a valid position.
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as MultiLineString

        if (coordinates != other.coordinates) return false
        if (bbox != other.bbox) return false

        return true
    }

    override fun hashCode(): Int {
        var result = coordinates.hashCode()
        result = 31 * result + (bbox?.hashCode() ?: 0)
        return result
    }

    override fun json(): String = GeoJson.encodeToString(this)

    public companion object {
        @JvmStatic
        public fun fromJson(json: String): MultiLineString = fromJson<MultiLineString>(json)

        @JvmStatic
        public fun fromJsonOrNull(json: String): MultiLineString? =
            try {
                fromJson(json)
            } catch (_: IllegalArgumentException) {
                null
            }
    }
}
