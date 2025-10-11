@file:JvmName("FeatureConversion")
@file:JvmMultifileClass

package org.maplibre.spatialk.turf.featureconversion

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import org.maplibre.spatialk.geojson.GeoJsonObject
import org.maplibre.spatialk.geojson.GeometryCollection
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.turf.meta.flattenCoordinates

/** @return a [GeometryCollection] with all coordinates of the input object as `Point` features */
public fun GeoJsonObject.explode(): GeometryCollection<Point> =
    GeometryCollection(flattenCoordinates().map { Point(it) })
