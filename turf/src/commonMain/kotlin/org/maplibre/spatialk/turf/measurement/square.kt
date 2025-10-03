@file:JvmName("Measurement")
@file:JvmMultifileClass

package org.maplibre.spatialk.turf.measurement

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.Position

/**
 * Takes a bounding box and calculates the minimum square bounding box that would contain the input.
 *
 * @return [BoundingBox] a square surrounding [this@square]
 */
public fun BoundingBox.square(): BoundingBox {
    val (east, north) = northeast
    val (west, south) = southwest

    val horizontalDistance = distance(Position(west, south), Position(east, south))
    val verticalDistance = distance(Position(west, south), Position(west, north))
    return if (horizontalDistance >= verticalDistance) {
        val verticalMidpoint = (south + north) / 2
        BoundingBox(
            west = west,
            south = verticalMidpoint - (east - west) / 2,
            east = east,
            north = verticalMidpoint + (east - west) / 2,
        )
    } else {
        val horizontalMidpoint = (west + east) / 2
        BoundingBox(
            west = horizontalMidpoint - (north - south) / 2,
            south = south,
            east = horizontalMidpoint + (north - south) / 2,
            north = north,
        )
    }
}
