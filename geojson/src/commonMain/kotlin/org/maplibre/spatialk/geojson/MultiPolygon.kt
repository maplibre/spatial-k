package org.maplibre.spatialk.geojson

import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic
import kotlinx.serialization.Serializable
import org.intellij.lang.annotations.Language
import org.maplibre.spatialk.geojson.serialization.MultiPolygonSerializer

/**
 * @throws IllegalArgumentException if any of the lists does not represent a valid polygon
 * @see <a href="https://tools.ietf.org/html/rfc7946#section-3.1.7">
 *   https://tools.ietf.org/html/rfc7946#section-3.1.7</a>
 * @see Polygon
 */
@Serializable(with = MultiPolygonSerializer::class)
public data class MultiPolygon
@JvmOverloads
constructor(
    /** a list (= polygons) of lists (= polygon rings) of lists of [Position]s. */
    public val coordinates: List<List<List<Position>>>,
    /** a bounding box */
    override val bbox: BoundingBox? = null,
) : MultiGeometry, PolygonGeometry, Collection<Polygon> {

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

    public override fun toJson(): String = GeoJson.encodeToString(this)

    override val size: Int
        get() = coordinates.size

    override fun isEmpty(): Boolean = coordinates.isEmpty()

    override fun contains(element: Polygon): Boolean = coordinates.contains(element.coordinates)

    override fun iterator(): Iterator<Polygon> =
        coordinates.asSequence().map { Polygon(it) }.iterator()

    override fun containsAll(elements: Collection<Polygon>): Boolean =
        coordinates.containsAll(elements.map { it.coordinates })

    public operator fun get(index: Int): Polygon = Polygon(coordinates[index])

    public companion object {
        @JvmStatic
        public fun fromJson(@Language("json") json: String): MultiPolygon =
            GeoJson.decodeFromString(json)

        @JvmStatic
        public fun fromJsonOrNull(@Language("json") json: String): MultiPolygon? =
            GeoJson.decodeFromStringOrNull(json)
    }
}
