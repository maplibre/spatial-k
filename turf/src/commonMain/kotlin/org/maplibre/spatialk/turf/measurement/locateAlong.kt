@file:JvmName("Measurement")
@file:JvmMultifileClass

package org.maplibre.spatialk.turf.measurement

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmSynthetic
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.units.International.Meters
import org.maplibre.spatialk.units.Length
import org.maplibre.spatialk.units.LengthUnit
import org.maplibre.spatialk.units.extensions.toLength

/**
 * Takes a [LineString] and returns a [position][Position] at a specified distance along the line.
 *
 * @param distance distance along the line
 * @return A position [distance] along the line
 */
@JvmSynthetic
public fun LineString.locateAlong(distance: Length): Position {
    var travelled = Length.Zero

    coordinates.forEachIndexed { i, coordinate ->
        when {
            distance >= travelled && i == coordinates.size - 1 -> {}
            travelled >= distance -> {
                val overshot = distance - travelled
                return if (overshot.isZero) coordinate
                else {
                    val direction = coordinates[i - 1].bearingTo(coordinate)
                    coordinate.offset(overshot, direction)
                }
            }

            else -> travelled += distance(coordinate, coordinates[i + 1])
        }
    }

    return coordinates[coordinates.size - 1]
}

@PublishedApi
@Suppress("unused")
@JvmOverloads
internal fun locateAlong(line: LineString, distance: Double, unit: LengthUnit = Meters): Position =
    line.locateAlong(distance.toLength(unit))
