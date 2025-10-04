@file:JvmName("Measurement")
@file:JvmMultifileClass

package org.maplibre.spatialk.turf.measurement

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.GeoJsonObject
import org.maplibre.spatialk.geojson.Polygon

/**
 * Takes any [GeoJsonObject] and returns a [Feature] containing a rectangular [Polygon] that
 * encompasses all vertices.
 *
 * @return a rectangular [Polygon] feature that encompasses all vertices, or `null` if the input has
 *   no geometry.
 */
public fun GeoJsonObject.envelope(): Feature<Polygon>? {
    val bbox = bbox ?: this.computeBbox() ?: return null
    return Feature(geometry = bbox.toPolygon(), bbox = bbox)
}
