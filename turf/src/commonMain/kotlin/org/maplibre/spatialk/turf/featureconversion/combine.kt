@file:JvmName("FeatureConversion")
@file:JvmMultifileClass

package org.maplibre.spatialk.turf.featureconversion

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import org.maplibre.spatialk.geojson.*

/**
 * Combines a [GeometryCollection] of [Point], [LineString], or [Polygon] features into
 * [MultiPoint], [MultiLineString], or [MultiPolygon] features.
 */
public fun GeometryCollection<SingleGeometry>.combine(): GeometryCollection<MultiGeometry> {
    val points = mutableListOf<Point>()
    val lines = mutableListOf<LineString>()
    val polygons = mutableListOf<Polygon>()

    this.geometries.forEach {
        when (it) {
            is Point -> points.add(it)
            is LineString -> lines.add(it)
            is Polygon -> polygons.add(it)
        }
    }

    return GeometryCollection(
        MultiPoint(points.map { it.coordinates }),
        MultiLineString(lines.map { it.coordinates }),
        MultiPolygon(polygons.map { it.coordinates }),
    )
}
