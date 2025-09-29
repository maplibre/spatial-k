package org.maplibre.spatialk.turf.measurement

import kotlin.jvm.JvmSynthetic
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.Polygon
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.turf.ExperimentalTurfApi

/**
 * Takes a bounding box and returns an equivalent [Polygon].
 *
 * @param bbox The bounding box to convert to a Polygon.
 * @return The bounding box as a polygon
 * @see BoundingBox.toPolygon
 */
@ExperimentalTurfApi
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

@JvmSynthetic @ExperimentalTurfApi public fun BoundingBox.toPolygon(): Polygon = bboxPolygon(this)
