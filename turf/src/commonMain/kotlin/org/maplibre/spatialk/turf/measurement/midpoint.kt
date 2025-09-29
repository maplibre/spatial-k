package org.maplibre.spatialk.turf.measurement

import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.turf.ExperimentalTurfApi

/**
 * Takes two [Position]s and returns a point midway between them. The midpoint is calculated
 * geodesically, meaning the curvature of the earth is taken into account.
 *
 * @param point1 the first point
 * @param point2 the second point
 * @return A [Position] midway between [point1] and [point2]
 */
@ExperimentalTurfApi
public fun midpoint(point1: Position, point2: Position): Position {
    val dist = distance(point1, point2)
    val heading = bearing(point1, point2)

    return destination(point1, dist / 2, heading)
}
