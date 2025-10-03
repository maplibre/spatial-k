@file:JvmName("Meta")
@file:JvmMultifileClass

package org.maplibre.spatialk.turf.meta

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import org.maplibre.spatialk.geojson.*

public fun Geometry.coordAll(): List<Position> =
    when (this) {
        is Point -> listOf(coordinates)
        is MultiPoint -> coordinates
        is LineString -> coordinates
        is MultiLineString -> coordinates.flatten()
        is Polygon -> coordinates.flatten()
        is MultiPolygon -> coordinates.flatMap { it.flatten() }
        is GeometryCollection -> geometries.flatMap { it.coordAll() }
    }

public fun GeoJsonObject.coordAll(): List<Position>? =
    when (this) {
        is Geometry -> this.coordAll()
        is Feature<*> -> this.geometry?.coordAll()
        is FeatureCollection -> features.mapNotNull { it.coordAll() }.ifEmpty { null }?.flatten()
    }
