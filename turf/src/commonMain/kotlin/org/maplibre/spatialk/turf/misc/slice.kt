@file:JvmName("Miscellaneous")
@file:JvmMultifileClass

package org.maplibre.spatialk.turf.misc

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmSynthetic
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.turf.measurement.bearingTo
import org.maplibre.spatialk.turf.measurement.distance
import org.maplibre.spatialk.turf.measurement.offset
import org.maplibre.spatialk.units.International.Meters
import org.maplibre.spatialk.units.Length
import org.maplibre.spatialk.units.LengthUnit
import org.maplibre.spatialk.units.extensions.toLength

/**
 * Takes a [LineString], a start and a stop [Position] and returns a subsection of the line between
 * those points. The start and stop points do not need to fall exactly on the line.
 *
 * @param start Start position
 * @param stop Stop position
 * @return The sliced subsection of the line
 */
public fun LineString.slice(start: Position, stop: Position): LineString {
    val startVertex = this.nearestPointTo(start)
    val stopVertex = this.nearestPointTo(stop)

    val (startPos, endPos) =
        if (startVertex.index <= stopVertex.index) startVertex to stopVertex
        else stopVertex to startVertex

    val positions = mutableListOf(startPos.point)
    for (i in startPos.index + 1 until endPos.index + 1) {
        positions.add(coordinates[i])
    }
    positions.add(endPos.point)

    return LineString(positions)
}

/**
 * Takes a [LineString] and a specified distance along the line to a [start] and [stop] [Position],
 * and returns a subsection of the line in-between those points.
 */
@JvmSynthetic
public fun LineString.slice(start: Length, stop: Length): LineString {
    val slice = mutableListOf<Position>()
    var travelled = Length.Zero

    coordinates.forEachIndexed { i, coordinate ->
        if (start >= travelled && i == coordinates.size - 1) {
            // Start is beyond the end of the line
            return@forEachIndexed
        } else if (travelled > start && slice.isEmpty()) {
            // Found the start point - interpolate backwards
            val overshot = start - travelled
            if (overshot.isZero) {
                slice.add(coordinate)
                return LineString(slice)
            }
            val direction = coordinates[i - 1].bearingTo(coordinate)
            val interpolated = coordinate.offset(overshot, direction)
            slice.add(interpolated)
        }

        if (travelled >= stop) {
            // Found the stop point - interpolate backwards and return
            val overshot = stop - travelled
            if (overshot.isZero) {
                slice.add(coordinate)
                return LineString(slice)
            }
            val direction = coordinates[i - 1].bearingTo(coordinate)
            val interpolated = coordinate.offset(overshot, direction)
            slice.add(interpolated)
            return LineString(slice)
        }

        if (travelled >= start) {
            // We're between start and stop, add the coordinate
            slice.add(coordinate)
        }

        if (i == coordinates.size - 1) {
            return LineString(slice)
        }

        travelled += distance(coordinate, coordinates[i + 1])
    }

    // If we get here and slice is empty, start is beyond the line
    if (travelled < start) {
        throw IllegalArgumentException("Start position is beyond line")
    }

    return LineString(slice)
}

@PublishedApi
@Suppress("unused")
@JvmOverloads
internal fun LineString.slice(start: Double, stop: Double, unit: LengthUnit = Meters): LineString =
    slice(start = start.toLength(unit), stop = stop.toLength(unit))
