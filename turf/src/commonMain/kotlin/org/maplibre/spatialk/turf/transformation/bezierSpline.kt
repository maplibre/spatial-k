@file:JvmName("Transformation")
@file:JvmMultifileClass

package org.maplibre.spatialk.turf.transformation

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.turf.coordinatemutation.flattenCoordinates

/**
 * Takes a [LineString] and returns a curved version by applying a Bezier spline algorithm.
 *
 * The bezier spline implementation is a port of the implementation by
 * [Leszek Rybicki](http://leszek.rybicki.cc/) used in turfjs.
 *
 * @param duration time in milliseconds between points in the output data
 * @param sharpness a measure of how curvy the path should be between splines
 * @return A [LineString] containing a curved line around the positions of the input line
 */
public fun LineString.bezierSpline(duration: Int = 10_000, sharpness: Double = 0.85): LineString =
    LineString(bezierSpline(this.flattenCoordinates(), duration, sharpness))

private fun bezierSpline(coords: List<Position>, duration: Int, sharpness: Double): List<Position> {
    // utility function to ensure a given altitude
    fun Position.altitude() = altitude ?: 0.0

    val controls = buildList {
        val centers =
            (0..<coords.lastIndex).map { i ->
                val p1 = coords[i]
                val p2 = coords[i + 1]
                Position(
                    longitude = (p1.longitude + p2.longitude) / 2,
                    latitude = (p1.latitude + p2.latitude) / 2,
                    altitude = (p1.altitude() + p2.altitude()) / 2,
                )
            }

        add(Pair(coords[0], coords[0]))
        for (i in 0..<centers.lastIndex) {
            val dx = coords[i + 1].longitude - (centers[i].longitude + centers[i + 1].longitude) / 2
            val dy = coords[i + 1].latitude - (centers[i].latitude + centers[i + 1].latitude) / 2
            val dz =
                coords[i + 1].altitude() - (centers[i].altitude() + centers[i + 1].altitude()) / 2
            add(
                Pair(
                    Position(
                        longitude =
                            (1.0 - sharpness) * coords[i + 1].longitude +
                                sharpness * (centers[i].longitude + dx),
                        latitude =
                            (1.0 - sharpness) * coords[i + 1].latitude +
                                sharpness * (centers[i].latitude + dy),
                        altitude =
                            (1.0 - sharpness) * coords[i + 1].altitude() +
                                sharpness * (centers[i].altitude() + dz),
                    ),
                    Position(
                        longitude =
                            (1.0 - sharpness) * coords[i + 1].longitude +
                                sharpness * (centers[i + 1].longitude + dx),
                        latitude =
                            (1.0 - sharpness) * coords[i + 1].latitude +
                                sharpness * (centers[i + 1].latitude + dy),
                        altitude =
                            (1.0 - sharpness) * coords[i + 1].altitude() +
                                sharpness * (centers[i + 1].altitude() + dz),
                    ),
                )
            )
        }
        add(Pair(coords[coords.lastIndex], coords[coords.lastIndex]))
    }

    fun bezier(t: Double, p1: Position, c1: Position, c2: Position, p2: Position): Position {
        val t2 = t * t
        val t3 = t2 * t
        val b = listOf(t3, 3 * t2 * (1 - t), 3 * t * (1 - t) * (1 - t), (1 - t) * (1 - t) * (1 - t))
        return Position(
            longitude =
                p2.longitude * b[0] +
                    c2.longitude * b[1] +
                    c1.longitude * b[2] +
                    p1.longitude * b[3],
            latitude =
                p2.latitude * b[0] + c2.latitude * b[1] + c1.latitude * b[2] + p1.latitude * b[3],
            altitude =
                p2.altitude() * b[0] +
                    c2.altitude() * b[1] +
                    c1.altitude() * b[2] +
                    p1.altitude() * b[3],
        )
    }

    fun pos(time: Int): Position {
        var t = time.coerceAtLeast(0)
        if (t > duration) {
            t = duration - 1
        }

        val t2 = t.toDouble() / duration
        if (t2 >= 1) {
            return coords[coords.lastIndex]
        }

        val n = (coords.lastIndex * t2).toInt()
        val t1 = coords.lastIndex * t2 - n
        return bezier(t1, coords[n], controls[n].second, controls[n + 1].first, coords[n + 1])
    }

    val positions =
        (0..<duration step 10).plus(duration).mapNotNull { i ->
            if ((i / 100) % 2 != 0) {
                return@mapNotNull null
            }

            val pos = pos(i)
            Position(pos.longitude, pos.latitude)
        }

    return positions
}
