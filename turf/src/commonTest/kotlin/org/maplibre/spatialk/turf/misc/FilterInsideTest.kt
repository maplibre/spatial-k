package org.maplibre.spatialk.turf.misc

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.maplibre.spatialk.geojson.GeometryCollection
import org.maplibre.spatialk.geojson.MultiPoint
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Polygon
import org.maplibre.spatialk.geojson.Position

class FilterInsideTest {

    @Test
    fun testFilterInsidePoint() {
        // test with a single point
        val result =
            GeometryCollection(Point(50.0, 50.0))
                .filterInside(
                    GeometryCollection(
                        Polygon(
                            listOf(
                                Position(0.0, 0.0),
                                Position(0.0, 100.0),
                                Position(100.0, 100.0),
                                Position(100.0, 0.0),
                                Position(0.0, 0.0),
                            )
                        )
                    )
                )

        assertTrue(result.geometries.isNotEmpty(), "returns a geometry collection")
        assertEquals(1, result.geometries.size, "1 point in 1 polygon")

        // test with multiple points and multiple polygons
        val result2 =
            GeometryCollection(
                    Point(1.0, 1.0),
                    Point(1.0, 3.0),
                    Point(14.0, 2.0),
                    Point(13.0, 1.0),
                    Point(19.0, 7.0),
                    Point(100.0, 7.0),
                )
                .filterInside(
                    GeometryCollection(
                        Polygon(
                            listOf(
                                Position(0.0, 0.0),
                                Position(10.0, 0.0),
                                Position(10.0, 10.0),
                                Position(0.0, 10.0),
                                Position(0.0, 0.0),
                            )
                        ),
                        Polygon(
                            listOf(
                                Position(10.0, 0.0),
                                Position(20.0, 10.0),
                                Position(20.0, 20.0),
                                Position(20.0, 0.0),
                                Position(10.0, 0.0),
                            )
                        ),
                    )
                )
        assertTrue(result2.geometries.isNotEmpty(), "returns a geometry collection")
        assertEquals(5, result2.geometries.size, "multiple points in multiple polygons")
    }

    @Test
    fun testFilterInsideMultiPoint() {
        val polygons =
            GeometryCollection(
                Polygon(
                    listOf(
                        Position(0.0, 0.0),
                        Position(0.0, 100.0),
                        Position(100.0, 100.0),
                        Position(100.0, 0.0),
                        Position(0.0, 0.0),
                    )
                )
            )

        // multipoint within
        val mpWithin =
            GeometryCollection(listOf(MultiPoint(Position(50.0, 50.0)))).filterInside(polygons)
        assertTrue(mpWithin.geometries.isNotEmpty(), "returns a geometry collection")
        assertEquals(1, mpWithin.geometries.size, "1 multipoint in 1 polygon")

        // multipoint collection within
        val mptCollection = GeometryCollection(listOf(MultiPoint(Position(50.0, 50.0))))
        val fcWithin = mptCollection.filterInside(polygons)
        assertTrue(fcWithin.geometries.isNotEmpty(), "returns a geometry collection")
        assertEquals(1, fcWithin.geometries.size, "1 multipoint in 1 polygon")

        // multipoint not within
        val mpNotWithin =
            GeometryCollection(listOf(MultiPoint(Position(150.0, 150.0)))).filterInside(polygons)
        assertEquals(0, mpNotWithin.geometries.size, "0 multipoint in 1 polygon")

        // multipoint with point coords both within and not within
        val mpPartWithin =
            GeometryCollection(listOf(MultiPoint(Position(50.0, 50.0), Position(150.0, 150.0))))
                .filterInside(polygons)
        assertTrue(mpPartWithin.geometries.isNotEmpty(), "returns a geometry collection")
        val partCoords = mpPartWithin.geometries[0].coordinates
        assertEquals(
            1,
            partCoords.size,
            "multipoint result should have 1 remaining coord that was within polygon",
        )
        assertEquals(
            MultiPoint(Position(50.0, 50.0), Position(150.0, 150.0)).coordinates[0].longitude,
            partCoords[0].longitude,
            "remaining coord should have expected x value",
        )
        assertEquals(
            MultiPoint(Position(50.0, 50.0), Position(150.0, 150.0)).coordinates[0].latitude,
            partCoords[0].latitude,
            "remaining coord should have expected y value",
        )

        // multiple multipoints and multiple polygons
        val mptCollection2 =
            GeometryCollection(
                MultiPoint(Position(50.0, 50.0)),
                MultiPoint(Position(150.0, 150.0)),
                MultiPoint(Position(50.0, 50.0), Position(150.0, 150.0)),
            )

        val fcMultiWithin =
            mptCollection2.filterInside(
                GeometryCollection(
                    Polygon(
                        listOf(
                            Position(0.0, 0.0),
                            Position(0.0, 100.0),
                            Position(100.0, 100.0),
                            Position(100.0, 0.0),
                            Position(0.0, 0.0),
                        )
                    ),
                    Polygon(
                        listOf(
                            Position(10.0, 0.0),
                            Position(20.0, 10.0),
                            Position(20.0, 20.0),
                            Position(20.0, 0.0),
                            Position(10.0, 0.0),
                        )
                    ),
                )
            )
        assertTrue(fcMultiWithin.geometries.isNotEmpty(), "returns a geometry collection")
        assertEquals(2, fcMultiWithin.geometries.size, "multiple points in multiple polygons")
    }

    @Test
    fun testFilterInsidePointAndMultiPoint() {
        val poly =
            Polygon(
                listOf(
                    Position(0.0, 0.0),
                    Position(0.0, 100.0),
                    Position(100.0, 100.0),
                    Position(100.0, 0.0),
                    Position(0.0, 0.0),
                )
            )
        val polygons = GeometryCollection(poly)

        val mixedCollection =
            GeometryCollection(
                Point(50.0, 50.0),
                MultiPoint(Position(50.0, 50.0)),
                MultiPoint(Position(150.0, 150.0)),
            )

        val result = mixedCollection.filterInside(polygons)

        assertTrue(result.geometries.isNotEmpty(), "returns a geometry collection")
        assertEquals(2, result.geometries.size, "1 point and 1 multipoint in 1 polygon")
        assertTrue(result.geometries[0] is Point, "first geometry is Point")
        assertTrue(result.geometries[1] is MultiPoint, "second geometry is MultiPoint")
    }

    @Test
    fun testFilterInsideNoDuplicatesWhenMultipleGeometryContainAPoint() {
        val poly1 =
            Polygon(
                listOf(
                    Position(0.0, 0.0),
                    Position(10.0, 0.0),
                    Position(10.0, 10.0),
                    Position(0.0, 10.0),
                    Position(0.0, 0.0),
                )
            )
        val poly2 =
            Polygon(
                listOf(
                    Position(0.0, 0.0),
                    Position(10.0, 0.0),
                    Position(10.0, 10.0),
                    Position(0.0, 10.0),
                    Position(0.0, 0.0),
                )
            )
        val polygons = GeometryCollection(poly1, poly2)
        val points = GeometryCollection(Point(5.0, 5.0))

        val result = points.filterInside(polygons)
        assertEquals(
            1,
            result.geometries.size,
            "although point is contained by two polygons it is only counted once",
        )
    }
}
