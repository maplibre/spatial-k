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
 * @see <a href="https://tools.ietf.org/html/rfc7946#section-3.1.7">
 *   https://tools.ietf.org/html/rfc7946#section-3.1.7</a>
 * @see Polygon
 */
@Suppress("SERIALIZER_TYPE_INCOMPATIBLE")
@Serializable(with = GeometrySerializer::class)
/** @throws IllegalArgumentException if any of the lists does not represent a valid polygon */
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

    override fun json(): String =
        """{"type":"MultiPolygon",${bbox.jsonProp()}"coordinates":${
            coordinates.jsonJoin { polygon ->
                polygon.jsonJoin {
                    it.jsonJoin(transform = Position::json)
                }
            }
        }}"""

    public companion object {
        @JvmStatic
        public fun fromJson(json: String): MultiPolygon =
            fromJson(Json.decodeFromString(JsonObject.serializer(), json))

        @JvmStatic
        public fun fromJsonOrNull(json: String): MultiPolygon? =
            try {
                fromJson(json)
            } catch (_: Exception) {
                null
            }

        @JvmStatic
        public fun fromJson(json: JsonObject): MultiPolygon {
            try {
                require(json.getValue("type").jsonPrimitive.content == "MultiPolygon") {
                    "Object \"type\" is not \"MultiPolygon\"."
                }

                val coords =
                    json.getValue("coordinates").jsonArray.map { polygon ->
                        polygon.jsonArray.map { ring ->
                            ring.jsonArray.map { it.jsonArray.toPosition() }
                        }
                    }
                val bbox = json["bbox"]?.jsonArray?.toBbox()

                return MultiPolygon(coords, bbox)
            } catch (e: IllegalArgumentException) {
                throw SerializationException(e.message)
            }
        }
    }
}
