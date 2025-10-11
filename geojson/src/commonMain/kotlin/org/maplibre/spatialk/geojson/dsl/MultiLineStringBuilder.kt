package org.maplibre.spatialk.geojson.dsl

import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.MultiLineString
import org.maplibre.spatialk.geojson.Position

@GeoJsonDsl
public class MultiLineStringBuilder {
    public var bbox: BoundingBox? = null
    private val coordinates: MutableList<List<Position>> = mutableListOf()

    public fun add(lineString: LineString) {
        coordinates.add(lineString.coordinates)
    }

    public fun build(): MultiLineString = MultiLineString(coordinates, bbox)
}
