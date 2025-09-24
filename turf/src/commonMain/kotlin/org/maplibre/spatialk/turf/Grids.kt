package org.maplibre.spatialk.turf

import kotlin.math.abs
import kotlin.math.floor
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Polygon
import org.maplibre.spatialk.geojson.Position

/**
 * Creates a square grid within a [BoundingBox].
 *
 * @param bbox [BoundingBox] bbox extent
 * @param cellWidth of each cell, in units
 * @param cellHeight of each cell, in units
 * @param units The unit of measurement of the cellSide length
 * @return a [FeatureCollection] grid of polygons
 */
@ExperimentalTurfApi
public fun squareGrid(
    bbox: BoundingBox,
    cellWidth: Double,
    cellHeight: Double,
    units: Units = Units.Kilometers,
): FeatureCollection {
    val featureList = mutableListOf<Feature>()
    val west = bbox.southwest.longitude
    val south = bbox.southwest.latitude
    val east = bbox.northeast.longitude
    val north = bbox.northeast.latitude

    val bboxWidth = east - west
    val cellWidthDeg = convertLength(cellWidth, units, Units.Degrees)

    val bboxHeight = north - south
    val cellHeightDeg = convertLength(cellHeight, units, Units.Degrees)

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
            mutableListOf<List<Position>>()
                .apply { add(positions) }
                .also { featureList.add(Feature(Polygon(it))) }
            currentY += cellHeightDeg
        }
        currentX += cellWidthDeg
    }
    return FeatureCollection(featureList)
}
