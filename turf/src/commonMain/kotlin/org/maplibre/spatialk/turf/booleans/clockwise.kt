@file:JvmName("Booleans")
@file:JvmMultifileClass

package org.maplibre.spatialk.turf.booleans

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.Position

/**
 * Takes a ring and returns true or false whether the ring is clockwise or counter-clockwise.
 *
 * @param line to be evaluated
 * @return `true` if clockwise, `false` if counter-clockwise
 */
public fun clockwise(line: LineString): Boolean = clockwise(line.coordinates)

/**
 * Takes a ring and returns true or false whether the ring is clockwise or counter-clockwise.
 *
 * @param ring to be evaluated
 * @return `true` if clockwise, `false` if counter-clockwise
 */
public fun clockwise(ring: List<Position>): Boolean =
    ring
        .zipWithNext { cur, next ->
            (next.longitude - cur.longitude) * (next.latitude + cur.latitude)
        }
        .sum()
        .plus(
            (ring.last().longitude - ring.first().longitude) *
                (ring.last().latitude + ring.first().latitude)
        ) > 0
