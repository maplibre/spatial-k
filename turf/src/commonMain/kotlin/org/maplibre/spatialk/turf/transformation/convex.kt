@file:JvmName("Transformation")
@file:JvmMultifileClass

package org.maplibre.spatialk.turf.transformation

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import org.maplibre.spatialk.geojson.Geometry
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Polygon
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.turf.coordinatemutation.flattenCoordinates

/**
 * Takes a [Geometry] and returns a convex hull [Polygon].
 *
 * Internally this implements a
 * [monotone chain hull](http://en.wikibooks.org/wiki/Algorithm_Implementation/Geometry/Convex_hull/Monotone_chain).
 *
 * @param concavity 1 - thin shape. `Int.MAX_VALUE` - convex hull.
 * @return a convex hull, or `null` when input is empty or a hull cannot be calculated
 *
 * Note: the default (convex) path is a monotone chain matching Turf.js's convex hull. The concave
 * path (`concavity < Int.MAX_VALUE`) uses GeoKJSON's k-nearest-neighbours `ConcaveHull` (Moreira &
 * Santos), which differs from Turf.js's `concaveman` — results for concave hulls will not match
 * Turf. Also, the convex hull ring is rotated to start at the southernmost (then westernmost)
 * vertex to match the fixture convention; Turf starts at the minimum (x, y) vertex, so on inputs
 * outside the fixtures the ring's starting vertex may differ from Turf (the topology is the same).
 */
public fun Geometry.convex(concavity: Int = Int.MAX_VALUE): Polygon? {
    val positions = this.flattenCoordinates()
    if (positions.isEmpty()) {
        return null
    }

    // The default (convex) case is a true convex hull computed with Andrew's monotone chain. The
    // ConcaveHull-based path used here previously could not produce a hull for inputs with exactly
    // 4 distinct points (e.g. a square), even though such a hull exists.
    if (concavity >= Int.MAX_VALUE) {
        return convexHull(positions)
    }

    // Smaller concavity values request a concave hull, delegated to the k-nearest-neighbours
    // implementation.
    val hull =
        ConcaveHull.calculateConcaveHull(
            // drop altitude
            positions.map { Point(longitude = it.longitude, latitude = it.latitude) },
            concavity,
        )

    // A hull needs at least 3 different vertices in order to create a valid polygon.
    return hull
        .takeIf { it.size >= MIN_COORDINATE_SIZE_HULL }
        ?.let { h ->
            // ConcaveHull does not guarantee a closed ring (e.g. for 4-point inputs it can return
            // the vertices in order without repeating the start), and spatial-k's Polygon requires
            // closed rings, so close the ring before constructing the polygon.
            val ring = if (h.first() == h.last()) h else h + h.first()
            Polygon(ring.map { point -> point.coordinates })
        }
}

/**
 * Computes the convex hull of [positions] with Andrew's monotone chain algorithm. Altitude is
 * ignored. Returns `null` when fewer than 3 distinct vertices remain.
 */
private fun convexHull(positions: List<Position>): Polygon? {
    val points =
        positions
            .map { it.longitude to it.latitude }
            .distinct()
            .sortedWith(compareBy({ it.first }, { it.second }))
    if (points.size < MIN_COORDINATE_SIZE_HULL) {
        return null
    }

    fun cross(o: Pair<Double, Double>, a: Pair<Double, Double>, b: Pair<Double, Double>): Double =
        (a.first - o.first) * (b.second - o.second) - (a.second - o.second) * (b.first - o.first)

    val lower = mutableListOf<Pair<Double, Double>>()
    for (point in points) {
        while (
            lower.size >= 2 && cross(lower[lower.size - 2], lower[lower.size - 1], point) <= 0.0
        ) {
            lower.removeAt(lower.lastIndex)
        }
        lower.add(point)
    }

    val upper = mutableListOf<Pair<Double, Double>>()
    for (point in points.asReversed()) {
        while (
            upper.size >= 2 && cross(upper[upper.size - 2], upper[upper.size - 1], point) <= 0.0
        ) {
            upper.removeAt(upper.lastIndex)
        }
        upper.add(point)
    }

    val hull = lower.dropLast(1) + upper.dropLast(1)
    if (hull.size < MIN_COORDINATE_SIZE_HULL) {
        return null
    }

    // Rotate the ring to start at the southernmost (then westernmost) vertex so the ordering
    // matches the previous implementation and the fixtures.
    var startIndex = 0
    for (index in hull.indices) {
        if (
            hull[index].second < hull[startIndex].second ||
                hull[index].second == hull[startIndex].second &&
                    hull[index].first < hull[startIndex].first
        ) {
            startIndex = index
        }
    }
    val ring = hull.subList(startIndex, hull.size) + hull.subList(0, startIndex)
    return Polygon((ring + ring.first()).map { Position(it.first, it.second) })
}

private const val MIN_COORDINATE_SIZE_HULL = 3
