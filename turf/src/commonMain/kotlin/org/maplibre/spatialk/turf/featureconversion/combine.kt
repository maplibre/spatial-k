@file:JvmName("FeatureConversion")
@file:JvmMultifileClass

package org.maplibre.spatialk.turf.featureconversion

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import org.maplibre.spatialk.geojson.*

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

    return FeatureCollection(
        Feature(MultiPoint(points.map { it.coordinates })),
        Feature(MultiLineString(lines.map { it.coordinates })),
        Feature(MultiPolygon(polygons.map { it.coordinates })),
    )
}
