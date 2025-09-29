package org.maplibre.spatialk.turf.measurement

import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.turf.ExperimentalTurfApi
import org.maplibre.spatialk.units.Length

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
