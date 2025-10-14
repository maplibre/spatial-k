@file:JvmName("Measurement")
@file:JvmMultifileClass

package org.maplibre.spatialk.turf.measurement

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.math.*
import org.maplibre.spatialk.geojson.Geometry
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.MultiLineString
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.units.extensions.degrees
import org.maplibre.spatialk.units.extensions.inDegrees
import org.maplibre.spatialk.units.extensions.inEarthRadians
import org.maplibre.spatialk.units.extensions.inRadians
import org.maplibre.spatialk.units.extensions.radians

private const val PositiveAntimeridian = 180.0

private const val NegativeAntimeridian = -180.0

/**
 * Calculate great circles routes as [LineString]. Raises error when [from] and [to] are antipodes.
 *
 * @param from source position
 * @param to destination position
 * @param pointCount number of positions on the arc (including [from] and [to])
 * @param antimeridianOffset from antimeridian in degrees (default long. = +/- 10deg, geometries
 *   within 170deg to -170deg will be split)
 * @throws IllegalArgumentException if [from] and [to] are diametrically opposite.
 */
public fun greatCircle(
    from: Position,
    to: Position,
    pointCount: Int = 100,
    antimeridianOffset: Double = 10.0,
): Geometry {

    val deltaLongitude = from.longitude - to.longitude
    val deltaLatitude = from.latitude - to.latitude

    // check antipodal positions
    require(abs(deltaLatitude) != 0.0 && abs(deltaLongitude % 360) - PositiveAntimeridian != 0.0) {
        "Input $from and $to are diametrically opposite, thus there is no single route but rather infinite"
    }

    val distance = distance(from, to).inEarthRadians

    /**
     * Calculates the intermediate point on a great circle line
     * http://www.edwilliams.org/avform.htm#Intermediate
     */
    fun intermediateCoordinate(fraction: Double): Position {
        val lon1 = from.longitude.degrees.inRadians
        val lon2 = to.longitude.degrees.inRadians
        val lat1 = from.latitude.degrees.inRadians
        val lat2 = to.latitude.degrees.inRadians

        val a = sin((1 - fraction) * distance) / sin(distance)
        val b = sin(fraction * distance) / sin(distance)
        val x = a * cos(lat1) * cos(lon1) + b * cos(lat2) * cos(lon2)
        val y = a * cos(lat1) * sin(lon1) + b * cos(lat2) * sin(lon2)
        val z = a * sin(lat1) + b * sin(lat2)

        val lat = atan2(z, sqrt(x.pow(2) + y.pow(2))).radians.inDegrees
        val lon = atan2(y, x).radians.inDegrees
        return Position(lon, lat)
    }

    fun createCoordinatesAntimeridianAttended(
        plainArc: List<Position>,
        antimeridianOffset: Double,
    ): List<List<Position>> {
        val borderEast = PositiveAntimeridian - antimeridianOffset
        val borderWest = NegativeAntimeridian + antimeridianOffset

        val diffSpace = 360.0 - antimeridianOffset

        val passesAntimeridian =
            plainArc
                .zipWithNext { a, b ->
                    val diff = abs(a.longitude - b.longitude)
                    (diff > diffSpace &&
                        ((a.longitude > borderEast && b.longitude < borderWest) ||
                            (b.longitude > borderEast && a.longitude < borderWest)))
                }
                .any()

        val maxSmallDiffLong =
            plainArc
                .zipWithNext { a, b -> abs(a.longitude - b.longitude) }
                .filter { it <= diffSpace } // Filter differences less than or equal to diffSpace
                .maxByOrNull { it } ?: 0.0

        val poMulti = mutableListOf<List<Position>>()
        if (passesAntimeridian && maxSmallDiffLong < antimeridianOffset) {
            var poNewLS = mutableListOf<Position>()
            plainArc.forEachIndexed { k, currentPosition ->
                if (
                    k > 0 && abs(currentPosition.longitude - plainArc[k - 1].longitude) > diffSpace
                ) {
                    val previousPosition = plainArc[k - 1]
                    var lon1 = previousPosition.longitude
                    var lat1 = previousPosition.latitude
                    var lon2 = currentPosition.longitude
                    var lat2 = currentPosition.latitude

                    @Suppress("ComplexCondition")
                    if (
                        lon1 in (NegativeAntimeridian + 1..<borderWest) &&
                            lon2 == PositiveAntimeridian &&
                            k + 1 < plainArc.size
                    ) {
                        poNewLS.add(Position(NegativeAntimeridian, currentPosition.latitude))
                        poNewLS.add(Position(plainArc[k + 1].longitude, plainArc[k + 1].latitude))
                        return@forEachIndexed
                    } else if (
                        lon1 > borderEast &&
                            lon1 < PositiveAntimeridian &&
                            lon2 == PositiveAntimeridian &&
                            k + 1 < plainArc.size
                    ) {
                        poNewLS.add(Position(PositiveAntimeridian, currentPosition.latitude))
                        poNewLS.add(Position(plainArc[k + 1].longitude, plainArc[k + 1].latitude))
                        return@forEachIndexed
                    }

                    if (lon1 < borderWest && lon2 > borderEast) {
                        val tmpX = lon1
                        lon1 = lon2
                        lon2 = tmpX
                        val tmpY = lat1
                        lat1 = lat2
                        lat2 = tmpY
                    }
                    if (lon1 > borderEast && lon2 < borderWest) {
                        lon2 += 360.0
                    }

                    if (PositiveAntimeridian in lon1..lon2 && lon1 < lon2) {
                        val ratio = (PositiveAntimeridian - lon1) / (lon2 - lon1)
                        val lat = ratio * lat2 + (1 - ratio) * lat1
                        poNewLS.add(
                            if (previousPosition.longitude > borderEast)
                                Position(PositiveAntimeridian, lat)
                            else Position(NegativeAntimeridian, lat)
                        )
                        poMulti.add(poNewLS.toList())
                        poNewLS =
                            mutableListOf() // Clear poNewLS instead of replacing it with an empty
                        // list
                        poNewLS.add(
                            if (previousPosition.longitude > borderEast)
                                Position(NegativeAntimeridian, lat)
                            else Position(PositiveAntimeridian, lat)
                        )
                    } else {
                        poNewLS =
                            mutableListOf() // Clear poNewLS instead of replacing it with an empty
                        // list
                        poMulti.add(poNewLS.toList())
                    }
                }
                poNewLS.add(currentPosition) // Adding current position to poNewLS
            }
            poMulti.add(poNewLS.toList()) // Adding the last remaining poNewLS to poMulti
        } else {
            poMulti.add(plainArc)
        }
        return poMulti
    }

    val arc = buildList {
        add(from)
        (1 until (pointCount - 1)).forEach { i ->
            add(intermediateCoordinate((i + 1).toDouble() / (pointCount - 2 + 1)))
        }
        add(to)
    }

    val coordinates = createCoordinatesAntimeridianAttended(arc, antimeridianOffset)
    return if (coordinates.size == 1) {
        LineString(coordinates = coordinates[0], bbox = computeBbox(coordinates[0]))
    } else {
        MultiLineString(coordinates = coordinates, bbox = computeBbox(coordinates.flatten()))
    }
}

/**
 * Calculate great circles routes as [LineString]. Raises error when [from] and [to] are antipodes.
 *
 * @param from source point
 * @param to destination point
 * @param pointCount number of positions on the arc (including [from] and [to])
 * @param antimeridianOffset from antimeridian in degrees (default long. = +/- 10deg, geometries
 *   within 170deg to -170deg will be split)
 * @throws IllegalArgumentException if [from] and [to] are diametrically opposite.
 */
public fun greatCircle(
    from: Point,
    to: Point,
    pointCount: Int = 100,
    antimeridianOffset: Double = 10.0,
): Geometry = greatCircle(from.coordinates, to.coordinates, pointCount, antimeridianOffset)
