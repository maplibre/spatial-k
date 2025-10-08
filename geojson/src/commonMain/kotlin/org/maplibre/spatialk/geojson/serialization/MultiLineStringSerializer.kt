package org.maplibre.spatialk.geojson.serialization

import kotlinx.serialization.builtins.ListSerializer
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.MultiLineString
import org.maplibre.spatialk.geojson.Position

internal object MultiLineStringSerializer :
    BaseGeometrySerializer<MultiLineString, List<List<Position>>>(
        "MultiLineString",
        ListSerializer(ListSerializer(Position.serializer())),
    ) {
    override fun getCoordinates(value: MultiLineString) = value.coordinates

    override fun construct(coordinates: List<List<Position>>, bbox: BoundingBox?) =
        MultiLineString(coordinates, bbox)
}
