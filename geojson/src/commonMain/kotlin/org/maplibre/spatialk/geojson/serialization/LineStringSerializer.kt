package org.maplibre.spatialk.geojson.serialization

import kotlinx.serialization.builtins.ListSerializer
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.Position

internal object LineStringSerializer :
    BaseGeometrySerializer<LineString, List<Position>>(
        "LineString",
        ListSerializer(Position.serializer()),
    ) {
    override fun getCoordinates(value: LineString) = value.coordinates

    override fun construct(coordinates: List<Position>, bbox: BoundingBox?) =
        LineString(coordinates, bbox)
}
