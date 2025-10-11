@file:JvmName("FeatureConversion")
@file:JvmMultifileClass

package org.maplibre.spatialk.turf.featureconversion

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import org.maplibre.spatialk.geojson.*
import org.maplibre.spatialk.geojson.dsl.feature
import org.maplibre.spatialk.geojson.dsl.featureCollection

/**
 * Combines a [FeatureCollection] of [Point], [LineString], or [Polygon] features into [MultiPoint],
 * [MultiLineString], or [MultiPolygon] features.
 */
public fun FeatureCollection<SingleGeometry>.combine(): FeatureCollection<MultiGeometry> {
    val points = mutableListOf<Point>()
    val lines = mutableListOf<LineString>()
    val polygons = mutableListOf<Polygon>()

    this.features.forEach {
        when (val geometry = it.geometry) {
            is Point -> points.add(geometry)
            is LineString -> lines.add(geometry)
            is Polygon -> polygons.add(geometry)
        }
    }

    return featureCollection {
        feature(MultiPoint(points.map { it.coordinates }))
        feature(MultiLineString(lines.map { it.coordinates }))
        feature(MultiPolygon(polygons.map { it.coordinates }))
    }
}
