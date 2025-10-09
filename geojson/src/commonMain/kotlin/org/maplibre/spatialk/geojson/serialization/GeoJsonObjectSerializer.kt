package org.maplibre.spatialk.geojson.serialization

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonPrimitive
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.GeoJsonObject
import org.maplibre.spatialk.geojson.Geometry
import org.maplibre.spatialk.geojson.GeometryCollection
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.MultiLineString
import org.maplibre.spatialk.geojson.MultiPoint
import org.maplibre.spatialk.geojson.MultiPolygon
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Polygon

internal object GeoJsonObjectSerializer :
    JsonContentPolymorphicSerializer<GeoJsonObject>(GeoJsonObject::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<GeoJsonObject> {
        element as? JsonObject ?: throw SerializationException("Expected JSON object")
        val type = element["type"]?.let { Json.decodeFromJsonElement<String>(it.jsonPrimitive) }
        return when (type) {
            // geometry types
            "Point" -> Point.serializer()
            "MultiPoint" -> MultiPoint.serializer()
            "LineString" -> LineString.serializer()
            "MultiLineString" -> MultiLineString.serializer()
            "Polygon" -> Polygon.serializer()
            "MultiPolygon" -> MultiPolygon.serializer()
            "GeometryCollection" -> GeometryCollection.serializer()
            // other geojson object types
            "Feature" -> Feature.serializer(Geometry.serializer().nullable)
            "FeatureCollection" -> FeatureCollection.serializer()
            else -> throw SerializationException("Unknown GeoJSON type '$type'")
        }
    }
}
