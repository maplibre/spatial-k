@file:JvmName("TurfMeasurement")

package org.maplibre.spatialk.turf

import kotlin.jvm.JvmName
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmSynthetic
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.GeoJsonObject
import org.maplibre.spatialk.geojson.Geometry
import org.maplibre.spatialk.geojson.GeometryCollection
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.MultiLineString
import org.maplibre.spatialk.geojson.MultiPoint
import org.maplibre.spatialk.geojson.MultiPolygon
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Polygon
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.units.Area
import org.maplibre.spatialk.units.Length
import org.maplibre.spatialk.units.LengthUnit.Geodesy.Radians
import org.maplibre.spatialk.units.times
import org.maplibre.spatialk.units.toLength

/**
 * Takes a [LineString] and returns a [position][Position] at a specified distance along the line.
 *
 * @param line input line
 * @param distance distance along the line
 * @return A position [distance] along the line
 */
@ExperimentalTurfApi
public fun along(line: LineString, distance: Length): Position {
    var travelled = Length.ZERO

    line.coordinates.forEachIndexed { i, coordinate ->
        when {
            distance >= travelled && i == line.coordinates.size - 1 -> {}
            travelled >= distance -> {
                val overshot = distance - travelled
                return if (overshot.isZero) coordinate
                else {
                    val direction = bearing(coordinate, line.coordinates[i - 1]) - 180
                    destination(coordinate, overshot, direction)
                }
            }

            else -> travelled += distance(coordinate, line.coordinates[i + 1])
        }
    }

    return line.coordinates[line.coordinates.size - 1]
}

/**
 * Takes a geometry and returns its area.
 *
 * @param geometry input geometry
 * @return area in square meters
 */
@ExperimentalTurfApi
public fun area(geometry: Geometry): Area {
    return when (geometry) {
        is GeometryCollection ->
            geometry.geometries.fold(Area.ZERO) { acc, geom -> acc + area(geom) }
        else -> calculateArea(geometry)
    }
}

private fun calculateArea(geometry: Geometry): Area {
    return when (geometry) {
        is Polygon -> polygonArea(geometry.coordinates)
        is MultiPolygon ->
            geometry.coordinates.fold(Area.ZERO) { acc, coords -> acc + polygonArea(coords) }
        else -> Area.ZERO
    }
}

private fun polygonArea(coordinates: List<List<Position>>): Area {
    var total = Area.ZERO
    if (coordinates.isNotEmpty()) {
        total += ringArea(coordinates[0]).absoluteValue
        for (i in 1 until coordinates.size) {
            total -= ringArea(coordinates[i]).absoluteValue
        }
    }
    return total
}

/**
 * Calculates the approximate area of the [polygon][coordinates] were it projected onto the earth.
 * Note that this area will be positive if the ring is oriented clockwise, otherwise it will be
 * negative.
 *
 * Reference: Robert. G. Chamberlain and William H. Duquette, "Some Algorithms for Polygons on a
 * Sphere", JPL Publication 07-03, Jet Propulsion Laboratory, Pasadena, CA, June 2007
 * https://trs.jpl.nasa.gov/handle/2014/40409
 */
private fun ringArea(coordinates: List<Position>): Area {
    var p1: Position
    var p2: Position
    var p3: Position
    var lowerIndex: Int
    var middleIndex: Int
    var upperIndex: Int
    var total = 0.0

    if (coordinates.size > 2) {
        for (i in coordinates.indices) {
            when (i) {
                coordinates.size - 2 -> {
                    lowerIndex = coordinates.size - 2
                    middleIndex = coordinates.size - 1
                    upperIndex = 0
                }

                coordinates.size - 1 -> {
                    lowerIndex = coordinates.size - 1
                    middleIndex = 0
                    upperIndex = 1
                }

                else -> {
                    lowerIndex = i
                    middleIndex = i + 1
                    upperIndex = i + 2
                }
            }
            p1 = coordinates[lowerIndex]
            p2 = coordinates[middleIndex]
            p3 = coordinates[upperIndex]
            total += (radians(p3.longitude) - radians(p1.longitude)) * sin(radians(p2.latitude))
        }
        return (total * EARTH_EQUATOR_RADIUS * EARTH_EQUATOR_RADIUS / 2)
    }
    return Area.ZERO
}

/**
 * Takes a geometry and calculates the bounding box of all input features.
 *
 * @param geometry The geometry to compute a bounding box for.
 * @return A [BoundingBox] that covers the geometry.
 */
@ExperimentalTurfApi
public fun bbox(geometry: Geometry): BoundingBox = computeBbox(geometry.coordAll())

/**
 * Takes a geometry and calculates the bounding box of all input features.
 *
 * @param geometry The geometry to compute a bounding box for.
 * @return A [BoundingBox] that covers the geometry.
 */
@ExperimentalTurfApi
public fun bbox(geometry: Point): BoundingBox = computeBbox(geometry.coordAll())

/**
 * Takes a geometry and calculates the bounding box of all input features.
 *
 * @param geometry The geometry to compute a bounding box for.
 * @return A [BoundingBox] that covers the geometry.
 */
@ExperimentalTurfApi
public fun bbox(geometry: MultiPoint): BoundingBox = computeBbox(geometry.coordAll())

/**
 * Takes a geometry and calculates the bounding box of all input features.
 *
 * @param geometry The geometry to compute a bounding box for.
 * @return A [BoundingBox] that covers the geometry.
 */
@ExperimentalTurfApi
public fun bbox(geometry: LineString): BoundingBox = computeBbox(geometry.coordAll())

/**
 * Takes a geometry and calculates the bounding box of all input features.
 *
 * @param geometry The geometry to compute a bounding box for.
 * @return A [BoundingBox] that covers the geometry.
 */
@ExperimentalTurfApi
public fun bbox(geometry: MultiLineString): BoundingBox = computeBbox(geometry.coordAll())

/**
 * Takes a geometry and calculates the bounding box of all input features.
 *
 * @param geometry The geometry to compute a bounding box for.
 * @return A [BoundingBox] that covers the geometry.
 */
@ExperimentalTurfApi
public fun bbox(geometry: Polygon): BoundingBox = computeBbox(geometry.coordAll())

/**
 * Takes a geometry and calculates the bounding box of all input features.
 *
 * @param geometry The geometry to compute a bounding box for.
 * @return A [BoundingBox] that covers the geometry.
 */
@ExperimentalTurfApi
public fun bbox(geometry: MultiPolygon): BoundingBox = computeBbox(geometry.coordAll())

/**
 * Takes a feature and calculates the bounding box of the feature's geometry.
 *
 * @param feature The feature to compute a bounding box for.
 * @return A [BoundingBox] that covers the geometry.
 */
@ExperimentalTurfApi
public fun bbox(feature: Feature): BoundingBox = computeBbox(feature.coordAll() ?: emptyList())

/**
 * Takes a feature collection and calculates a bounding box that covers all features in the
 * collection.
 *
 * @param featureCollection The collection of features to compute a bounding box for.
 * @return A [BoundingBox] that covers the geometry.
 */
@ExperimentalTurfApi
public fun bbox(featureCollection: FeatureCollection): BoundingBox =
    computeBbox(featureCollection.coordAll())

public fun computeBbox(coordinates: List<Position>): BoundingBox {
    val result =
        doubleArrayOf(
            Double.POSITIVE_INFINITY,
            Double.POSITIVE_INFINITY,
            Double.NEGATIVE_INFINITY,
            Double.NEGATIVE_INFINITY,
        )
    coordinates.forEach { (longitude, latitude) ->
        if (result[0] > longitude) {
            result[0] = longitude
        }
        if (result[1] > latitude) {
            result[1] = latitude
        }
        if (result[2] < longitude) {
            result[2] = longitude
        }
        if (result[3] < latitude) {
            result[3] = latitude
        }
    }

    return BoundingBox(result)
}

/**
 * Takes a bounding box and returns an equivalent [Polygon].
 *
 * @param bbox The bounding box to convert to a Polygon.
 * @return The bounding box as a polygon
 * @see BoundingBox.toPolygon
 */
@ExperimentalTurfApi
public fun bboxPolygon(bbox: BoundingBox): Polygon {
    require(bbox.northeast.altitude == null && bbox.southwest.altitude == null) {
        "Bounding Box cannot have altitudes"
    }

    return Polygon(
        listOf(
            bbox.southwest,
            Position(bbox.northeast.longitude, bbox.southwest.latitude),
            bbox.northeast,
            Position(bbox.southwest.longitude, bbox.northeast.latitude),
            bbox.southwest,
        )
    )
}

@JvmSynthetic @ExperimentalTurfApi public fun BoundingBox.toPolygon(): Polygon = bboxPolygon(this)

/**
 * Takes two positions ([start], [end]) and finds the geographic bearing between them, i.e., the
 * angle measured in degrees from the north line (0 degrees)
 *
 * @param start starting point
 * @param end ending point
 * @param final calculates the final bearing if true
 * @return bearing in decimal degrees, between -180 and 180 degrees (positive clockwise)
 */
@JvmOverloads
@ExperimentalTurfApi
public fun bearing(start: Position, end: Position, final: Boolean = false): Double {
    if (final) return finalBearing(start, end)

    val lon1 = radians(start.longitude)
    val lon2 = radians(end.longitude)
    val lat1 = radians(start.latitude)
    val lat2 = radians(end.latitude)

    val a = sin(lon2 - lon1) * cos(lat2)
    val b = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(lon2 - lon1)

    return degrees(atan2(a, b))
}

@ExperimentalTurfApi
internal fun finalBearing(start: Position, end: Position): Double =
    (bearing(end, start) + 180) % 360

/**
 * Takes an [origin] [Position] and calculates the location of a destination position given a
 * distance [Length] and bearing in degrees. This uses the Haversine formula to account for global
 * curvature.
 *
 * @param origin starting point
 * @param distance distance from the origin point
 * @param bearing ranging from -180 to 180
 * @return destination position
 * @see <a href="https://en.wikipedia.org/wiki/Haversine_formula">Haversine formula</a>
 */
@ExperimentalTurfApi
public fun destination(origin: Position, distance: Length, bearing: Double): Position {
    val longitude1 = radians(origin.longitude)
    val latitude1 = radians(origin.latitude)
    val bearingRad = radians(bearing)
    val radians = distance.toDouble(Radians)

    val latitude2 =
        asin(sin(latitude1) * cos(radians) + cos(latitude1) * sin(radians) * cos(bearingRad))
    val longitude2 =
        longitude1 +
            atan2(
                sin(bearingRad) * sin(radians) * cos(latitude1),
                cos(radians) - sin(latitude1) * sin(latitude2),
            )

    return Position(degrees(longitude2), degrees(latitude2))
}

/**
 * Calculates the distance between two positions. This uses the Haversine formula to account for
 * global curvature.
 *
 * @param from origin point
 * @param to destination point
 * @return distance between the two points
 * @see <a href="https://en.wikipedia.org/wiki/Haversine_formula">Haversine formula</a>
 */
@ExperimentalTurfApi
public fun distance(from: Position, to: Position): Length {
    val dLat = radians(to.latitude - from.latitude)
    val dLon = radians(to.longitude - from.longitude)
    val lat1 = radians(from.latitude)
    val lat2 = radians(to.latitude)

    val a = sin(dLat / 2).pow(2) + sin(dLon / 2).pow(2) * cos(lat1) * cos(lat2)
    return 2 * atan2(sqrt(a), sqrt(1 - a)).toLength(Radians)
}

/**
 * Calculates the length of the given [LineString].
 *
 * @param lineString The geometry to measure
 * @return The length of the geometry
 */
@ExperimentalTurfApi
public fun length(lineString: LineString): Length = length(lineString.coordinates)

/**
 * Calculates the combined length of all [LineString]s from the given [MultiLineString].
 *
 * @param multiLineString The geometry to measure
 * @return The length of the geometry
 */
@ExperimentalTurfApi
public fun length(multiLineString: MultiLineString): Length =
    multiLineString.coordinates.fold(Length.ZERO) { acc, coords -> acc + length(coords) }

/**
 * Calculates the length of perimeter the given [Polygon]. Any holes in the polygon will be included
 * in the length.
 *
 * @param polygon The geometry to measure
 * @return The length of the geometry
 */
@ExperimentalTurfApi
public fun length(polygon: Polygon): Length =
    polygon.coordinates.fold(Length.ZERO) { acc, ring -> acc + length(ring) }

/**
 * Calculates the combined length of perimeter the [Polygon]s in the [MultiPolygon]. Any holes in
 * the polygons will be included in the length.
 *
 * @param multiPolygon The geometry to measure
 * @return The length of the geometry
 */
@ExperimentalTurfApi
public fun length(multiPolygon: MultiPolygon): Length =
    multiPolygon.coordinates.fold(Length.ZERO) { total, polygon ->
        total + polygon.fold(Length.ZERO) { acc, ring -> acc + length(ring) }
    }

@ExperimentalTurfApi
private fun length(coords: List<Position>): Length {
    var travelled = Length.ZERO
    var prevCoords = coords[0]
    for (i in 1 until coords.size) {
        travelled += distance(prevCoords, coords[i])
        prevCoords = coords[i]
    }
    return travelled
}

/**
 * Takes two [Position]s and returns a point midway between them. The midpoint is calculated
 * geodesically, meaning the curvature of the earth is taken into account.
 *
 * @param point1 the first point
 * @param point2 the second point
 * @return A [Position] midway between [point1] and [point2]
 */
@ExperimentalTurfApi
public fun midpoint(point1: Position, point2: Position): Position {
    val dist = distance(point1, point2)
    val heading = bearing(point1, point2)

    return destination(point1, dist / 2, heading)
}

/**
 * Takes any kind of [Feature] and returns the center point. It will create a [BoundingBox] around
 * the given [Feature] and calculates the center point of it.
 *
 * @param feature the feature to find the center for
 * @return A [Point] holding the center coordinates
 */
@ExperimentalTurfApi
public fun center(feature: Feature): Point {
    val ext = bbox(feature)
    val x = (ext.southwest.longitude + ext.northeast.longitude) / 2
    val y = (ext.southwest.latitude + ext.northeast.latitude) / 2
    return Point(Position(longitude = x, latitude = y))
}

/**
 * It overloads the `center(feature: Feature)` method.
 *
 * @param geometry the [Geometry] to find the center for
 */
@ExperimentalTurfApi
public fun center(geometry: Geometry): Point {
    return center(Feature(geometry = geometry))
}

/**
 * Calculate great circles routes as [LineString]. Raises error when [start] and [end] are
 * antipodes.
 *
 * @param start source position
 * @param end destination position
 * @param pointCount number of positions on the arc (including [start] and [end])
 * @param antimeridianOffset from antimeridian in degrees (default long. = +/- 10deg, geometries
 *   within 170deg to -170deg will be split)
 * @throws IllegalArgumentException if [start] and [end] are diametrically opposite.
 */
@ExperimentalTurfApi
public fun greatCircle(
    start: Position,
    end: Position,
    pointCount: Int = 100,
    antimeridianOffset: Double = 10.0,
): Geometry {

    val deltaLongitude = start.longitude - end.longitude
    val deltaLatitude = start.latitude - end.latitude

    // check antipodal positions
    require(abs(deltaLatitude) != 0.0 && abs(deltaLongitude % 360) - ANTIMERIDIAN_POS != 0.0) {
        "Input $start and $end are diametrically opposite, thus there is no single route but rather infinite"
    }

    val distance = distance(start, end).toDouble(Radians)

    /**
     * Calculates the intermediate point on a great circle line
     * http://www.edwilliams.org/avform.htm#Intermediate
     */
    fun intermediateCoordinate(fraction: Double): Position {
        val lon1 = radians(start.longitude)
        val lon2 = radians(end.longitude)
        val lat1 = radians(start.latitude)
        val lat2 = radians(end.latitude)

        val a = sin((1 - fraction) * distance) / sin(distance)
        val b = sin(fraction * distance) / sin(distance)
        val x = a * cos(lat1) * cos(lon1) + b * cos(lat2) * cos(lon2)
        val y = a * cos(lat1) * sin(lon1) + b * cos(lat2) * sin(lon2)
        val z = a * sin(lat1) + b * sin(lat2)

        val lat = degrees(atan2(z, sqrt(x.pow(2) + y.pow(2))))
        val lon = degrees(atan2(y, x))
        return Position(lon, lat)
    }

    @Suppress("LongMethod")
    fun createCoordinatesAntimeridianAttended(
        plainArc: List<Position>,
        antimeridianOffset: Double,
    ): List<List<Position>> {
        val borderEast = ANTIMERIDIAN_POS - antimeridianOffset
        val borderWest = ANTIMERIDIAN_NEG + antimeridianOffset

        @Suppress("MagicNumber") val diffSpace = 360.0 - antimeridianOffset

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
                        lon1 in (ANTIMERIDIAN_NEG + 1..<borderWest) &&
                            lon2 == ANTIMERIDIAN_POS &&
                            k + 1 < plainArc.size
                    ) {
                        poNewLS.add(Position(ANTIMERIDIAN_NEG, currentPosition.latitude))
                        poNewLS.add(Position(plainArc[k + 1].longitude, plainArc[k + 1].latitude))
                        return@forEachIndexed
                    } else if (
                        lon1 > borderEast &&
                            lon1 < ANTIMERIDIAN_POS &&
                            lon2 == ANTIMERIDIAN_POS &&
                            k + 1 < plainArc.size
                    ) {
                        poNewLS.add(Position(ANTIMERIDIAN_POS, currentPosition.latitude))
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
                        @Suppress("MagicNumber")
                        lon2 += 360.0
                    }

                    if (ANTIMERIDIAN_POS in lon1..lon2 && lon1 < lon2) {
                        val ratio = (ANTIMERIDIAN_POS - lon1) / (lon2 - lon1)
                        val lat = ratio * lat2 + (1 - ratio) * lat1
                        poNewLS.add(
                            if (previousPosition.longitude > borderEast)
                                Position(ANTIMERIDIAN_POS, lat)
                            else Position(ANTIMERIDIAN_NEG, lat)
                        )
                        poMulti.add(poNewLS.toList())
                        poNewLS =
                            mutableListOf() // Clear poNewLS instead of replacing it with an empty
                        // list
                        poNewLS.add(
                            if (previousPosition.longitude > borderEast)
                                Position(ANTIMERIDIAN_NEG, lat)
                            else Position(ANTIMERIDIAN_POS, lat)
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
        add(start)
        (1 until (pointCount - 1)).forEach { i ->
            add(intermediateCoordinate((i + 1).toDouble() / (pointCount - 2 + 1)))
        }
        add(end)
    }

    val coordinates = createCoordinatesAntimeridianAttended(arc, antimeridianOffset)
    return if (coordinates.size == 1) {
        LineString(coordinates = coordinates[0], bbox = computeBbox(coordinates[0]))
    } else {
        MultiLineString(coordinates = coordinates, bbox = computeBbox(coordinates.flatten()))
    }
}

/**
 * Takes any [GeoJsonObject] and returns a [Feature] containing a rectangular [Polygon] that
 * encompasses all vertices.
 *
 * @param geoJson input containing any coordinates
 * @return a rectangular [Polygon] feature that encompasses all vertices
 */
@ExperimentalTurfApi
public fun envelope(geoJson: GeoJsonObject): Feature {
    val coordinates =
        when (geoJson) {
            is Feature -> geoJson.coordAll()
            is FeatureCollection -> geoJson.coordAll()
            is GeometryCollection -> geoJson.coordAll()
            is Geometry -> geoJson.coordAll()
        }.orEmpty()

    val bbox = geoJson.bbox ?: computeBbox(coordinates)

    return Feature(geometry = bboxPolygon(bbox), bbox = bbox)
}

/**
 * Calculates the distance between a given point and the nearest point on a line.
 *
 * @param point point to calculate from
 * @param line line to calculate to
 */
@ExperimentalTurfApi
public fun pointToLineDistance(point: Position, line: LineString): Length {
    var distance = Length.MAX_VALUE

    line.coordinates
        .drop(1)
        .mapIndexed { idx, position -> line.coordinates[idx] to position }
        .forEach { (prev, cur) ->
            val d = distanceToSegment(point, prev, cur)
            if (d < distance) distance = d
        }

    return distance
}

@OptIn(ExperimentalTurfApi::class)
private fun distanceToSegment(point: Position, start: Position, end: Position): Length {
    fun dot(u: Position, v: Position): Double {
        return u.longitude * v.longitude + u.latitude * v.latitude
    }

    val segmentVector = Position(end.longitude - start.longitude, end.latitude - start.latitude)
    val pointVector = Position(point.longitude - start.longitude, point.latitude - start.latitude)

    val projectionLengthSquared = dot(pointVector, segmentVector)
    if (projectionLengthSquared <= 0) {
        return rhumbDistance(point, start)
    }
    val segmentLengthSquared = dot(segmentVector, segmentVector)
    if (segmentLengthSquared <= projectionLengthSquared) {
        return rhumbDistance(point, end)
    }

    val projectionRatio = projectionLengthSquared / segmentLengthSquared
    val projectedPoint =
        Position(
            start.longitude + projectionRatio * segmentVector.longitude,
            start.latitude + projectionRatio * segmentVector.latitude,
        )

    return rhumbDistance(point, projectedPoint)
}

/** Calculates the distance along a rhumb line between two points. */
@OptIn(ExperimentalTurfApi::class)
public fun rhumbDistance(origin: Position, destination: Position): Length {
    // compensate the crossing of the 180th meridian
    val destination =
        Position(
            destination.longitude +
                when {
                    destination.longitude - origin.longitude > 180 -> -360
                    origin.longitude - destination.longitude > 180 -> 360
                    else -> 0
                },
            destination.latitude,
        )

    val phi1 = origin.latitude * PI / 180
    val phi2 = destination.latitude * PI / 180
    val deltaPhi = phi2 - phi1
    var deltaLambda = abs(destination.longitude - origin.longitude) * PI / 180

    if (deltaLambda > PI) {
        deltaLambda -= 2 * PI
    }

    val deltaPsi = ln(tan(phi2 / 2 + PI / 4) / tan(phi1 / 2 + PI / 4))
    val q = if (abs(deltaPsi) > 10e-12) deltaPhi / deltaPsi else cos(phi1)

    val delta = sqrt(deltaPhi * deltaPhi + q * q * deltaLambda * deltaLambda)
    val dist = delta.toLength(Radians)

    return dist
}
