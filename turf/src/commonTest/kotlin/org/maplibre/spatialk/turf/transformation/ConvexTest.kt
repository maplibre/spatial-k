package org.maplibre.spatialk.turf.transformation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.serialization.json.JsonObject
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Geometry
import org.maplibre.spatialk.geojson.GeometryCollection
import org.maplibre.spatialk.geojson.Polygon
import org.maplibre.spatialk.testutil.readResourceFile
import org.maplibre.spatialk.turf.coordinatemutation.flattenCoordinates

class ConvexTest {

    @Test
    fun testConvexElevation1() {
        testConvex(
            "transformation/convex/in/elevation1.geojson",
            "transformation/convex/out/elevation1.geojson",
        )
    }

    @Test
    fun testConvexElevation2() {
        testConvex(
            "transformation/convex/in/elevation2.geojson",
            "transformation/convex/out/elevation2.geojson",
        )
    }

    @Test
    fun testConvexElevation3() {
        testConvex(
            "transformation/convex/in/elevation3.geojson",
            "transformation/convex/out/elevation3.geojson",
        )
    }

    @Test
    fun testConvexElevation4() {
        testConvex(
            "transformation/convex/in/elevation4.geojson",
            "transformation/convex/out/elevation4.geojson",
        )
    }

    @Test
    fun testConvexElevation5() {
        testConvex(
            "transformation/convex/in/elevation5.geojson",
            "transformation/convex/out/elevation5.geojson",
        )
    }

    // The monotone chain cannot form a polygon from fewer than 3 distinct points; convex must then
    // return null instead of throwing on Polygon construction.
    @Test
    fun testConvexWithSinglePointReturnsNull() {
        val point = org.maplibre.spatialk.geojson.Point(longitude = 10.0, latitude = 20.0)
        assertNull(point.convex())
    }

    @Test
    fun testConvexWithTwoPointsReturnsNull() {
        val line =
            org.maplibre.spatialk.geojson.LineString(
                org.maplibre.spatialk.geojson.Position(10.0, 20.0),
                org.maplibre.spatialk.geojson.Position(30.0, 40.0),
            )
        assertNull(line.convex())
    }

    // A square is convex, so its hull is the square itself. The monotone chain must return the
    // square rather than null (a 4-vertex convex hull is valid).
    @Test
    fun testConvexWithSquareReturnsHull() {
        val polygon =
            Polygon(
                listOf(
                    org.maplibre.spatialk.geojson.Position(0.0, 0.0),
                    org.maplibre.spatialk.geojson.Position(1.0, 0.0),
                    org.maplibre.spatialk.geojson.Position(1.0, 1.0),
                    org.maplibre.spatialk.geojson.Position(0.0, 1.0),
                    org.maplibre.spatialk.geojson.Position(0.0, 0.0),
                )
            )
        assertEquals(polygon, polygon.convex())
    }

    // Three distinct points form a valid triangular concave hull; the previous strict `> 3` check
    // filtered it out and returned null.
    @Test
    fun testConvexTriangleWithConcavityReturnsHull() {
        val triangle =
            Polygon(
                listOf(
                    listOf(
                        org.maplibre.spatialk.geojson.Position(0.0, 0.0),
                        org.maplibre.spatialk.geojson.Position(1.0, 0.0),
                        org.maplibre.spatialk.geojson.Position(0.5, 1.0),
                        org.maplibre.spatialk.geojson.Position(0.0, 0.0),
                    )
                )
            )
        val hull = triangle.convex(concavity = 3)
        assertNotNull(hull)
        assertEquals(3, hull.flattenCoordinates().distinct().size)
    }

    private fun testConvex(input: String, expected: String) {
        val fc = FeatureCollection.fromJson<Geometry?, JsonObject?>(readResourceFile(input))
        val expectedPolygon =
            assertIs<Polygon>(
                FeatureCollection.fromJson<Geometry?, JsonObject?>(readResourceFile(expected))
                    .last()
                    .geometry
            )
        val geometryCollection = GeometryCollection(fc.features.mapNotNull { it.geometry })
        val polygon = geometryCollection.convex()

        assertNotNull(polygon)
        val actual = polygon.flattenCoordinates().distinct().sortedBy { it.longitude }
        val expectedCoordinates =
            expectedPolygon.flattenCoordinates().distinct().sortedBy { it.longitude }
        assertEquals(expectedCoordinates, actual)
        assertEquals(expectedPolygon, polygon)
    }
}
