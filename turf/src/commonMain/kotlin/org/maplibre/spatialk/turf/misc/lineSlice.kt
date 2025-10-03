@file:JvmName("Miscellaneous")
@file:JvmMultifileClass

package org.maplibre.spatialk.turf.misc

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.Position

/**
 * Takes a [LineString], a start and a stop [Position] and returns a subsection of the line between
 * those points. The start and stop points do not need to fall exactly on the line.
 *
 * @param start Start position
 * @param stop Stop position
 * @return The sliced subsection of the line
 */
public fun LineString.slice(start: Position, stop: Position): LineString {
    val startVertex = nearestPointOnLine(this, start)
    val stopVertex = nearestPointOnLine(this, stop)

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
