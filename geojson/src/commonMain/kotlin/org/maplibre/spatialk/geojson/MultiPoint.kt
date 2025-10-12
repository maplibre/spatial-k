package org.maplibre.spatialk.geojson

import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic
import kotlinx.serialization.Serializable
import org.intellij.lang.annotations.Language
import org.maplibre.spatialk.geojson.serialization.MultiPointSerializer

/**
 * @see <a href="https://tools.ietf.org/html/rfc7946#section-3.1.3">
 *   https://tools.ietf.org/html/rfc7946#section-3.1.3</a>
 * @see Point
 */
@Serializable(with = MultiPointSerializer::class)
public data class MultiPoint
@JvmOverloads
constructor(
    /** a list of [Position]s. */
    public val coordinates: List<Position>,
    /** a bounding box */
    override val bbox: BoundingBox? = null,
) : MultiGeometry, PointGeometry, Collection<Point> {

    /** Create a MultiPoint by a number of [Position]s. */
    @JvmOverloads
    public constructor(
        vararg coordinates: Position,
        bbox: BoundingBox? = null,
    ) : this(coordinates.toList(), bbox)

    @JvmOverloads
    public constructor(
        vararg points: Point,
        bbox: BoundingBox? = null,
    ) : this(points.map { it.coordinates }, bbox)

    /**
     * Create a MultiPoint by an array of [DoubleArray]s that each represent a position.
     *
     * @throws IllegalArgumentException if any array of doubles does not represent a valid position
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

    public operator fun get(index: Int): Point = Point(coordinates[index])

    public companion object {
        @JvmStatic
        public fun fromJson(@Language("json") json: String): MultiPoint =
            GeoJson.decodeFromString(json)

        @JvmStatic
        public fun fromJsonOrNull(@Language("json") json: String): MultiPoint? =
            GeoJson.decodeFromStringOrNull(json)

        @PublishedApi
        @JvmStatic
        internal fun toJson(multiPoint: MultiPoint): String = multiPoint.toJson()
    }
}
