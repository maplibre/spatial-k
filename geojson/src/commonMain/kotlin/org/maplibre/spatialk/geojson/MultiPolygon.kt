package org.maplibre.spatialk.geojson

import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys
import org.intellij.lang.annotations.Language
import org.maplibre.spatialk.geojson.serialization.GeoJson

/**
 * @throws IllegalArgumentException if any of the lists does not represent a valid polygon
 * @see <a href="https://tools.ietf.org/html/rfc7946#section-3.1.7">
 *   https://tools.ietf.org/html/rfc7946#section-3.1.7</a>
 * @see Polygon
 */
@Serializable
@SerialName("MultiPolygon")
@JsonIgnoreUnknownKeys
public class MultiPolygon
@JvmOverloads
constructor(
    /** a list (= polygons) of lists (= polygon rings) of lists of [Position]s. */
    public val coordinates: List<List<List<Position>>>,
    /** a bounding box */
    override val bbox: BoundingBox? = null,
) : Geometry() {

    /**
     * Create a MultiPolygon by a number of lists (= polygon rings) of lists (= positions).
     *
     * @throws IllegalArgumentException if any list does not represent a valid polygon
     */
    @JvmOverloads
    public constructor(
        vararg coordinates: List<List<Position>>,
        bbox: BoundingBox? = null,
    ) : this(coordinates.toList(), bbox)

    /** Create a MultiPolygon by a number of [Polygon]s. */
    @JvmOverloads
    public constructor(
        vararg polygons: Polygon,
        bbox: BoundingBox? = null,
    ) : this(polygons.map { it.coordinates }, bbox)

    /**
     * Create a MultiPolygon by an array (= polygons) of arrays (= polygon rings) of arrays (=
     * positions) where each position is represented by a [DoubleArray].
     *
     * @throws IllegalArgumentException if the array does not represent a valid multi polygon
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as MultiPolygon

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
        public fun fromJson(@Language("json") json: String): MultiPolygon =
            fromJson<MultiPolygon>(json)

        @JvmStatic
        public fun fromJsonOrNull(@Language("json") json: String): MultiPolygon? =
            try {
                fromJson(json)
            } catch (_: IllegalArgumentException) {
                null
            }
    }
}
