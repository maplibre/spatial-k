package org.maplibre.spatialk.geojson.dsl

import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.Geometry
import org.maplibre.spatialk.geojson.GeometryCollection

@GeoJsonDsl
public class GeometryCollectionBuilder<T : Geometry> {
    public var bbox: BoundingBox? = null
    private val geometries: MutableList<T> = mutableListOf()

    public fun add(geometry: T) {
        geometries.add(geometry)
    }

    public fun build(): GeometryCollection<T> = GeometryCollection(geometries, bbox)
}
