package org.maplibre.spatialk.gpx

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position

public fun Waypoint.toGeoJson(): Feature<Point> {
    val obj = Json.encodeToJsonElement(Waypoint.serializer(), this) as JsonObject
    val properties = obj.toMutableMap()
    properties.remove("lon")
    properties.remove("lat")
    properties.remove("ele")

    return Feature(geometry = Point(Position(lon, lat, ele)), properties = JsonObject(properties))
}

public fun Feature<Point>.toGpx(): Waypoint {
    val properties = properties?.toMutableMap() ?: mutableMapOf()

    properties["lon"] = JsonPrimitive(geometry?.coordinates?.longitude)
    properties["lat"] = JsonPrimitive(geometry?.coordinates?.latitude)
    properties["ele"] = JsonPrimitive(geometry?.coordinates?.altitude)

    return Json.decodeFromJsonElement(Waypoint.serializer(), JsonObject(properties))
}

public fun List<Waypoint>.toGeoJson(): FeatureCollection {
    return FeatureCollection(map { it.toGeoJson() })
}

// public fun FeatureCollection.toGpx(): List<Waypoint> {
//
//    return features.map { (it as Feature<Point>).toGpx() }
// }
