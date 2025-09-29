package org.maplibre.spatialk.turf.grids

import kotlin.math.abs
import kotlin.math.floor
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Polygon
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.turf.ExperimentalTurfApi
import org.maplibre.spatialk.units.Length
import org.maplibre.spatialk.units.LengthUnit.Geodesy.*

/**
 * Creates a square grid within a [BoundingBox].
 *
 * @param bbox [BoundingBox] bbox extent
 * @param cellWidth of each cell
 * @param cellHeight of each cell
 * @return a [FeatureCollection] grid of polygons
 */
@ExperimentalTurfApi
public fun squareGrid(bbox: BoundingBox, cellWidth: Length, cellHeight: Length): FeatureCollection {
    val featureList = mutableListOf<Feature>()
    val west = bbox.southwest.longitude
    val south = bbox.southwest.latitude
    val east = bbox.northeast.longitude
    val north = bbox.northeast.latitude

    val bboxWidth = east - west
    val cellWidthDeg = cellWidth.toDouble(Degrees)

    val bboxHeight = north - south
    val cellHeightDeg = cellHeight.toDouble(Degrees)

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
