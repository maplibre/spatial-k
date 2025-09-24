package org.maplibre.spatialk.geojson

import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.maplibre.spatialk.geojson.serialization.GeometrySerializer
import org.maplibre.spatialk.geojson.serialization.jsonJoin
import org.maplibre.spatialk.geojson.serialization.jsonProp
import org.maplibre.spatialk.geojson.serialization.toBbox

@Suppress("SERIALIZER_TYPE_INCOMPATIBLE")
@Serializable(with = GeometrySerializer::class)
public class GeometryCollection
@JvmOverloads
constructor(public val geometries: List<Geometry>, override val bbox: BoundingBox? = null) :
    Geometry(), Collection<Geometry> by geometries {
    @JvmOverloads
    public constructor(
        vararg geometries: Geometry,
        bbox: BoundingBox? = null,
    ) : this(geometries.toList(), bbox)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as GeometryCollection

        if (geometries != other.geometries) return false
        if (bbox != other.bbox) return false

        return true
    }

    override fun hashCode(): Int {
        var result = geometries.hashCode()
        result = 31 * result + (bbox?.hashCode() ?: 0)
        return result
    }

    override fun json(): String =
        """{"type":"GeometryCollection",${bbox.jsonProp()}"geometries":${geometries.jsonJoin { it.json() }}}"""

    public companion object {
        @JvmStatic
        public fun fromJson(json: String): GeometryCollection =
            fromJson(Json.decodeFromString(JsonObject.serializer(), json))

        @JvmStatic
        public fun fromJsonOrNull(json: String): GeometryCollection? =
            try {
                fromJson(json)
            } catch (_: Exception) {
                null
            }

        @JvmStatic
        public fun fromJson(json: JsonObject): GeometryCollection {
            try {
                require(json.getValue("type").jsonPrimitive.content == "GeometryCollection") {
                    "Object \"type\" is not \"GeometryCollection\"."
                }

                val geometries =
                    json.getValue("geometries").jsonArray.map { Geometry.fromJson(it.jsonObject) }

                val bbox = json["bbox"]?.jsonArray?.toBbox()

                return GeometryCollection(geometries, bbox)
            } catch (e: IllegalArgumentException) {
                throw SerializationException(e.message)
            }
        }
    }
}
