@file:JvmName("CoordinateMutation")
@file:JvmMultifileClass

package org.maplibre.spatialk.turf.coordinatemutation

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import org.maplibre.spatialk.geojson.*

/** @return A list of all the coordinates of all the geometry contained in this object. */
public fun GeoJsonObject.flattenCoordinates(): List<Position> =
    when (this) {
        is Point -> listOf(coordinates)
        is MultiPoint -> coordinates
        is LineString -> coordinates
        is MultiLineString -> coordinates.flatten()
        is Polygon -> coordinates.flatten()
        is MultiPolygon -> coordinates.flatMap { it.flatten() }
        is GeometryCollection<*> -> geometries.flatMap { it.flattenCoordinates() }
        is Feature<*, *> -> this.geometry?.flattenCoordinates().orEmpty()
        is FeatureCollection<*, *> -> features.flatMap { it.flattenCoordinates() }
    }
