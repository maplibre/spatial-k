package org.maplibre.spatialk.geojson.serialization

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.maplibre.spatialk.geojson.Geometry
import org.maplibre.spatialk.geojson.GeometryCollection
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.MultiLineString
import org.maplibre.spatialk.geojson.MultiPoint
import org.maplibre.spatialk.geojson.MultiPolygon
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Polygon

internal object GeometrySerializer : JsonContentPolymorphicSerializer<Geometry>(Geometry::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<Geometry> {
        val json = element.jsonObject
        val type = json.getValue("type").jsonPrimitive.content
        return when (type) {
            GeometryCollection.JSON_NAME -> GeometryCollection.serializer()
            LineString.JSON_NAME -> LineString.serializer()
            MultiLineString.JSON_NAME -> MultiLineString.serializer()
            MultiPoint.JSON_NAME -> MultiPoint.serializer()
            MultiPolygon.JSON_NAME -> MultiPolygon.serializer()
            Point.JSON_NAME -> Point.serializer()
            Polygon.JSON_NAME -> Polygon.serializer()
            else -> error("$type is not a valid Geometry type")
        }
    }
}
