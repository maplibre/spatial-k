@file:JvmName("FeatureConversion")
@file:JvmMultifileClass

package org.maplibre.spatialk.turf.featureconversion

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.GeoJsonObject
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.dsl.featureCollection
import org.maplibre.spatialk.turf.meta.flattenCoordinates

/** @return a [FeatureCollection] with all coordinates of the input object as `Point` features */
public fun GeoJsonObject.explode(): FeatureCollection<Point> = featureCollection {
    flattenCoordinates().forEach { feature(Point(it)) }
}
