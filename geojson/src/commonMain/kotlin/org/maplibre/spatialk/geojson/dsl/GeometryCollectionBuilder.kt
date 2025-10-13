package org.maplibre.spatialk.geojson.dsl

import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.Geometry
import org.maplibre.spatialk.geojson.GeometryCollection

/**
 * Builder for constructing [GeometryCollection] objects using a DSL.
 *
 * @property bbox An optional [BoundingBox] for this [GeometryCollection].
 * @see GeometryCollection
 * @see buildGeometryCollection
 * @see addPoint
 * @see addLineString
 * @see addPolygon
 * @see addMultiPoint
 * @see addMultiLineString
 * @see addMultiPolygon
 * @see addGeometryCollection
 */
@GeoJsonDsl
public class GeometryCollectionBuilder<G : Geometry> {
    public var bbox: BoundingBox? = null
    private val geometries: MutableList<G> = mutableListOf()

    /**
     * Adds a [Geometry] to this [GeometryCollection].
     *
     * @param geometry The [Geometry] to add.
     */
    public fun add(geometry: G) {
        geometries.add(geometry)
    }

    /**
     * Builds the [GeometryCollection] from the configured values.
     *
     * @return The constructed [GeometryCollection].
     */
    public fun build(): GeometryCollection<G> = GeometryCollection(geometries, bbox)
}
