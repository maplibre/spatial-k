@file:JvmName("Measurement")
@file:JvmMultifileClass

package org.maplibre.spatialk.turf.measurement

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import org.maplibre.spatialk.geojson.*
import org.maplibre.spatialk.turf.meta.coordAll

/**
 * Takes any [GeoJsonObject] and returns a [Feature] containing a rectangular [Polygon] that
 * encompasses all vertices.
 *
 * @return a rectangular [Polygon] feature that encompasses all vertices
 */
public fun GeoJsonObject.envelope(): Feature<Polygon> {
    val coordinates =
        when (this) {
            is Feature<*> -> this.coordAll()
            is FeatureCollection -> this.coordAll()
            is GeometryCollection -> this.coordAll()
            is Geometry -> this.coordAll()
        }.orEmpty()

    val bbox = bbox ?: computeBbox(coordinates)

    return Feature(geometry = bbox.toPolygon(), bbox = bbox)
}
