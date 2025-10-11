package org.maplibre.spatialk.turf.featureconversion

import kotlin.test.Test
import kotlin.test.assertEquals
import org.maplibre.spatialk.geojson.*

class ExplodeTest {

    @Test
    fun testExplodePoint() {
        val point = Point(Position(1.0, 2.0))
        val result = point.explode()

        assertEquals(1, result.size)
        assertEquals(Position(1.0, 2.0), result.geometries[0].coordinates)
    }

    @Test
    fun testExplodeMultiPoint() {
        val multiPoint =
            MultiPoint(listOf(Position(1.0, 2.0), Position(3.0, 4.0), Position(5.0, 6.0)))
        val result = multiPoint.explode()

        assertEquals(3, result.size)
        assertEquals(Position(1.0, 2.0), result.geometries[0].coordinates)
        assertEquals(Position(3.0, 4.0), result.geometries[1].coordinates)
        assertEquals(Position(5.0, 6.0), result.geometries[2].coordinates)
    }

    @Test
    fun testExplodeLineString() {
        val lineString =
            LineString(listOf(Position(1.0, 2.0), Position(3.0, 4.0), Position(5.0, 6.0)))
        val result = lineString.explode()

        assertEquals(3, result.size)
        assertEquals(Position(1.0, 2.0), result.geometries[0].coordinates)
        assertEquals(Position(3.0, 4.0), result.geometries[1].coordinates)
        assertEquals(Position(5.0, 6.0), result.geometries[2].coordinates)
    }

    @Test
    fun testExplodeMultiLineString() {
        val multiLineString =
            MultiLineString(
                listOf(
                    listOf(Position(1.0, 2.0), Position(3.0, 4.0)),
                    listOf(Position(5.0, 6.0), Position(7.0, 8.0), Position(9.0, 10.0)),
                )
            )
        val result = multiLineString.explode()

        assertEquals(5, result.size)
        assertEquals(Position(1.0, 2.0), result.geometries[0].coordinates)
        assertEquals(Position(3.0, 4.0), result.geometries[1].coordinates)
        assertEquals(Position(5.0, 6.0), result.geometries[2].coordinates)
        assertEquals(Position(7.0, 8.0), result.geometries[3].coordinates)
        assertEquals(Position(9.0, 10.0), result.geometries[4].coordinates)
    }

    @Test
    fun testExplodePolygon() {
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
        val result = polygon.explode()

        assertEquals(5, result.size)
        assertEquals(Position(0.0, 0.0), result.geometries[0].coordinates)
        assertEquals(Position(1.0, 0.0), result.geometries[1].coordinates)
        assertEquals(Position(1.0, 1.0), result.geometries[2].coordinates)
        assertEquals(Position(0.0, 1.0), result.geometries[3].coordinates)
        assertEquals(Position(0.0, 0.0), result.geometries[4].coordinates)
    }

    @Test
    fun testExplodePolygonWithHoles() {
        val polygon =
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
        val result = polygon.explode()

        assertEquals(10, result.size)
        // Outer ring
        assertEquals(Position(0.0, 0.0), result.geometries[0].coordinates)
        assertEquals(Position(4.0, 0.0), result.geometries[1].coordinates)
        assertEquals(Position(4.0, 4.0), result.geometries[2].coordinates)
        assertEquals(Position(0.0, 4.0), result.geometries[3].coordinates)
        assertEquals(Position(0.0, 0.0), result.geometries[4].coordinates)
        // Hole
        assertEquals(Position(1.0, 1.0), result.geometries[5].coordinates)
        assertEquals(Position(3.0, 1.0), result.geometries[6].coordinates)
        assertEquals(Position(3.0, 3.0), result.geometries[7].coordinates)
        assertEquals(Position(1.0, 3.0), result.geometries[8].coordinates)
        assertEquals(Position(1.0, 1.0), result.geometries[9].coordinates)
    }

    @Test
    fun testExplodeMultiPolygon() {
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
        val result = multiPolygon.explode()

        assertEquals(10, result.size)
        // First polygon
        assertEquals(Position(0.0, 0.0), result.geometries[0].coordinates)
        assertEquals(Position(1.0, 0.0), result.geometries[1].coordinates)
        assertEquals(Position(1.0, 1.0), result.geometries[2].coordinates)
        assertEquals(Position(0.0, 1.0), result.geometries[3].coordinates)
        assertEquals(Position(0.0, 0.0), result.geometries[4].coordinates)
        // Second polygon
        assertEquals(Position(2.0, 2.0), result.geometries[5].coordinates)
        assertEquals(Position(3.0, 2.0), result.geometries[6].coordinates)
        assertEquals(Position(3.0, 3.0), result.geometries[7].coordinates)
        assertEquals(Position(2.0, 3.0), result.geometries[8].coordinates)
        assertEquals(Position(2.0, 2.0), result.geometries[9].coordinates)
    }

    @Test
    fun testExplodeGeometryCollection() {
        val geometryCollection =
            GeometryCollection(
                listOf(
                    Point(Position(1.0, 2.0)),
                    LineString(listOf(Position(3.0, 4.0), Position(5.0, 6.0))),
                    MultiPoint(listOf(Position(7.0, 8.0), Position(9.0, 10.0))),
                )
            )
        val result = geometryCollection.explode()

        assertEquals(5, result.size)
        assertEquals(Position(1.0, 2.0), result.geometries[0].coordinates)
        assertEquals(Position(3.0, 4.0), result.geometries[1].coordinates)
        assertEquals(Position(5.0, 6.0), result.geometries[2].coordinates)
        assertEquals(Position(7.0, 8.0), result.geometries[3].coordinates)
        assertEquals(Position(9.0, 10.0), result.geometries[4].coordinates)
    }

    @Test
    fun testExplodeFeature() {
        val feature =
            Feature(LineString(listOf(Position(1.0, 2.0), Position(3.0, 4.0), Position(5.0, 6.0))))
        val result = feature.explode()

        assertEquals(3, result.size)
        assertEquals(Position(1.0, 2.0), result.geometries[0].coordinates)
        assertEquals(Position(3.0, 4.0), result.geometries[1].coordinates)
        assertEquals(Position(5.0, 6.0), result.geometries[2].coordinates)
    }

    @Test
    fun testExplodeFeatureWithNullGeometry() {
        val feature = Feature(null)
        val result = feature.explode()

        assertEquals(0, result.size)
    }

    @Test
    fun testExplodeFeatureCollection() {
        val featureCollection =
            FeatureCollection(
                Feature(Point(Position(1.0, 2.0))),
                Feature(LineString(listOf(Position(3.0, 4.0), Position(5.0, 6.0)))),
                Feature(MultiPoint(listOf(Position(7.0, 8.0), Position(9.0, 10.0)))),
            )
        val result = featureCollection.explode()

        assertEquals(5, result.size)
        assertEquals(Position(1.0, 2.0), result.geometries[0].coordinates)
        assertEquals(Position(3.0, 4.0), result.geometries[1].coordinates)
        assertEquals(Position(5.0, 6.0), result.geometries[2].coordinates)
        assertEquals(Position(7.0, 8.0), result.geometries[3].coordinates)
        assertEquals(Position(9.0, 10.0), result.geometries[4].coordinates)
    }

    @Test
    fun testExplodeEmptyFeatureCollection() {
        val featureCollection = FeatureCollection<Nothing?>()
        val result = featureCollection.explode()
        assertEquals(0, result.size)
    }

    @Test
    fun testExplodeComplexNestedStructure() {
        val complexGeometry =
            GeometryCollection(
                listOf(
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
                            )
                        )
                    ),
                    MultiLineString(
                        listOf(
                            listOf(Position(2.0, 2.0), Position(3.0, 3.0)),
                            listOf(Position(4.0, 4.0), Position(5.0, 5.0), Position(6.0, 6.0)),
                        )
                    ),
                )
            )
        val result = complexGeometry.explode()

        assertEquals(10, result.size)
        // MultiPolygon coordinates
        assertEquals(Position(0.0, 0.0), result.geometries[0].coordinates)
        assertEquals(Position(1.0, 0.0), result.geometries[1].coordinates)
        assertEquals(Position(1.0, 1.0), result.geometries[2].coordinates)
        assertEquals(Position(0.0, 1.0), result.geometries[3].coordinates)
        assertEquals(Position(0.0, 0.0), result.geometries[4].coordinates)
        // MultiLineString coordinates
        assertEquals(Position(2.0, 2.0), result.geometries[5].coordinates)
        assertEquals(Position(3.0, 3.0), result.geometries[6].coordinates)
        assertEquals(Position(4.0, 4.0), result.geometries[7].coordinates)
        assertEquals(Position(5.0, 5.0), result.geometries[8].coordinates)
        assertEquals(Position(6.0, 6.0), result.geometries[9].coordinates)
    }
}
