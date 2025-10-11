@file:JvmName("Grids")
@file:JvmMultifileClass

package org.maplibre.spatialk.turf.grids

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmSynthetic
import kotlin.math.abs
import kotlin.math.floor
import org.maplibre.spatialk.geojson.*
import org.maplibre.spatialk.units.International.Meters
import org.maplibre.spatialk.units.Length
import org.maplibre.spatialk.units.LengthUnit
import org.maplibre.spatialk.units.extensions.inEarthDegrees
import org.maplibre.spatialk.units.extensions.toLength

/**
 * Creates a square grid within a [BoundingBox].
 *
 * @param bbox [BoundingBox] bbox extent
 * @param cellWidth of each cell
 * @param cellHeight of each cell
 * @return a [GeometryCollection] grid of polygons
 */
@JvmSynthetic
public fun squareGrid(
    bbox: BoundingBox,
    cellWidth: Length,
    cellHeight: Length = cellWidth,
): MultiPolygon {
    val polygons = mutableListOf<List<List<Position>>>()
    val west = bbox.southwest.longitude
    val south = bbox.southwest.latitude
    val east = bbox.northeast.longitude
    val north = bbox.northeast.latitude

    val bboxWidth = east - west
    val cellWidthDeg = cellWidth.inEarthDegrees

    val bboxHeight = north - south
    val cellHeightDeg = cellHeight.inEarthDegrees

    val columns = floor(abs(bboxWidth) / cellWidthDeg)
    val rows = floor(abs(bboxHeight) / cellHeightDeg)

    val deltaX = (bboxWidth - columns * cellWidthDeg) / 2
    val deltaY = (bboxHeight - rows * cellHeightDeg) / 2

    var currentX = west + deltaX
    repeat(columns.toInt()) {
        var currentY = south + deltaY
        repeat(rows.toInt()) {
            val positions =
                mutableListOf<Position>().apply {
                    add(Position(currentX, currentY))
                    add(Position(currentX, currentY + cellHeightDeg))
                    add(Position(currentX + cellWidthDeg, currentY + cellHeightDeg))
                    add(Position(currentX + cellWidthDeg, currentY))
                    add(Position(currentX, currentY))
                }
            mutableListOf<List<Position>>().apply { add(positions) }.also { polygons.add(it) }
            currentY += cellHeightDeg
        }
        currentX += cellWidthDeg
    }
    return MultiPolygon(polygons)
}

@PublishedApi
@Suppress("unused")
@JvmOverloads
internal fun squareGrid(
    bbox: BoundingBox,
    cellWidth: Double,
    cellHeight: Double = cellWidth,
    unit: LengthUnit = Meters,
): MultiPolygon = squareGrid(bbox, cellWidth.toLength(unit), cellHeight.toLength(unit))
