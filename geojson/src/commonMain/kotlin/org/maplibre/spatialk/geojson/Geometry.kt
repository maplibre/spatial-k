package org.maplibre.spatialk.geojson

import org.maplibre.spatialk.geojson.serialization.GeometrySerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.jvm.JvmStatic

@Serializable(with = GeometrySerializer::class)
public sealed class Geometry protected constructor() : GeoJson {
    abstract override val bbox: BoundingBox?

    override fun toString(): String = json()

    public companion object {
        @JvmStatic
        public fun fromJson(json: String): Geometry =
            fromJson(Json.decodeFromString(JsonObject.serializer(), json))

        @JvmStatic
        public fun fromJsonOrNull(json: String): Geometry? = try {
            fromJson(json)
        } catch (_: Exception) {
            null
        }

        @JvmStatic
        public fun fromJson(json: JsonObject): Geometry =
            try {
                when (val type = json.getValue("type").jsonPrimitive.content) {
                    "Point" -> Point.fromJson(json)
                    "MultiPoint" -> MultiPoint.fromJson(json)
                    "LineString" -> LineString.fromJson(json)
                    "MultiLineString" -> MultiLineString.fromJson(json)
                    "Polygon" -> Polygon.fromJson(json)
                    "MultiPolygon" -> MultiPolygon.fromJson(json)
                    "GeometryCollection" -> GeometryCollection.fromJson(json)
                    else -> throw IllegalArgumentException("Unsupported Geometry type \"$type\"")
                }
            } catch (e: IllegalArgumentException) {
                throw SerializationException(e.message)
            }
    }
}



