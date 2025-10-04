@file:JvmName("Measurement")
@file:JvmMultifileClass

package org.maplibre.spatialk.turf.measurement

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import org.maplibre.spatialk.geojson.Position

/**
 * Takes two [Position]s and returns a point midway between them. The midpoint is calculated
 * geodesically, meaning the curvature of the earth is taken into account.
 *
 * @param from the first point
 * @param to the second point
 * @return A [Position] midway between [from] and [to]
 */
public fun midpoint(from: Position, to: Position): Position {
    val dist = distance(from, to)
    val heading = bearing(from, to)

    return from.offset(dist / 2.0, heading)
}
