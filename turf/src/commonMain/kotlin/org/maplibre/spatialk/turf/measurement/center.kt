@file:JvmName("Measurement")
@file:JvmMultifileClass

package org.maplibre.spatialk.turf.measurement

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.Position

/** @return The [Position] at the center of the bounding box. */
public fun BoundingBox.center(): Position {
    val x = (southwest.longitude + northeast.longitude) / 2
    val y = (southwest.latitude + northeast.latitude) / 2
    return Position(longitude = x, latitude = y)
}
