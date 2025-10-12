package org.maplibre.spatialk.turf.booleans

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.serialization.json.JsonObject
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.MultiPolygon
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Polygon
import org.maplibre.spatialk.geojson.dsl.addRing
import org.maplibre.spatialk.geojson.dsl.buildPolygon
import org.maplibre.spatialk.testutil.readResourceFile

class PointInPolygonTest {

    @Test
    fun testFeatureCollection() {
        // test for a simple polygon
        val poly = buildPolygon {
            addRing {
                add(0.0, 0.0)
                add(0.0, 100.0)
                add(100.0, 0.0)
            }
        }
        val ptIn = Point(50.0, 50.0)
        val ptOut = Point(140.0, 150.0)

        assertTrue(poly.contains(ptIn.coordinates), "point inside simple polygon")
        assertFalse(poly.contains(ptOut.coordinates), "point outside simple polygon")

        // test for a concave polygon
        val concavePoly = buildPolygon {
            addRing {
                add(0.0, 0.0)
                add(50.0, 50.0)
                add(0.0, 100.0)
                add(100.0, 100.0)
                add(100.0, 0.0)
            }
        }
        val ptConcaveIn = Point(75.0, 75.0)
        val ptConcaveOut = Point(25.0, 50.0)

        assertTrue(concavePoly.contains(ptConcaveIn.coordinates), "point inside concave polygon")
        assertFalse(concavePoly.contains(ptConcaveOut.coordinates), "point outside concave polygon")
    }

    @Test
    fun testPolyWithHole() {
        val ptInHole = Point(-86.69208526611328, 36.20373274711739)
        val ptInPoly = Point(-86.72229766845702, 36.20258997094334)
        val ptOutsidePoly = Point(-86.75079345703125, 36.18527313913089)
        val polyHole =
            Feature.fromJson<Polygon, JsonObject?>(
                    readResourceFile("booleans/in/poly-with-hole.geojson")
                )
                .geometry

        assertFalse(polyHole.contains(ptInHole.coordinates))
        assertTrue(polyHole.contains(ptInPoly.coordinates))
        assertFalse(polyHole.contains(ptOutsidePoly.coordinates))
    }

    @Test
    fun testMultipolygonWithHole() {
        val ptInHole = Point(-86.69208526611328, 36.20373274711739)
        val ptInPoly = Point(-86.72229766845702, 36.20258997094334)
        val ptInPoly2 = Point(-86.75079345703125, 36.18527313913089)
        val ptOutsidePoly = Point(-86.75302505493164, 36.23015046460186)
        val multiPolyHole =
            Feature.fromJson<MultiPolygon, JsonObject?>(
                    readResourceFile("booleans/in/multipoly-with-hole.geojson")
                )
                .geometry

        assertFalse(multiPolyHole.contains(ptInHole.coordinates))
        assertTrue(multiPolyHole.contains(ptInPoly.coordinates))
        assertTrue(multiPolyHole.contains(ptInPoly2.coordinates))
        assertTrue(multiPolyHole.contains(ptInPoly.coordinates))
        assertFalse(multiPolyHole.contains(ptOutsidePoly.coordinates))
    }

    @Test
    fun testBoundaryTest() {
        val poly1 = buildPolygon {
            addRing {
                add(10.0, 10.0)
                add(30.0, 20.0)
                add(50.0, 10.0)
                add(30.0, 0.0)
                add(10.0, 10.0)
            }
        }
        val poly2 = buildPolygon {
            addRing {
                add(10.0, 0.0)
                add(30.0, 20.0)
                add(50.0, 0.0)
                add(30.0, 10.0)
                add(10.0, 0.0)
            }
        }
        val poly3 = buildPolygon {
            addRing {
                add(10.0, 0.0)
                add(30.0, 20.0)
                add(50.0, 0.0)
                add(30.0, -20.0)
                add(10.0, 0.0)
            }
        }
        val poly4 = buildPolygon {
            addRing {
                add(0.0, 0.0)
                add(0.0, 20.0)
                add(50.0, 20.0)
                add(50.0, 0.0)
                add(40.0, 0.0)
                add(30.0, 10.0)
                add(30.0, 0.0)
                add(20.0, 10.0)
                add(10.0, 10.0)
                add(10.0, 0.0)
                add(0.0, 0.0)
            }
        }
        val poly5 = buildPolygon {
            addRing {
                add(0.0, 20.0)
                add(20.0, 40.0)
                add(40.0, 20.0)
                add(20.0, 0.0)
                add(0.0, 20.0)
            }
            addRing {
                add(10.0, 20.0)
                add(20.0, 30.0)
                add(30.0, 20.0)
                add(20.0, 10.0)
                add(10.0, 20.0)
            }
        }

        fun runTest(ignoreBoundary: Boolean) {
            val isBoundaryIncluded = !ignoreBoundary
            val tests =
                arrayOf(
                    Triple(poly1, Point(10.0, 10.0), isBoundaryIncluded), // 0
                    Triple(poly1, Point(30.0, 20.0), isBoundaryIncluded),
                    Triple(poly1, Point(50.0, 10.0), isBoundaryIncluded),
                    Triple(poly1, Point(30.0, 10.0), true),
                    Triple(poly1, Point(0.0, 10.0), false),
                    Triple(poly1, Point(60.0, 10.0), false),
                    Triple(poly1, Point(30.0, -10.0), false),
                    Triple(poly1, Point(30.0, 30.0), false),
                    Triple(poly2, Point(30.0, 0.0), false),
                    Triple(poly2, Point(0.0, 0.0), false),
                    Triple(poly2, Point(60.0, 0.0), false), // 10
                    Triple(poly3, Point(30.0, 0.0), true),
                    Triple(poly3, Point(0.0, 0.0), false),
                    Triple(poly3, Point(60.0, 0.0), false),
                    Triple(poly4, Point(0.0, 20.0), isBoundaryIncluded),
                    Triple(poly4, Point(10.0, 20.0), isBoundaryIncluded),
                    Triple(poly4, Point(50.0, 20.0), isBoundaryIncluded),
                    Triple(poly4, Point(0.0, 10.0), isBoundaryIncluded),
                    Triple(poly4, Point(5.0, 10.0), true),
                    Triple(poly4, Point(25.0, 10.0), true),
                    Triple(poly4, Point(35.0, 10.0), true), // 20
                    Triple(poly4, Point(0.0, 0.0), isBoundaryIncluded),
                    Triple(poly4, Point(20.0, 0.0), false),
                    Triple(poly4, Point(35.0, 0.0), false),
                    Triple(poly4, Point(50.0, 0.0), isBoundaryIncluded),
                    Triple(poly4, Point(50.0, 10.0), isBoundaryIncluded),
                    Triple(poly4, Point(5.0, 0.0), isBoundaryIncluded),
                    Triple(poly4, Point(10.0, 0.0), isBoundaryIncluded),
                    Triple(poly5, Point(20.0, 30.0), isBoundaryIncluded),
                    Triple(poly5, Point(25.0, 25.0), isBoundaryIncluded),
                    Triple(poly5, Point(30.0, 20.0), isBoundaryIncluded), // 30
                    Triple(poly5, Point(25.0, 15.0), isBoundaryIncluded),
                    Triple(poly5, Point(20.0, 10.0), isBoundaryIncluded),
                    Triple(poly5, Point(15.0, 15.0), isBoundaryIncluded),
                    Triple(poly5, Point(10.0, 20.0), isBoundaryIncluded),
                    Triple(poly5, Point(15.0, 25.0), isBoundaryIncluded),
                    Triple(poly5, Point(20.0, 20.0), false),
                )

            val testTitle = "Boundary " + (if (ignoreBoundary) "ignored " else "") + "test number "
            tests.forEachIndexed { i, item ->
                assertEquals(
                    item.first.contains(item.second.coordinates, ignoreBoundary),
                    item.third,
                    testTitle + i,
                )
            }
        }
        runTest(false)
        runTest(true)
    }

    // https://github.com/Turfjs/turf-inside/issues/15
    @Test
    fun testIssue15() {
        val pt1 = Point(-9.9964077, 53.8040989)
        val poly =
            Polygon(
                arrayOf(
                    arrayOf(
                        doubleArrayOf(5.080336744095521, 67.89398938540765),
                        doubleArrayOf(0.35070899909145403, 69.32470003971179),
                        doubleArrayOf(-24.453622256504122, 41.146696777884564),
                        doubleArrayOf(-21.6445524714804, 40.43225902006474),
                        doubleArrayOf(5.080336744095521, 67.89398938540765),
                    )
                )
            )

        assertTrue(poly.contains(pt1.coordinates))
    }
}
