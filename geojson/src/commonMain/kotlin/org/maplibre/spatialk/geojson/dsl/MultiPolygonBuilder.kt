package org.maplibre.spatialk.geojson.dsl

import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.MultiPolygon
import org.maplibre.spatialk.geojson.Polygon
import org.maplibre.spatialk.geojson.Position

/**
 * Builder for constructing [MultiPolygon] objects using a DSL.
 *
 * @property bbox An optional [BoundingBox] for this [MultiPolygon].
 * @see MultiPolygon
 * @see buildMultiPolygon
 * @see addPolygon
 */
@GeoJsonDsl
public class MultiPolygonBuilder {
    public var bbox: BoundingBox? = null
    private val coordinates: MutableList<List<List<Position>>> = mutableListOf()

    /**
     * Adds a [Polygon] to this [MultiPolygon].
     *
     * @param polygon The [Polygon] to add.
     */
    public fun add(polygon: Polygon) {
        coordinates.add(polygon.coordinates)
    }

    /**
     * Builds the [MultiPolygon] from the configured values.
     *
     * @return The constructed [MultiPolygon].
     */
    public fun build(): MultiPolygon = MultiPolygon(coordinates, bbox)
}
