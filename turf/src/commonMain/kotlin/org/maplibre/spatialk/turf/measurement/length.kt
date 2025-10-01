@file:JvmName("Measurement")
@file:JvmMultifileClass

package org.maplibre.spatialk.turf.measurement

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.MultiLineString
import org.maplibre.spatialk.geojson.MultiPolygon
import org.maplibre.spatialk.geojson.Polygon
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.units.Length

/**
 * Calculates the length of the given [LineString].
 *
 * @param lineString The geometry to measure
 * @return The length of the geometry
 */
public fun length(lineString: LineString): Length = length(lineString.coordinates)

/**
 * Calculates the combined length of all [LineString]s from the given [MultiLineString].
 *
 * @param multiLineString The geometry to measure
 * @return The length of the geometry
 */
public fun length(multiLineString: MultiLineString): Length =
    multiLineString.coordinates.fold(Length.ZERO) { acc, coords -> acc + length(coords) }

/**
 * Calculates the length of perimeter the given [Polygon]. Any holes in the polygon will be included
 * in the length.
 *
 * @param polygon The geometry to measure
 * @return The length of the geometry
 */
public fun length(polygon: Polygon): Length =
    polygon.coordinates.fold(Length.ZERO) { acc, ring -> acc + length(ring) }

/**
 * Calculates the combined length of perimeter the [Polygon]s in the [MultiPolygon]. Any holes in
 * the polygons will be included in the length.
 *
 * @param multiPolygon The geometry to measure
 * @return The length of the geometry
 */
public fun length(multiPolygon: MultiPolygon): Length =
    multiPolygon.coordinates.fold(Length.ZERO) { total, polygon ->
        total + polygon.fold(Length.ZERO) { acc, ring -> acc + length(ring) }
    }

private fun length(coords: List<Position>): Length {
    var travelled = Length.ZERO
    var prevCoords = coords[0]
    for (i in 1 until coords.size) {
        travelled += distance(prevCoords, coords[i])
        prevCoords = coords[i]
    }
    return travelled
}
