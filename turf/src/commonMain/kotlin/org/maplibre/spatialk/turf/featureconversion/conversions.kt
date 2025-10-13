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

/**
 * Converts a [Polygon] to a [MultiLineString] by extracting its rings as line strings.
 *
 * @return A [MultiLineString] containing all rings of the polygon.
 */
public fun Polygon.toMultiLineString(): MultiLineString = MultiLineString(coordinates, bbox = bbox)

/**
 * Converts a [MultiPolygon] to a [GeometryCollection] of [MultiLineString]s by extracting each
 * polygon's rings as [MultiLineString]s.
 *
 * @return A [GeometryCollection] containing [MultiLineString]s, one for each polygon.
 */
public fun MultiPolygon.toMultiLineStrings(): GeometryCollection<MultiLineString> =
    GeometryCollection(this.map { it.toMultiLineString() }, bbox = bbox)

// Turf lineToPolygon

/**
 * Converts a [LineStringGeometry] to a [Polygon]. The largest (or first, if [autoOrder] is false)
 * [LineString] becomes the outer shell of the polygon, and subsequent line strings (if the receiver
 * is [MultiLineString] become holes in the polygon.
 *
 * @param autoClose If true, automatically closes each ring by duplicating the first point at the
 *   end if it's not already closed.
 * @param autoOrder If true, automatically orders rings so the largest ring (by area) becomes the
 *   outer ring and others become holes.
 * @return A [Polygon] created from the line string geometry.
 */
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

/**
 * Converts a [GeometryCollection] of [LineStringGeometry] to a [MultiPolygon] by converting each
 * contained geometry using [toPolygon].
 *
 * @param autoClose If true, automatically closes each ring by duplicating the first point at the
 *   end if it's not already closed.
 * @param autoOrder If true, automatically orders rings so the largest ring (by area) becomes the
 *   outer ring and others become holes.
 * @return A [MultiPolygon] created from the line string geometries.
 */
@JvmOverloads
public fun GeometryCollection<LineStringGeometry>.toMultiPolygon(
    autoClose: Boolean = true,
    autoOrder: Boolean = true,
): MultiPolygon =
    MultiPolygon(geometries.map { it.toPolygon(autoClose, autoOrder).coordinates }, bbox = bbox)

// GeometryCollection <> FeatureCollection

/**
 * Converts a [FeatureCollection] to a [GeometryCollection] by extracting all geometries from the
 * features.
 *
 * @return A [GeometryCollection] containing all non-null geometries from the feature collection.
 */
public fun <T : Geometry> FeatureCollection<T?, *>.toGeometryCollection(): GeometryCollection<T> =
    GeometryCollection(features.mapNotNull { it.geometry })

/**
 * Converts a [GeometryCollection] to a [FeatureCollection] by wrapping each geometry in a feature.
 *
 * @param block Optional configuration block applied to each created feature.
 * @return A [FeatureCollection] containing features for each geometry.
 */
@JvmOverloads
public fun <T : Geometry, P : @Serializable Any> GeometryCollection<T>.toFeatureCollection(
    block: FeatureBuilder<T, P?>.() -> Unit = {}
): FeatureCollection<T, P?> = FeatureCollection(geometries.map { buildFeature(it) { block() } })

// Single -> Multi

/**
 * Converts a [Point] to a [MultiPoint] containing the single point.
 *
 * @return A [MultiPoint] wrapping the point.
 */
public fun Point.toMultiPoint(): MultiPoint = MultiPoint(coordinates, bbox = bbox)

/**
 * Converts a [LineString] to a [MultiLineString] containing the single line string.
 *
 * @return A [MultiLineString] wrapping the line string.
 */
public fun LineString.toMultiLineString(): MultiLineString =
    MultiLineString(coordinates, bbox = bbox)

/**
 * Converts a [Polygon] to a [MultiPolygon] containing the single polygon.
 *
 * @return A [MultiPolygon] wrapping the polygon.
 */
public fun Polygon.toMultiPolygon(): MultiPolygon = MultiPolygon(coordinates, bbox = bbox)
