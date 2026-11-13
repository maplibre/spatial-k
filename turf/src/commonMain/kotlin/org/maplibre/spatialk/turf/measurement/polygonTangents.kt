@file:JvmName("Measurement")
@file:JvmMultifileClass

package org.maplibre.spatialk.turf.measurement

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import org.maplibre.spatialk.geojson.MultiPolygon
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Polygon
import org.maplibre.spatialk.geojson.PolygonGeometry
import org.maplibre.spatialk.geojson.Position

/**
 * Finds the tangents of a [Polygon] or [MultiPolygon] from a [Point].
 *
 * @param point to calculate the tangent points from
 * @param polygon to get tangents from
 * @return list containing the two tangent points
 */
public fun polygonTangents(point: Point, polygon: PolygonGeometry): List<Point> =
    polygonTangents(point.coordinates, polygon)

/**
 * Finds the tangents of a [Polygon] or [MultiPolygon] from a [Position].
 *
 * @param position to calculate the tangent points from
 * @param polygon to get tangents from
 * @return list containing the two tangent points
 */
public fun polygonTangents(position: Position, polygon: PolygonGeometry): List<Point> {
    val bbox = polygon.bbox ?: polygon.computeBbox()

    fun nearestPtIndex(coordinates: List<Position>): Int =
        if (position.inBBox(bbox)) {
            nearestPointIndex(position, coordinates)
        } else {
            0
        }

    fun processRings(
        rings: List<List<Position>>,
        initialRtan: Position,
        initialLtan: Position,
    ): Pair<Position, Position> {
        val firstRing = rings.first()
        // eprev is intentionally computed once from the first ring and reused for every ring,
        // matching GeoKJSON and Turf.js exactly (both compute it once and pass it to each
        // processPolygon call). Do not "fix" this to thread the edge state across rings — that
        // would deviate from the reference implementations.
        val eprev = isLeft(firstRing.first(), firstRing.last(), position)
        return rings.fold(initialRtan to initialLtan) { (rtan, ltan), ring ->
            processPolygon(ring, position, eprev, rtan, ltan)
        }
    }

    return when (polygon) {
        is Polygon -> {
            val rings = polygon.coordinates
            val outerRing = rings.first()
            // The nearest index is computed against the outer ring only: the initial tangent is
            // seeded from the outer ring, and computing the index over all rings (including holes)
            // could exceed the outer ring's bounds and throw (GeoKJSON/Turf.js have the same
            // latent indexing bug, but in JS it yields garbage instead of throwing).
            val initialRightTan = outerRing[nearestPtIndex(outerRing)]
            val initialLeftTan =
                if (initialRightTan.latitude < position.latitude) initialRightTan
                else outerRing.first()

            val (finalRightTan, finalLeftTan) = processRings(rings, initialRightTan, initialLeftTan)
            listOf(Point(finalRightTan), Point(finalLeftTan))
        }

        is MultiPolygon -> {
            val flattened = polygon.coordinates.flatten().flatten()
            val initialRightTan = flattened[nearestPtIndex(flattened)]
            val firstPosition = polygon.coordinates.first().first().first()

            val initialLeftTan =
                if (initialRightTan.latitude < position.latitude) initialRightTan else firstPosition

            val (finalRightTan, finalLeftTan) =
                polygon.coordinates.fold(initialRightTan to initialLeftTan) {
                    (rightTan, leftTan),
                    rings ->
                    processRings(rings, rightTan, leftTan)
                }
            listOf(Point(finalRightTan), Point(finalLeftTan))
        }
    }
}

private fun processPolygon(
    polygonCoords: List<Position>,
    ptCoords: Position,
    eprev: Double,
    rtan: Position,
    ltan: Position,
): Pair<Position, Position> {
    var prevE = eprev
    var currentRtan = rtan
    var currentLtan = ltan
    polygonCoords.forEachIndexed { index, currentPosition ->
        val nextPosition = polygonCoords[(index + 1) % polygonCoords.size]
        val enext = isLeft(currentPosition, nextPosition, ptCoords)

        if (prevE <= 0 && enext > 0 && !isBelow(ptCoords, currentPosition, currentRtan)) {
            currentRtan = currentPosition
        }

        if (prevE > 0 && enext <= 0 && !isAbove(ptCoords, currentPosition, currentLtan)) {
            currentLtan = currentPosition
        }

        prevE = enext
    }
    return currentRtan to currentLtan
}

private fun isAbove(point1: Position, point2: Position, point3: Position): Boolean =
    isLeft(point1, point2, point3) > 0

private fun isBelow(point1: Position, point2: Position, point3: Position): Boolean =
    isLeft(point1, point2, point3) < 0

private fun isLeft(point1: Position, point2: Position, point3: Position): Double =
    (point2.longitude - point1.longitude) * (point3.latitude - point1.latitude) -
        (point3.longitude - point1.longitude) * (point2.latitude - point1.latitude)

private fun Position.inBBox(bbox: org.maplibre.spatialk.geojson.BoundingBox): Boolean =
    bbox.west <= longitude &&
        bbox.south <= latitude &&
        bbox.east >= longitude &&
        bbox.north >= latitude

internal fun nearestPointIndex(target: Position, points: List<Position>): Int =
    points.indices.minBy { distance(target, points[it]) }
