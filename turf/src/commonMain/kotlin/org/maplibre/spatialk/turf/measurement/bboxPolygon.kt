@file:JvmName("Measurement")
@file:JvmMultifileClass

package org.maplibre.spatialk.turf.measurement

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.Polygon
import org.maplibre.spatialk.geojson.Position

/**
 * Takes a bounding box and returns an equivalent [Polygon].
 *
 * @return The bounding box as a polygon
 */
public fun BoundingBox.toPolygon(): Polygon {
    require(northeast.altitude == null && southwest.altitude == null) {
        "Bounding Box cannot have altitudes"
    }

    return Polygon(
        listOf(
            southwest,
            Position(northeast.longitude, southwest.latitude),
            northeast,
            Position(southwest.longitude, northeast.latitude),
            southwest,
        )
    )
}
