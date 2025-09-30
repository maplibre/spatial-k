package org.maplibre.spatialk.geojson.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.ClassDiscriminatorMode
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.maplibre.spatialk.geojson.Geometry
import org.maplibre.spatialk.geojson.GeometryCollection
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.MultiLineString
import org.maplibre.spatialk.geojson.MultiPoint
import org.maplibre.spatialk.geojson.MultiPolygon
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Polygon

public val GeoJson: Json = Json {
    @OptIn(ExperimentalSerializationApi::class)
    classDiscriminatorMode = ClassDiscriminatorMode.ALL_JSON_OBJECTS
    ignoreUnknownKeys = true
    serializersModule = GeometrySerializersModule
}

private val GeometrySerializersModule = SerializersModule {
    polymorphic(Geometry::class) {
        subclass(GeometryCollection::class)
        subclass(LineString::class)
        subclass(MultiLineString::class)
        subclass(MultiPoint::class)
        subclass(MultiPolygon::class)
        subclass(Point::class)
        subclass(Polygon::class)
    }
}
