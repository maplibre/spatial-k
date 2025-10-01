@file:JvmName("Meta")
@file:JvmMultifileClass

package org.maplibre.spatialk.turf.meta

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Geometry
import org.maplibre.spatialk.geojson.GeometryCollection
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.MultiLineString
import org.maplibre.spatialk.geojson.MultiPoint
import org.maplibre.spatialk.geojson.MultiPolygon
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Polygon
import org.maplibre.spatialk.geojson.Position

public fun Geometry.coordAll(): List<Position> =
    when (this) {
        is Point -> this.coordAll()
        is MultiPoint -> this.coordAll()
        is LineString -> this.coordAll()
        is MultiLineString -> this.coordAll()
        is Polygon -> this.coordAll()
        is MultiPolygon -> this.coordAll()
        is GeometryCollection -> this.coordAll()
    }

public fun Point.coordAll(): List<Position> = listOf(coordinates)

public fun MultiPoint.coordAll(): List<Position> = coordinates

public fun LineString.coordAll(): List<Position> = coordinates

public fun MultiLineString.coordAll(): List<Position> =
    coordinates.reduce { acc, list -> acc + list }

public fun Polygon.coordAll(): List<Position> = coordinates.reduce { acc, list -> acc + list }

public fun MultiPolygon.coordAll(): List<Position> =
    coordinates.fold(emptyList()) { acc, list ->
        list.reduce { innerAcc, innerList -> innerAcc + innerList } + acc
    }

public fun GeometryCollection.coordAll(): List<Position> =
    geometries.fold(emptyList()) { acc, geometry -> acc + geometry.coordAll() }

public fun Feature<*>.coordAll(): List<Position>? = geometry?.coordAll()

public fun FeatureCollection.coordAll(): List<Position> =
    features.fold(emptyList()) { acc, feature -> acc + (feature.coordAll() ?: emptyList()) }
