package org.maplibre.spatialk.turf

import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Polygon
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.testutil.readResourceFile
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GridsTest {

    private lateinit var box: BoundingBox

    @BeforeTest
    fun before() {
        Polygon.fromJson(readResourceFile("grids/bbox.json")).also {
            box = computeBbox(it.coordinates[0])
        }
    }

    @OptIn(ExperimentalTurfApi::class)
    @Test
    fun testSquareGrid() {
        squareGrid(bbox = box, cellWidth = 200.0, cellHeight = 200.0, units = Units.Meters).also {
            verifyValidGrid(it)
        }
    }

    @OptIn(ExperimentalTurfApi::class)
    @Test
    fun testSquareGridSameCellSizeButDifferentUnitWillHaveSameResult() {
        squareGrid(bbox = box, cellWidth = 0.2, cellHeight = 0.2, units = Units.Kilometers).also {
            verifyValidGrid(it)
        }
    }

    @OptIn(ExperimentalTurfApi::class)
    @Test
    fun defaultUnitsValueIsKilometers() {
        squareGrid(bbox = box, cellWidth = 0.2, cellHeight = 0.2).also {
            verifyValidGrid(it)
        }
    }

    @OptIn(ExperimentalTurfApi::class)
    private fun verifyValidGrid(grid: FeatureCollection) {
        assertEquals(16, grid.features.size)
        val expectedFistItem = mutableListOf(
            Position(13.170147683370761, 52.515969323342695),
            Position(13.170147683370761, 52.517765865),
            Position(13.17194422502807, 52.517765865),
            Position(13.17194422502807, 52.515969323342695),
            Position(13.170147683370761, 52.515969323342695)
        )
        assertEquals(expectedFistItem, grid.features.first().geometry!!.coordAll())
        val expectedLastItem = mutableListOf(
            Position(13.18272347497193, 52.517765865),
            Position(13.18272347497193, 52.51956240665731),
            Position(13.18452001662924, 52.51956240665731),
            Position(13.18452001662924, 52.517765865),
            Position(13.18272347497193, 52.517765865)
        )
        assertEquals(expectedLastItem, grid.features.last().geometry!!.coordAll())
    }

    @OptIn(ExperimentalTurfApi::class)
    @Test
    fun cellSizeBiggerThanBboxExtendLeadIntoEmptyGrid() {
        squareGrid(bbox = box, cellWidth = 2000.0, cellHeight = 2000.0, units = Units.Meters).also {
            assertEquals(0, it.features.size)
        }
    }

    @OptIn(ExperimentalTurfApi::class)
    @Test
    fun smallerCellSizeWillOutputMoreCellsInGrid() {
        squareGrid(bbox = box, cellWidth = 0.1, cellHeight = 0.1).also {
            assertEquals(85, it.features.size)
        }
    }

    @OptIn(ExperimentalTurfApi::class)
    @Test
    fun increasedCellSizeWillOutputLessCellsInGrid() {
        squareGrid(bbox = box, cellWidth = 0.3, cellHeight = 0.3).also {
            assertEquals(5, it.features.size)
        }
    }
}
