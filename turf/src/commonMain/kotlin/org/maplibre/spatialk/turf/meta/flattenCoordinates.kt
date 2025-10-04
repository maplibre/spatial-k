@file:JvmName("Meta")
@file:JvmMultifileClass

package org.maplibre.spatialk.turf.meta

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import org.maplibre.spatialk.geojson.*

public fun GeoJsonObject.flattenCoordinates(): List<Position> =
    when (this) {
        is Point -> listOf(coordinates)
        is MultiPoint -> coordinates
        is LineString -> coordinates
        is MultiLineString -> coordinates.flatten()
        is Polygon -> coordinates.flatten()
        is MultiPolygon -> coordinates.flatMap { it.flatten() }
        is GeometryCollection -> geometries.flatMap { it.flattenCoordinates() }
        is Feature<*> -> this.geometry?.flattenCoordinates().orEmpty()
        is FeatureCollection -> features.flatMap { it.flattenCoordinates() }
    }
