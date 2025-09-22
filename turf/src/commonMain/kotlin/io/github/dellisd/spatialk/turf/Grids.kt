package io.github.dellisd.spatialk.turf

import io.github.dellisd.spatialk.geojson.BoundingBox
import io.github.dellisd.spatialk.geojson.Feature
import io.github.dellisd.spatialk.geojson.FeatureCollection
import io.github.dellisd.spatialk.geojson.Geometry
import io.github.dellisd.spatialk.geojson.Polygon
import io.github.dellisd.spatialk.geojson.Position
import kotlin.math.abs
import kotlin.math.floor

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
    units: Units = Units.Kilometers
): FeatureCollection {
    val featureList = mutableListOf<Feature<Geometry>>()
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
            val positions = mutableListOf<Position>().apply {
                add(Position(currentX, currentY))
                add(Position(currentX, currentY + cellHeightDeg))
                add(Position(currentX + cellWidthDeg, currentY + cellHeightDeg))
                add(Position(currentX + cellWidthDeg, currentY))
                add(Position(currentX, currentY))
            }
            mutableListOf<List<Position>>().apply {
                add(positions)
            }.also {
                featureList.add(Feature(Polygon(it)))
            }
            currentY += cellHeightDeg
        }
        currentX += cellWidthDeg
    }
    return FeatureCollection(featureList)
}
