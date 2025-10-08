package org.maplibre.spatialk.geojson.serialization

import kotlinx.serialization.builtins.ListSerializer
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.MultiPoint
import org.maplibre.spatialk.geojson.Position

internal object MultiPointSerializer :
    BaseGeometrySerializer<MultiPoint, List<Position>>(
        "MultiPoint",
        ListSerializer(Position.serializer()),
    ) {
    override fun getCoordinates(value: MultiPoint) = value.coordinates

    override fun construct(coordinates: List<Position>, bbox: BoundingBox?) =
        MultiPoint(coordinates, bbox)
}
