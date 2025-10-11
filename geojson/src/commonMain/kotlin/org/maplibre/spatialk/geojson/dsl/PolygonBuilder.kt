package org.maplibre.spatialk.geojson.dsl

import kotlin.collections.plus
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.Polygon
import org.maplibre.spatialk.geojson.Position

@GeoJsonDsl
public class PolygonBuilder() {
    public var bbox: BoundingBox? = null
    private val coordinates: MutableList<List<Position>> = mutableListOf()

    public fun add(ring: List<Position>) {
        require(ring.isNotEmpty()) { "Polygon ring cannot be empty" }
        coordinates += if (ring.first() == ring.last()) ring else ring + listOf(ring.first())
    }

    public fun add(ring: LineString) {
        add(ring.coordinates)
    }

    public fun build(): Polygon = Polygon(coordinates, bbox)
}
