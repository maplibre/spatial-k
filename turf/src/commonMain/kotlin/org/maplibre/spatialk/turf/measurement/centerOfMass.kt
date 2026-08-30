@file:JvmName("Measurement")
@file:JvmMultifileClass

package org.maplibre.spatialk.turf.measurement

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import org.maplibre.spatialk.geojson.Geometry
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Polygon
import org.maplibre.spatialk.turf.transformation.convex

/**
 * Takes any [Geometry] and returns its center of mass using this formula:
 * [centroid of polygon](https://en.wikipedia.org/wiki/Centroid#Centroid_of_polygon).
 *
 * @return the center of mass
 */
public fun Geometry.centerOfMass(): Point {
    return when (this) {
        is Point -> this
        is Polygon -> {
            // First, we neutralize the feature (set it around coordinates [0,0]) to prevent
            // rounding errors
            // We take any point to translate all the points around 0
            val center = centroid()
            val translation = center.coordinates

            // sx and sy are the sums used to compute the final coordinates
            // sArea is the sum used to compute the signed area
            // Accumulate per ring so the closing coordinate of one ring is never paired with the
            // first coordinate of the next ring; that phantom edge would skew the signed area for
            // polygons with holes.
            val (sx, sy, sArea) =
                coordinates
                    .flatMap { ring ->
                        ring.zipWithNext { currentPoint, nextPoint ->
                            val x1 = currentPoint.longitude - translation.longitude
                            val y1 = currentPoint.latitude - translation.latitude
                            val x2 = nextPoint.longitude - translation.longitude
                            val y2 = nextPoint.latitude - translation.latitude

                            // a is the common factor to compute the signed area and the final
                            // coordinates
                            val a = x1 * y2 - x2 * y1
                            Triple((x1 + x2) * a, (y1 + y2) * a, a)
                        }
                    }
                    .fold(Triple(0.0, 0.0, 0.0)) { acc, (sx, sy, a) ->
                        Triple(acc.first + sx, acc.second + sy, acc.third + a)
                    }

            // Shape has no area: fallback on turf.centroid
            if (sArea == 0.0) {
                center
            } else {
                // Compute the signed area, and factorize 1/6A
                val area = sArea * 0.5

                @Suppress("MagicNumber") val areaFactor = 1 / (6 * area)

                // Compute the final coordinates, adding back the values that have been neutralized
                Point(
                    longitude = translation.longitude + areaFactor * sx,
                    latitude = translation.latitude + areaFactor * sy,
                )
            }
        }

        else -> {
            // Not a polygon: Compute the convex hull and work with that
            val hull = convex()

            if (hull != null) {
                hull.centerOfMass()
            } else {
                // Hull is empty: fallback on the centroid
                centroid()
            }
        }
    }
}
