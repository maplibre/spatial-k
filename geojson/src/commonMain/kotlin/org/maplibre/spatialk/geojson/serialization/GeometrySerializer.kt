package org.maplibre.spatialk.geojson.serialization

import kotlinx.serialization.SerializationException
import org.maplibre.spatialk.geojson.Geometry
import org.maplibre.spatialk.geojson.GeometryCollection
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.MultiLineString
import org.maplibre.spatialk.geojson.MultiPoint
import org.maplibre.spatialk.geojson.MultiPolygon
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Polygon

internal object GeometrySerializer : GeoJsonTypePolymorphicSerializer<Geometry>(Geometry::class) {

    override fun selectSerializer(type: String) =
        when (type) {
            "Point" -> Point.serializer()
            "MultiPoint" -> MultiPoint.serializer()
            "LineString" -> LineString.serializer()
            "MultiLineString" -> MultiLineString.serializer()
            "Polygon" -> Polygon.serializer()
            "MultiPolygon" -> MultiPolygon.serializer()
            "GeometryCollection" -> GeometryCollection.serializer()
            else -> throw SerializationException("Unknown type: $type")
        }
}
