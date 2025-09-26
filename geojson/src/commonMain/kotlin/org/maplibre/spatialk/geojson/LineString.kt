package org.maplibre.spatialk.geojson

import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import org.maplibre.spatialk.geojson.serialization.GeometrySerializer
import org.maplibre.spatialk.geojson.serialization.jsonJoin
import org.maplibre.spatialk.geojson.serialization.jsonProp
import org.maplibre.spatialk.geojson.serialization.toBbox
import org.maplibre.spatialk.geojson.serialization.toPosition

/**
 * @throws IllegalArgumentException if the coordinates contain fewer than two positions
 * @see <a href="https://tools.ietf.org/html/rfc7946#section-3.1.4">
 *   https://tools.ietf.org/html/rfc7946#section-3.1.4</a>
 * @see MultiLineString
 */
@Suppress("SERIALIZER_TYPE_INCOMPATIBLE")
@Serializable(with = GeometrySerializer::class)
public class LineString
@JvmOverloads
constructor(
    /** a list of [Position]s. */
    public val coordinates: List<Position>,
    /** a bounding box */
    override val bbox: BoundingBox? = null,
) : Geometry() {

    /**
     * Create a LineString by a number of [Position]s.
     *
     * @throws IllegalArgumentException if fewer than two coordinates have been specified
     */
    @JvmOverloads
    public constructor(
        vararg coordinates: Position,
        bbox: BoundingBox? = null,
    ) : this(coordinates.toList(), bbox)

    /**
     * Create a LineString by the positions of a number of [Point]s.
     *
     * @throws IllegalArgumentException if fewer than two points have been specified
     */
    @JvmOverloads
    public constructor(
        vararg points: Point,
        bbox: BoundingBox? = null,
    ) : this(points.map { it.coordinates }, bbox)

    /**
     * Create a LineString by an array of [DoubleArray]s that each represent a position.
     *
     * @throws IllegalArgumentException if the coordinates contain fewer than two positions or any
     *   array of doubles does not represent a valid position
     */
    @JvmOverloads
    public constructor(
        coordinates: Array<DoubleArray>,
        bbox: BoundingBox? = null,
    ) : this(coordinates.map(::Position), bbox)

    init {
        require(coordinates.size >= 2) { "LineString must contain at least two positions" }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as LineString

        if (coordinates != other.coordinates) return false
        if (bbox != other.bbox) return false

        return true
    }

    override fun hashCode(): Int {
        var result = coordinates.hashCode()
        result = 31 * result + (bbox?.hashCode() ?: 0)
        return result
    }

    override fun json(): String =
        """{"type":"LineString",${bbox.jsonProp()}"coordinates":${coordinates.jsonJoin(transform = Position::json)}}"""

    public companion object {
        @JvmStatic
        public fun fromJson(json: String): LineString =
            fromJson(Json.decodeFromString(JsonObject.serializer(), json))

        @JvmStatic
        public fun fromJsonOrNull(json: String): LineString? =
            try {
                fromJson(json)
            } catch (_: Exception) {
                null
            }

        @JvmStatic
        public fun fromJson(json: JsonObject): LineString {
            try {
                require(json.getValue("type").jsonPrimitive.content == "LineString") {
                    "Object \"type\" is not \"LineString\"."
                }

                val coords =
                    json.getValue("coordinates").jsonArray.map { it.jsonArray.toPosition() }
                val bbox = json["bbox"]?.jsonArray?.toBbox()

                return LineString(coords, bbox)
            } catch (e: IllegalArgumentException) {
                throw SerializationException(e.message)
            }
        }
    }
}
