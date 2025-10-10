package org.maplibre.spatialk.geojson.serialization

import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.json.JsonObject
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
    GeoJsonTypePolymorphicSerializer<GeoJsonObject>(GeoJsonObject::class) {

    override fun selectSerializer(type: String) =
        when (type) {
            "Point" -> Point.serializer()
            "MultiPoint" -> MultiPoint.serializer()
            "LineString" -> LineString.serializer()
            "MultiLineString" -> MultiLineString.serializer()
            "Polygon" -> Polygon.serializer()
            "MultiPolygon" -> MultiPolygon.serializer()
            "GeometryCollection" -> GeometryCollection.serializer()
            "Feature" ->
                Feature.serializer(Geometry.serializer().nullable, JsonObject.serializer().nullable)
            "FeatureCollection" -> FeatureCollection.serializer(JsonObject.serializer().nullable)
            else -> throw SerializationException("Unknown type: $type")
        }
}
