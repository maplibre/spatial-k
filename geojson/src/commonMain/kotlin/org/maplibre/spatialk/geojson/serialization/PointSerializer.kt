package org.maplibre.spatialk.geojson.serialization

import kotlinx.serialization.json.JsonElement
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position

internal object PointSerializer :
    BaseGeometrySerializer<Point, Position>("Point", Position.serializer()) {
    override fun getCoordinates(value: Point) = value.coordinates

    override fun construct(
        coordinates: Position,
        bbox: BoundingBox?,
        foreignMembers: Map<String, JsonElement>,
    ) = Point(coordinates, bbox, foreignMembers)
}
