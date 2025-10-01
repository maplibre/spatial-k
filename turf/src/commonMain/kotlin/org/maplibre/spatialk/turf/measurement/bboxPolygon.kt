@file:JvmName("Measurement")
@file:JvmMultifileClass

package org.maplibre.spatialk.turf.measurement

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.Polygon
import org.maplibre.spatialk.geojson.Position

/**
 * Takes a bounding box and returns an equivalent [Polygon].
 *
 * @param bbox The bounding box to convert to a Polygon.
 * @return The bounding box as a polygon
 * @see BoundingBox.toPolygon
 */
public fun bboxPolygon(bbox: BoundingBox): Polygon {
    require(bbox.northeast.altitude == null && bbox.southwest.altitude == null) {
        "Bounding Box cannot have altitudes"
    }

    return Polygon(
        listOf(
            bbox.southwest,
            Position(bbox.northeast.longitude, bbox.southwest.latitude),
            bbox.northeast,
            Position(bbox.southwest.longitude, bbox.northeast.latitude),
            bbox.southwest,
        )
    )
}

@JvmSynthetic public fun BoundingBox.toPolygon(): Polygon = bboxPolygon(this)
