@file:JvmName("Measurement")
@file:JvmMultifileClass

package org.maplibre.spatialk.turf.measurement

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import org.maplibre.spatialk.geojson.Geometry
import org.maplibre.spatialk.geojson.Polygon

/**
 * Takes a [Geometry] and returns a rectangular [Polygon] that encompasses all vertices.
 *
 * @return a rectangular [Polygon] that encompasses all vertices
 */
public fun Geometry.envelope(): Polygon = (bbox ?: this.computeBbox()).toPolygon()
