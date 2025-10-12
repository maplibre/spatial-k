@file:JvmName("FeatureConversion")
@file:JvmMultifileClass

package org.maplibre.spatialk.turf.featureconversion

import kotlin.collections.plus
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmOverloads
import kotlinx.serialization.Serializable
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Geometry
import org.maplibre.spatialk.geojson.GeometryCollection
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.LineStringGeometry
import org.maplibre.spatialk.geojson.MultiLineString
import org.maplibre.spatialk.geojson.MultiPoint
import org.maplibre.spatialk.geojson.MultiPolygon
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Polygon
import org.maplibre.spatialk.geojson.dsl.FeatureBuilder
import org.maplibre.spatialk.geojson.dsl.buildFeature
import org.maplibre.spatialk.turf.measurement.area
import org.maplibre.spatialk.turf.measurement.computeBbox
import org.maplibre.spatialk.turf.measurement.toPolygon

// Turf polygonToLine

public fun Polygon.toMultiLineString(): MultiLineString = MultiLineString(coordinates, bbox = bbox)

public fun MultiPolygon.toMultiLineStrings(): GeometryCollection<MultiLineString> =
    GeometryCollection(this.map { it.toMultiLineString() }, bbox = bbox)

// Turf lineToPolygon

@JvmOverloads
public fun LineStringGeometry.toPolygon(
    autoClose: Boolean = true,
    autoOrder: Boolean = true,
): Polygon =
    when (this) {
        is LineString -> toMultiLineString().toPolygon(autoClose, autoOrder)
        is MultiLineString -> {
            var rings = coordinates

            if (autoClose)
                rings = rings.map { if (it.first() != it.last()) it + listOf(it.first()) else it }

            if (autoOrder && size > 1) {
                val largestRing = rings.maxBy { LineString(it).computeBbox().toPolygon().area() }
                rings = listOf(largestRing) + rings.filterNot { it === largestRing }
            }

            Polygon(rings, bbox = bbox)
        }
    }

@JvmOverloads
public fun GeometryCollection<LineStringGeometry>.toMultiPolygon(
    autoClose: Boolean = true,
    autoOrder: Boolean = true,
): MultiPolygon =
    MultiPolygon(geometries.map { it.toPolygon(autoClose, autoOrder).coordinates }, bbox = bbox)

// GeometryCollection <> FeatureCollection

public fun <T : Geometry> FeatureCollection<T?, *>.toGeometryCollection(): GeometryCollection<T> =
    GeometryCollection(features.mapNotNull { it.geometry })

@JvmOverloads
public fun <T : Geometry, P : @Serializable Any> GeometryCollection<T>.toFeatureCollection(
    block: FeatureBuilder<T, P?>.() -> Unit = {}
): FeatureCollection<T, P?> = FeatureCollection(geometries.map { buildFeature(it) { block() } })

// Single -> Multi

public fun Point.toMultiPoint(): MultiPoint = MultiPoint(coordinates, bbox = bbox)

public fun LineString.toMultiLineString(): MultiLineString =
    MultiLineString(coordinates, bbox = bbox)

public fun Polygon.toMultiPolygon(): MultiPolygon = MultiPolygon(coordinates, bbox = bbox)
