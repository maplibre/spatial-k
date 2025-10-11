package org.maplibre.spatialk.turf.featureconversion

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.maplibre.spatialk.geojson.*

class CombineTests {

    @Test
    fun testCombineEmptyFeatureCollection() {
        val input = GeometryCollection<SingleGeometry>()
        val result = input.combine()

        assertEquals(3, result.size)

        val multiPoint = result.geometries[0] as MultiPoint
        assertTrue(multiPoint.coordinates.isEmpty())

        val multiLineString = result.geometries[1] as MultiLineString
        assertTrue(multiLineString.coordinates.isEmpty())

        val multiPolygon = result.geometries[2] as MultiPolygon
        assertTrue(multiPolygon.coordinates.isEmpty())
    }

    @Test
    fun testCombineOnlyPoints() {
        val input =
            GeometryCollection(
                Point(Position(0.0, 0.0)),
                Point(Position(1.0, 1.0)),
                Point(Position(2.0, 2.0)),
            )

        val result = input.combine()

        assertEquals(3, result.size)

        val multiPoint = result.geometries[0] as MultiPoint
        assertEquals(3, multiPoint.coordinates.size)
        assertEquals(Position(0.0, 0.0), multiPoint.coordinates[0])
        assertEquals(Position(1.0, 1.0), multiPoint.coordinates[1])
        assertEquals(Position(2.0, 2.0), multiPoint.coordinates[2])

        val multiLineString = result.geometries[1] as MultiLineString
        assertTrue(multiLineString.coordinates.isEmpty())

        val multiPolygon = result.geometries[2] as MultiPolygon
        assertTrue(multiPolygon.coordinates.isEmpty())
    }

    @Test
    fun testCombineOnlyLineStrings() {
        val input =
            GeometryCollection(
                LineString(listOf(Position(0.0, 0.0), Position(1.0, 1.0))),
                LineString(listOf(Position(2.0, 2.0), Position(3.0, 3.0))),
            )

        val result = input.combine()

        assertEquals(3, result.size)

        val multiPoint = result.geometries[0] as MultiPoint
        assertTrue(multiPoint.coordinates.isEmpty())

        val multiLineString = result.geometries[1] as MultiLineString
        assertEquals(2, multiLineString.coordinates.size)
        assertEquals(listOf(Position(0.0, 0.0), Position(1.0, 1.0)), multiLineString.coordinates[0])
        assertEquals(listOf(Position(2.0, 2.0), Position(3.0, 3.0)), multiLineString.coordinates[1])

        val multiPolygon = result.geometries[2] as MultiPolygon
        assertTrue(multiPolygon.coordinates.isEmpty())
    }

    @Test
    fun testCombineOnlyPolygons() {
        val input =
            GeometryCollection(
                Polygon(
                    listOf(
                        listOf(
                            Position(0.0, 0.0),
                            Position(1.0, 0.0),
                            Position(1.0, 1.0),
                            Position(0.0, 1.0),
                            Position(0.0, 0.0),
                        )
                    )
                ),
                Polygon(
                    listOf(
                        listOf(
                            Position(2.0, 2.0),
                            Position(3.0, 2.0),
                            Position(3.0, 3.0),
                            Position(2.0, 3.0),
                            Position(2.0, 2.0),
                        )
                    )
                ),
            )

        val result = input.combine()

        assertEquals(3, result.size)

        val multiPoint = result.geometries[0] as MultiPoint
        assertTrue(multiPoint.coordinates.isEmpty())

        val multiLineString = result.geometries[1] as MultiLineString
        assertTrue(multiLineString.coordinates.isEmpty())

        val multiPolygon = result.geometries[2] as MultiPolygon
        assertEquals(2, multiPolygon.coordinates.size)
        assertEquals(
            listOf(
                listOf(
                    Position(0.0, 0.0),
                    Position(1.0, 0.0),
                    Position(1.0, 1.0),
                    Position(0.0, 1.0),
                    Position(0.0, 0.0),
                )
            ),
            multiPolygon.coordinates[0],
        )
        assertEquals(
            listOf(
                listOf(
                    Position(2.0, 2.0),
                    Position(3.0, 2.0),
                    Position(3.0, 3.0),
                    Position(2.0, 3.0),
                    Position(2.0, 2.0),
                )
            ),
            multiPolygon.coordinates[1],
        )
    }

    @Test
    fun testCombineMixedGeometries() {
        val input =
            GeometryCollection(
                Point(Position(0.0, 0.0)),
                LineString(listOf(Position(1.0, 1.0), Position(2.0, 2.0))),
                Polygon(
                    listOf(
                        listOf(
                            Position(3.0, 3.0),
                            Position(4.0, 3.0),
                            Position(4.0, 4.0),
                            Position(3.0, 4.0),
                            Position(3.0, 3.0),
                        )
                    )
                ),
                Point(Position(5.0, 5.0)),
            )

        val result = input.combine()

        assertEquals(3, result.size)

        val multiPoint = result.geometries[0] as MultiPoint
        assertEquals(2, multiPoint.coordinates.size)
        assertEquals(Position(0.0, 0.0), multiPoint.coordinates[0])
        assertEquals(Position(5.0, 5.0), multiPoint.coordinates[1])

        val multiLineString = result.geometries[1] as MultiLineString
        assertEquals(1, multiLineString.coordinates.size)
        assertEquals(listOf(Position(1.0, 1.0), Position(2.0, 2.0)), multiLineString.coordinates[0])

        val multiPolygon = result.geometries[2] as MultiPolygon
        assertEquals(1, multiPolygon.coordinates.size)
        assertEquals(
            listOf(
                listOf(
                    Position(3.0, 3.0),
                    Position(4.0, 3.0),
                    Position(4.0, 4.0),
                    Position(3.0, 4.0),
                    Position(3.0, 3.0),
                )
            ),
            multiPolygon.coordinates[0],
        )
    }

    @Test
    fun testCombinePolygonWithHoles() {
        val input =
            GeometryCollection(
                Polygon(
                    listOf(
                        listOf(
                            Position(0.0, 0.0),
                            Position(4.0, 0.0),
                            Position(4.0, 4.0),
                            Position(0.0, 4.0),
                            Position(0.0, 0.0),
                        ),
                        listOf(
                            Position(1.0, 1.0),
                            Position(3.0, 1.0),
                            Position(3.0, 3.0),
                            Position(1.0, 3.0),
                            Position(1.0, 1.0),
                        ),
                    )
                )
            )

        val result = input.combine()

        assertEquals(3, result.size)

        val multiPolygon = result.geometries[2] as MultiPolygon
        assertEquals(1, multiPolygon.coordinates.size)
        assertEquals(2, multiPolygon.coordinates[0].size) // outer ring + hole
        assertEquals(
            listOf(
                Position(0.0, 0.0),
                Position(4.0, 0.0),
                Position(4.0, 4.0),
                Position(0.0, 4.0),
                Position(0.0, 0.0),
            ),
            multiPolygon.coordinates[0][0],
        )
        assertEquals(
            listOf(
                Position(1.0, 1.0),
                Position(3.0, 1.0),
                Position(3.0, 3.0),
                Position(1.0, 3.0),
                Position(1.0, 1.0),
            ),
            multiPolygon.coordinates[0][1],
        )
    }
}
