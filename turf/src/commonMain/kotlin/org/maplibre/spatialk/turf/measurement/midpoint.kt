@file:JvmName("Measurement")
@file:JvmMultifileClass

package org.maplibre.spatialk.turf.measurement

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position

/**
 * Takes two [Position]s and returns a point midway between them. The midpoint is calculated
 * geodesically, meaning the curvature of the earth is taken into account.
 *
 * @param pos1 the first point
 * @param pos2 the second point
 * @return A [Position] midway between [pos1] and [pos2]
 */
public fun midpoint(pos1: Position, pos2: Position): Position {
    val dist = distance(pos1, pos2)
    val heading = pos1.bearingTo(pos2)

    return pos1.offset(dist / 2.0, heading)
}

/**
 * Takes two [Point]s and returns a point midway between them. The midpoint is calculated
 * geodesically, meaning the curvature of the earth is taken into account.
 *
 * @param pos1 the first point
 * @param pos2 the second point
 * @return A [Position] midway between [pos1] and [pos2]
 */
public fun midpoint(pos1: Point, pos2: Point): Position =
    midpoint(pos1.coordinates, pos2.coordinates)
