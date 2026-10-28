@file:JvmName("CoordinateMutation")
@file:JvmMultifileClass

package org.maplibre.spatialk.turf.coordinatemutation

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlinx.serialization.Serializable
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

/**
 * Takes a [Geometry] and flips all of its coordinates from `[longitude, latitude]` to `[latitude,
 * longitude]`.
 *
 * @return the flipped geometry
 */
public inline fun <reified T : Geometry> T.flip(): T = flipGeometry(this) as T

/**
 * Takes a [FeatureCollection] and flips all of its coordinates from `[longitude, latitude]` to
 * `[latitude, longitude]`.
 *
 * @return the flipped feature collection
 */
@Suppress("UNCHECKED_CAST")
public fun <G : Geometry?, P : @Serializable Any?> FeatureCollection<G, P>.flip():
    FeatureCollection<G, P> {
    val features =
        this.features.map { feature ->
            feature.copy(geometry = feature.geometry?.let { flipGeometry(it) } as G, bbox = null)
        }
    // The bbox of a flipped feature collection would still describe the unflipped coordinates, so
    // drop it like the flipped geometries do.
    return this.copy(features = features, bbox = null)
}

@PublishedApi
internal fun flipGeometry(geometry: Geometry): Geometry =
    when (geometry) {
        is GeometryCollection<*> -> GeometryCollection(geometry.geometries.map(::flipGeometry))
        is LineString -> LineString(geometry.coordinates.map(Position::flip))
        is MultiLineString -> MultiLineString(geometry.coordinates.map { it.map(Position::flip) })
        is MultiPoint -> MultiPoint(geometry.coordinates.map(Position::flip))
        is MultiPolygon ->
            MultiPolygon(
                geometry.coordinates.map { polygon -> polygon.map { it.map(Position::flip) } }
            )
        is Point -> Point(geometry.coordinates.flip())
        is Polygon -> Polygon(geometry.coordinates.map { it.map(Position::flip) })
    }

@PublishedApi internal fun Position.flip(): Position = Position(latitude, longitude, altitude)
