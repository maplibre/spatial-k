@file:JvmName("FeatureConversion")
@file:JvmMultifileClass

package org.maplibre.spatialk.turf.featureconversion

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import org.maplibre.spatialk.geojson.GeoJsonObject
import org.maplibre.spatialk.geojson.MultiPoint
import org.maplibre.spatialk.turf.coordinatemutation.flattenCoordinates

/**
 * Explodes every coordinate of the input into a [MultiPoint].
 *
 * Rebuilds a new geometry from coordinates. The result does not keep [bbox] or
 * [GeoJsonObject.foreignMembers].
 *
 * @return a [MultiPoint] with all coordinates of the input object
 */
public fun GeoJsonObject.explode(): MultiPoint = MultiPoint(flattenCoordinates())
