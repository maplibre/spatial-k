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

@Suppress("SERIALIZER_TYPE_INCOMPATIBLE")
@Serializable(with = GeometrySerializer::class)
public class MultiPolygon
@JvmOverloads
constructor(
    public val coordinates: List<List<List<Position>>>,
    override val bbox: BoundingBox? = null,
) : Geometry() {
    @JvmOverloads
    public constructor(
        vararg coordinates: List<List<Position>>,
        bbox: BoundingBox? = null,
    ) : this(coordinates.toList(), bbox)

    @JvmOverloads
    public constructor(
        vararg polygons: Polygon,
        bbox: BoundingBox? = null,
    ) : this(polygons.map { it.coordinates }, bbox)

    @JvmOverloads
    public constructor(
        coordinates: Array<Array<Array<DoubleArray>>>,
        bbox: BoundingBox? = null,
    ) : this(coordinates.map { ring -> ring.map { it.map(::Position) } }, bbox)

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
