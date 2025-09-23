package org.maplibre.spatialk.geojson

import org.maplibre.spatialk.geojson.serialization.GeometrySerializer
import org.maplibre.spatialk.geojson.serialization.jsonJoin
import org.maplibre.spatialk.geojson.serialization.jsonProp
import org.maplibre.spatialk.geojson.serialization.toBbox
import org.maplibre.spatialk.geojson.serialization.toPosition
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic

@Suppress("SERIALIZER_TYPE_INCOMPATIBLE")
@Serializable(with = GeometrySerializer::class)
public class MultiLineString @JvmOverloads constructor(
    public val coordinates: List<List<Position>>,
    override val bbox: BoundingBox? = null
) : Geometry() {
    @JvmOverloads
    public constructor(vararg coordinates: List<Position>, bbox: BoundingBox? = null) : this(coordinates.toList(), bbox)

    @JvmOverloads
    public constructor(
        coordinates: Array<Array<DoubleArray>>,
        bbox: BoundingBox? = null
    ) : this(coordinates.map { it.map(::Position) }, bbox)

    init {
        coordinates.forEach { line ->
            require(line.size >= 2) { "LineString must have at least two positions" }
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

    override fun json(): String =
        """{"type":"MultiLineString",${bbox.jsonProp()}"coordinates":${
            coordinates.jsonJoin {
                it.jsonJoin(transform = Position::json)
            }
        }}"""

    public companion object {
        @JvmStatic
        public fun fromJson(json: String): MultiLineString =
            fromJson(Json.decodeFromString(JsonObject.serializer(), json))

        @JvmStatic
        public fun fromJsonOrNull(json: String): MultiLineString? = try {
            fromJson(json)
        } catch (_: Exception) {
            null
        }

        @JvmStatic
        public fun fromJson(json: JsonObject): MultiLineString {
            require(json.getValue("type").jsonPrimitive.content == "MultiLineString") {
                "Object \"type\" is not \"MultiLineString\"."
            }

            val coords =
                json.getValue("coordinates").jsonArray.map { line -> line.jsonArray.map { it.jsonArray.toPosition() } }
            val bbox = json["bbox"]?.jsonArray?.toBbox()

            return MultiLineString(coords, bbox)
        }
    }
}
