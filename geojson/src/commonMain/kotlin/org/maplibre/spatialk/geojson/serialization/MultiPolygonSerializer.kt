package org.maplibre.spatialk.geojson.serialization

import kotlinx.serialization.builtins.ListSerializer
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.MultiPolygon
import org.maplibre.spatialk.geojson.Position

internal object MultiPolygonSerializer :
    BaseGeometrySerializer<MultiPolygon, List<List<List<Position>>>>(
        "MultiPolygon",
        ListSerializer(ListSerializer(ListSerializer(Position.serializer()))),
    ) {
    override fun getCoordinates(value: MultiPolygon) = value.coordinates

    override fun construct(coordinates: List<List<List<Position>>>, bbox: BoundingBox?) =
        MultiPolygon(coordinates, bbox)
}
