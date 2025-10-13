@file:JvmName("Miscellaneous")
@file:JvmMultifileClass

package org.maplibre.spatialk.turf.misc

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import org.maplibre.spatialk.geojson.GeometryCollection
import org.maplibre.spatialk.geojson.MultiPoint
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.PointGeometry
import org.maplibre.spatialk.geojson.PolygonGeometry
import org.maplibre.spatialk.turf.booleans.contains

public fun <G : PointGeometry> GeometryCollection<G>.filterInside(
    polygons: GeometryCollection<PolygonGeometry>
): GeometryCollection<G> {
    val results = mutableListOf<G>()

    forEach { pointGeometry ->
        when (pointGeometry) {
            is Point ->
                if (polygons.any { pointGeometry.coordinates in it }) results.add(pointGeometry)
            is MultiPoint -> {
                val pointsInside =
                    pointGeometry.filter { point -> polygons.any { point.coordinates in it } }
                @Suppress("UNCHECKED_CAST")
                if (pointsInside.isNotEmpty())
                    results.add(MultiPoint(pointsInside.map { it.coordinates }) as G)
            }
        }
    }

    return GeometryCollection(results)
}
