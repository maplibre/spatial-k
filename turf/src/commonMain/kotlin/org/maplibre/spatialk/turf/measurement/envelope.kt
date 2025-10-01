@file:JvmName("Measurement")
@file:JvmMultifileClass

package org.maplibre.spatialk.turf.measurement

import kotlin.collections.orEmpty
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.GeoJsonObject
import org.maplibre.spatialk.geojson.Geometry
import org.maplibre.spatialk.geojson.GeometryCollection
import org.maplibre.spatialk.geojson.Polygon
import org.maplibre.spatialk.turf.meta.coordAll

/**
 * Takes any [GeoJsonObject] and returns a [Feature] containing a rectangular [Polygon] that
 * encompasses all vertices.
 *
 * @param geoJson input containing any coordinates
 * @return a rectangular [Polygon] feature that encompasses all vertices
 */
public fun envelope(geoJson: GeoJsonObject): Feature<Polygon> {
    val coordinates =
        when (geoJson) {
            is Feature<*> -> geoJson.coordAll()
            is FeatureCollection -> geoJson.coordAll()
            is GeometryCollection -> geoJson.coordAll()
            is Geometry -> geoJson.coordAll()
        }.orEmpty()

    val bbox = geoJson.bbox ?: computeBbox(coordinates)

    return Feature(geometry = bboxPolygon(bbox), bbox = bbox)
}
