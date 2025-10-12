package org.maplibre.spatialk.geojson

import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic
import kotlinx.serialization.Serializable
import org.intellij.lang.annotations.Language
import org.maplibre.spatialk.geojson.serialization.MultiLineStringSerializer

/**
 * @throws IllegalArgumentException if any of the position lists is not a valid line string
 * @see <a href="https://tools.ietf.org/html/rfc7946#section-3.1.5">
 *   https://tools.ietf.org/html/rfc7946#section-3.1.5</a>
 * @see LineString
 */
@Serializable(with = MultiLineStringSerializer::class)
public data class MultiLineString
@JvmOverloads
constructor(public val coordinates: List<List<Position>>, override val bbox: BoundingBox? = null) :
    MultiGeometry, LineStringGeometry, Collection<LineString> {

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

    override val size: Int
        get() = coordinates.size

    override fun isEmpty(): Boolean = coordinates.isEmpty()

    override fun contains(element: LineString): Boolean = coordinates.contains(element.coordinates)

    override fun iterator(): Iterator<LineString> =
        coordinates.asSequence().map { LineString(it) }.iterator()

    override fun containsAll(elements: Collection<LineString>): Boolean =
        coordinates.containsAll(elements.map { it.coordinates })

    public operator fun get(index: Int): LineString = LineString(coordinates[index])

    public companion object {
        @JvmStatic
        public fun fromJson(@Language("json") json: String): MultiLineString =
            GeoJson.decodeFromString(json)

        @JvmStatic
        public fun fromJsonOrNull(@Language("json") json: String): MultiLineString? =
            GeoJson.decodeFromStringOrNull(json)

        @PublishedApi
        @JvmStatic
        internal fun toJson(multiLineString: MultiLineString): String = multiLineString.toJson()
    }
}
