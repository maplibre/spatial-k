package org.maplibre.spatialk.geojson.serialization

import kotlinx.serialization.builtins.ListSerializer
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.Polygon
import org.maplibre.spatialk.geojson.Position

internal object PolygonSerializer :
    BaseGeometrySerializer<Polygon, List<List<Position>>>(
        "Polygon",
        ListSerializer(ListSerializer(Position.serializer())),
    ) {
    override fun getCoordinates(value: Polygon) = value.coordinates

    override fun construct(coordinates: List<List<Position>>, bbox: BoundingBox?) =
        Polygon(coordinates, bbox)
}
