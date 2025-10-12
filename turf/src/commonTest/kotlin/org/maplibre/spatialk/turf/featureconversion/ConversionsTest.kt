package org.maplibre.spatialk.turf.featureconversion

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.maplibre.spatialk.geojson.*

class ConversionsTest {

    // Single -> Multi conversions

    @Test
    fun testPointToMultiPoint() {
        val point = Point(Position(1.0, 2.0))
        val result = point.toMultiPoint()

        assertEquals(listOf(Position(1.0, 2.0)), result.coordinates)
    }

    @Test
    fun testLineStringToMultiLineString() {
        val lineString =
            LineString(listOf(Position(0.0, 0.0), Position(1.0, 1.0), Position(2.0, 2.0)))
        val result = lineString.toMultiLineString()

        assertEquals(
            listOf(listOf(Position(0.0, 0.0), Position(1.0, 1.0), Position(2.0, 2.0))),
            result.coordinates,
        )
    }

    @Test
    fun testPolygonToMultiPolygon() {
        val polygon =
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
            )
        val result = polygon.toMultiPolygon()

        assertEquals(
            listOf(
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
            result.coordinates,
        )
    }

    // Polygon -> MultiLineString conversions

    @Test
    fun testPolygonToMultiLineString() {
        val polygon =
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
            )
        val result = polygon.toMultiLineString()

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
            result.coordinates,
        )
    }

    @Test
    fun testPolygonWithHoleToMultiLineString() {
        val polygon =
            Polygon(
                listOf(
                    // Outer ring
                    listOf(
                        Position(0.0, 0.0),
                        Position(4.0, 0.0),
                        Position(4.0, 4.0),
                        Position(0.0, 4.0),
                        Position(0.0, 0.0),
                    ),
                    // Inner ring (hole)
                    listOf(
                        Position(1.0, 1.0),
                        Position(3.0, 1.0),
                        Position(3.0, 3.0),
                        Position(1.0, 3.0),
                        Position(1.0, 1.0),
                    ),
                )
            )
        val result = polygon.toMultiLineString()

        assertEquals(2, result.coordinates.size)
        assertEquals(
            listOf(
                Position(0.0, 0.0),
                Position(4.0, 0.0),
                Position(4.0, 4.0),
                Position(0.0, 4.0),
                Position(0.0, 0.0),
            ),
            result.coordinates[0],
        )
        assertEquals(
            listOf(
                Position(1.0, 1.0),
                Position(3.0, 1.0),
                Position(3.0, 3.0),
                Position(1.0, 3.0),
                Position(1.0, 1.0),
            ),
            result.coordinates[1],
        )
    }

    @Test
    fun testMultiPolygonToMultiLineStrings() {
        val multiPolygon =
            MultiPolygon(
                listOf(
                    listOf(
                        listOf(
                            Position(0.0, 0.0),
                            Position(1.0, 0.0),
                            Position(1.0, 1.0),
                            Position(0.0, 1.0),
                            Position(0.0, 0.0),
                        )
                    ),
                    listOf(
                        listOf(
                            Position(2.0, 2.0),
                            Position(3.0, 2.0),
                            Position(3.0, 3.0),
                            Position(2.0, 3.0),
                            Position(2.0, 2.0),
                        )
                    ),
                )
            )
        val result = multiPolygon.toMultiLineStrings()

        assertEquals(2, result.size)
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
            result.geometries[0].coordinates,
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
            result.geometries[1].coordinates,
        )
    }

    // LineString -> Polygon conversions

    @Test
    fun testLineStringToPolygonDefaults() {
        val lineString =
            LineString(
                listOf(
                    Position(0.0, 0.0),
                    Position(1.0, 0.0),
                    Position(1.0, 1.0),
                    Position(0.0, 1.0),
                )
            )
        val result = lineString.toPolygon()

        assertEquals(1, result.coordinates.size)
        assertEquals(5, result.coordinates[0].size)
        assertEquals(Position(0.0, 0.0), result.coordinates[0].first())
        assertEquals(Position(0.0, 0.0), result.coordinates[0].last())
    }

    @Test
    fun testMultiLineStringToPolygonDefaults() {
        // Small ring first, large ring second - should be reordered
        val multiLineString =
            MultiLineString(
                listOf(
                    // Small inner ring (hole)
                    listOf(
                        Position(1.0, 1.0),
                        Position(2.0, 1.0),
                        Position(2.0, 2.0),
                        Position(1.0, 2.0),
                    ),
                    // Large outer ring
                    listOf(
                        Position(0.0, 0.0),
                        Position(4.0, 0.0),
                        Position(4.0, 4.0),
                        Position(0.0, 4.0),
                    ),
                )
            )
        val result = multiLineString.toPolygon()

        assertEquals(2, result.coordinates.size)

        // Large ring should be first (reordered)
        assertEquals(Position(0.0, 0.0), result.coordinates[0].first())
        assertEquals(Position(0.0, 0.0), result.coordinates[0].last()) // and closed

        // Small ring should be second
        assertEquals(Position(1.0, 1.0), result.coordinates[1].first())
        assertEquals(Position(1.0, 1.0), result.coordinates[1].last()) // and closed
    }

    @Test
    fun testMultiLineStringToPolygonNoAutoClose() {
        // With autoClose = false, rings must already be closed
        val multiLineString =
            MultiLineString(
                listOf(
                    listOf(
                        Position(0.0, 0.0),
                        Position(1.0, 0.0),
                        Position(1.0, 1.0),
                        Position(0.0, 1.0),
                        Position(0.0, 0.0),
                    )
                )
            )
        val result = multiLineString.toPolygon(autoClose = false, autoOrder = true)

        assertEquals(1, result.coordinates.size)
        assertEquals(5, result.coordinates[0].size)
        assertEquals(Position(0.0, 0.0), result.coordinates[0].first())
        assertEquals(Position(0.0, 0.0), result.coordinates[0].last())
    }

    @Test
    fun testMultiLineStringToPolygonNoAutoOrder() {
        // Small ring first, large ring second - should NOT be reordered
        val multiLineString =
            MultiLineString(
                listOf(
                    // Small inner ring
                    listOf(
                        Position(1.0, 1.0),
                        Position(2.0, 1.0),
                        Position(2.0, 2.0),
                        Position(1.0, 2.0),
                    ),
                    // Large outer ring
                    listOf(
                        Position(0.0, 0.0),
                        Position(4.0, 0.0),
                        Position(4.0, 4.0),
                        Position(0.0, 4.0),
                    ),
                )
            )
        val result = multiLineString.toPolygon(autoClose = true, autoOrder = false)

        assertEquals(2, result.coordinates.size)

        // Small ring should still be first (NOT reordered)
        assertEquals(Position(1.0, 1.0), result.coordinates[0].first())

        // Large ring should still be second
        assertEquals(Position(0.0, 0.0), result.coordinates[1].first())
    }

    @Test
    fun testMultiLineStringToPolygonBothFalse() {
        // With autoClose = false, rings must already be closed
        val multiLineString =
            MultiLineString(
                listOf(
                    // Small ring first
                    listOf(
                        Position(1.0, 1.0),
                        Position(2.0, 1.0),
                        Position(2.0, 2.0),
                        Position(1.0, 2.0),
                        Position(1.0, 1.0),
                    ),
                    // Large ring second
                    listOf(
                        Position(0.0, 0.0),
                        Position(4.0, 0.0),
                        Position(4.0, 4.0),
                        Position(0.0, 4.0),
                        Position(0.0, 0.0),
                    ),
                )
            )
        val result = multiLineString.toPolygon(autoClose = false, autoOrder = false)

        assertEquals(2, result.coordinates.size)

        // Small ring should still be first (NOT reordered)
        assertEquals(Position(1.0, 1.0), result.coordinates[0].first())
        assertEquals(5, result.coordinates[0].size)

        // Large ring should still be second
        assertEquals(Position(0.0, 0.0), result.coordinates[1].first())
        assertEquals(5, result.coordinates[1].size)
    }

    @Test
    fun testMultiLineStringToPolygonUnclosedRingsThrows() {
        // When autoClose = false and rings are not closed, should throw
        val multiLineString =
            MultiLineString(
                listOf(
                    listOf(
                        Position(0.0, 0.0),
                        Position(1.0, 0.0),
                        Position(1.0, 1.0),
                        Position(0.0, 1.0),
                    )
                )
            )
        assertFailsWith<IllegalArgumentException> {
            multiLineString.toPolygon(autoClose = false, autoOrder = true)
        }
    }

    @Test
    fun testGeometryCollectionToMultiPolygon() {
        val geometryCollection =
            GeometryCollection(
                listOf(
                    LineString(
                        listOf(
                            Position(0.0, 0.0),
                            Position(1.0, 0.0),
                            Position(1.0, 1.0),
                            Position(0.0, 1.0),
                        )
                    ),
                    LineString(
                        listOf(
                            Position(2.0, 2.0),
                            Position(3.0, 2.0),
                            Position(3.0, 3.0),
                            Position(2.0, 3.0),
                        )
                    ),
                )
            )
        val result = geometryCollection.toMultiPolygon(autoClose = true, autoOrder = false)

        assertEquals(2, result.coordinates.size)
        assertEquals(5, result.coordinates[0][0].size) // First polygon should be closed
        assertEquals(5, result.coordinates[1][0].size) // Second polygon should be closed
    }

    // FeatureCollection <-> GeometryCollection conversions

    @Test
    fun testFeatureCollectionToGeometryCollection() {
        val featureCollection =
            FeatureCollection(
                Feature(Point(Position(0.0, 0.0)), null),
                Feature(Point(Position(1.0, 1.0)), null),
                Feature(Point(Position(2.0, 2.0)), null),
            )
        val result = featureCollection.toGeometryCollection()

        assertEquals(3, result.size)
        assertEquals(Position(0.0, 0.0), result.geometries[0].coordinates)
        assertEquals(Position(1.0, 1.0), result.geometries[1].coordinates)
        assertEquals(Position(2.0, 2.0), result.geometries[2].coordinates)
    }

    @Test
    fun testFeatureCollectionWithNullGeometryToGeometryCollection() {
        val featureCollection =
            FeatureCollection(
                Feature(Point(Position(0.0, 0.0)), null),
                Feature(null, null),
                Feature(Point(Position(2.0, 2.0)), null),
            )
        val result = featureCollection.toGeometryCollection()

        assertEquals(2, result.size)
        assertEquals(Position(0.0, 0.0), result.geometries[0].coordinates)
        assertEquals(Position(2.0, 2.0), result.geometries[1].coordinates)
    }

    @Test
    fun testGeometryCollectionToFeatureCollection() {
        val geometryCollection =
            GeometryCollection(
                listOf(
                    Point(Position(0.0, 0.0)),
                    Point(Position(1.0, 1.0)),
                    Point(Position(2.0, 2.0)),
                )
            )
        val result = geometryCollection.toFeatureCollection { properties = null }

        assertEquals(3, result.size)
        assertEquals(Position(0.0, 0.0), result.features[0].geometry.coordinates)
        assertEquals(Position(1.0, 1.0), result.features[1].geometry.coordinates)
        assertEquals(Position(2.0, 2.0), result.features[2].geometry.coordinates)
    }
}
