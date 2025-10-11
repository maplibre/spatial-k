@file:JvmName("FeatureConversion")
@file:JvmMultifileClass

package org.maplibre.spatialk.turf.featureconversion

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import org.maplibre.spatialk.geojson.GeoJsonObject
import org.maplibre.spatialk.geojson.GeometryCollection
import org.maplibre.spatialk.geojson.MultiPoint
import org.maplibre.spatialk.turf.coordinatemutation.flattenCoordinates

/** @return a [GeometryCollection] with all coordinates of the input object as `Point` features */
public fun GeoJsonObject.explode(): MultiPoint = MultiPoint(flattenCoordinates())
