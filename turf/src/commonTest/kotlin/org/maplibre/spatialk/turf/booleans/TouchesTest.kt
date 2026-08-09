package org.maplibre.spatialk.turf.booleans

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.serialization.json.JsonObject
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Geometry
import org.maplibre.spatialk.geojson.GeometryCollection
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.MultiLineString
import org.maplibre.spatialk.geojson.MultiPoint
import org.maplibre.spatialk.geojson.MultiPolygon
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Polygon
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.testutil.readResourceFile

class TouchesTest {

    @Test
    fun testPointTrue() {
        listOf(
                "booleans/touches/true/Point/LineString/PointOnEndLine.geojson",
                "booleans/touches/true/Point/LineString/PointOnStartLine.geojson",
                "booleans/touches/true/Point/MultiLineString/MpOnEndLine.geojson",
                "booleans/touches/true/Point/MultiLineString/MpOnStartLine.geojson",
                "booleans/touches/true/Point/MultiPolygon/PointTouchesMultipolygon.geojson",
                "booleans/touches/true/Point/MultiPolygon/PointTouchesMultipolygonHole.geojson",
                "booleans/touches/true/Point/Polygon/PointOnEdgePolygon.geojson",
                "booleans/touches/true/Point/Polygon/PointOnHole.geojson",
                "booleans/touches/true/Point/Polygon/PointOnVerticePolygon.geojson",
            )
            .forEach { path ->
                val (point, other) =
                    FeatureCollection.fromJson<Geometry?, JsonObject?>(readResourceFile(path))
                        .features
                        .map { it.geometry }

                assertTrue(
                    touches(point!!, other!!),
                    "assertion failed for path $path",
                )
            }
    }

    @Test
    fun testPointFalse() {
        listOf(
                "booleans/touches/false/Point/LineString/PointIsNotTouchLine.geojson",
                "booleans/touches/false/Point/LineString/PointOnMidLinestring.geojson",
                "booleans/touches/false/Point/MultiLineString/MpNotTouchMidLineString.geojson",
                "booleans/touches/false/Point/MultiLineString/MpOnMidLineString.geojson",
                "booleans/touches/false/Point/MultiPolygon/PointNotTouchMultipolygon.geojson",
                "booleans/touches/false/Point/Polygon/PointDoesNotTouchPolygon.geojson",
                "booleans/touches/false/Point/Polygon/PointInsidePolygon.geojson",
            )
            .forEach { path ->
                val (point, other) =
                    FeatureCollection.fromJson<Geometry?, JsonObject?>(readResourceFile(path))
                        .features
                        .map { it.geometry }

                assertFalse(
                    touches(point!!, other!!),
                    "assertion failed for path $path",
                )
            }
    }

    @Test
    fun testMultiPointTrue() {
        listOf(
                "booleans/touches/true/MultiPoint/MultiPolygon/multipoint-touches-multipolygon.geojson",
                "booleans/touches/true/MultiPoint/MultiLineString/MpTouchesSecondMultiLine.geojson",
                "booleans/touches/true/MultiPoint/LineString/MultipointTouchesLine.geojson",
                "booleans/touches/true/MultiPoint/MultiLineString/MpTouchesEndMultiLine.geojson",
                "booleans/touches/true/MultiPoint/Polygon/MultiPointIsWithinPolygon.geojson",
            )
            .forEach { path ->
                val (multiPoint, other) =
                    FeatureCollection.fromJson<Geometry?, JsonObject?>(readResourceFile(path))
                        .features
                        .map { it.geometry }

                assertTrue(
                    touches(multiPoint!!, other!!),
                    "assertion failed for path $path",
                )
            }
    }

    @Test
    fun testMultiPointFalse() {
        listOf(
                "booleans/touches/false/MultiPoint/LineString/MultipointDoesNotTouchLine.geojson",
                "booleans/touches/false/MultiPoint/LineString/MultiPointTouchesInsideLine.geojson",
                "booleans/touches/false/MultiPoint/MultiLineString/MpDoesNotTouchMultiLine.geojson",
                "booleans/touches/false/MultiPoint/MultiLineString/MpTouchesInternalMultiLine.geojson",
                "booleans/touches/false/MultiPoint/Polygon/MultiPointInsidePolygon.geojson",
                "booleans/touches/false/MultiPoint/Polygon/MultiPointNoTouchPolygon.geojson",
                "booleans/touches/false/MultiPoint/MultiPolygon/multipoint-inside-multipolygon.geojson",
                "booleans/touches/false/MultiPoint/MultiPolygon/MultiPointDoesNotTouchMultipolygon.geojson",
            )
            .forEach { path ->
                val (multiPoint, other) =
                    FeatureCollection.fromJson<Geometry?, JsonObject?>(readResourceFile(path))
                        .features
                        .map { it.geometry }

                assertFalse(
                    touches(multiPoint!!, other!!),
                    "assertion failed for path $path",
                )
            }
    }

    @Test
    fun testLineStringTrue() {
        listOf(
                "booleans/touches/true/LineString/LineString/LineTouchesEndpoint.geojson",
                "booleans/touches/true/LineString/MultiLineString/LineStringTouchesEnd.geojson",
                "booleans/touches/true/LineString/MultiLineString/LineStringTouchesStart.geojson",
                "booleans/touches/true/LineString/MultiPoint/MultipointTouchesLine.geojson",
                "booleans/touches/true/LineString/MultiPolygon/LineTouchesMultiPoly.geojson",
                "booleans/touches/true/LineString/MultiPolygon/LineTouchesSecondMultiPoly.geojson",
                "booleans/touches/true/LineString/Polygon/LineTouchesPolygon.geojson",
            )
            .forEach { path ->
                val (lineString, other) =
                    FeatureCollection.fromJson<Geometry?, JsonObject?>(readResourceFile(path))
                        .features
                        .map { it.geometry }

                assertTrue(
                    touches(lineString!!, other!!),
                    "assertion failed for path $path",
                )
            }
    }

    @Test
    fun testLineStringFalse() {
        listOf(
                "booleans/touches/false/LineString/LineString/LinesExactSame.geojson",
                "booleans/touches/false/LineString/LineString/LivesOverlap.geojson",
                "booleans/touches/false/LineString/MultiLineString/LineStringOverlapsMultiLinestring.geojson",
                "booleans/touches/false/LineString/MultiLineString/LineStringSameAsMultiLinestring.geojson",
                "booleans/touches/false/LineString/MultiPoint/LineStringDoesNotTouchMP.geojson",
                "booleans/touches/false/LineString/MultiPoint/LineStringTouchesMultiPointButInternal.geojson",
                "booleans/touches/false/LineString/MultiPolygon/LineDoesNotTouchMultiPoly.geojson",
                "booleans/touches/false/LineString/Polygon/LineCrossesPolygon.geojson",
                "booleans/touches/false/LineString/Polygon/LineDoesNotTouch.geojson",
                "booleans/touches/false/LineString/Polygon/LineWIthinPolygon.geojson",
            )
            .forEach { path ->
                val (lineString, other) =
                    FeatureCollection.fromJson<Geometry?, JsonObject?>(readResourceFile(path))
                        .features
                        .map { it.geometry }

                assertFalse(
                    touches(lineString!!, other!!),
                    "assertion failed for path $path",
                )
            }
    }

    @Test
    fun testMultiLineStringTrue() {
        listOf(
                "booleans/touches/true/MultiLineString/LineString/MultiLineTouchesLine.geojson",
                "booleans/touches/true/MultiLineString/MultiLineString/MultiLineTouchesMultiLine.geojson",
                "booleans/touches/true/MultiLineString/MultiPoint/MultiLineTouchesMultiPoint.geojson",
                "booleans/touches/true/MultiLineString/Point/MultiLineTouchesPoint.geojson",
                "booleans/touches/true/MultiLineString/Polygon/MultiLineTouchesPolygon.geojson",
            )
            .forEach { path ->
                val (multiLineString, other) =
                    FeatureCollection.fromJson<Geometry?, JsonObject?>(readResourceFile(path))
                        .features
                        .map { it.geometry }

                assertTrue(
                    touches(multiLineString!!, other!!),
                    "assertion failed for path $path",
                )
            }
    }

    @Test
    fun testMultiLineStringFalse() {
        listOf(
                "booleans/touches/false/MultiLineString/LineString/MultiLineStringOverlapsLine.geojson",
                "booleans/touches/false/MultiLineString/LineString/MultiLineStringSameAsLine.geojson",
                "booleans/touches/false/MultiLineString/MultiLineString/MultiLineStringsOverlap.geojson",
                "booleans/touches/false/MultiLineString/MultiLineString/MultiLineStringsSame.geojson",
                "booleans/touches/false/MultiLineString/MultiPoint/MpTouchesInternalMultiline.geojson",
                "booleans/touches/false/MultiLineString/MultiPoint/MultiPointNotTouchMultiline.geojson",
                "booleans/touches/false/MultiLineString/MultiPolygon/MultiLineInsideMultipoly.geojson",
                "booleans/touches/false/MultiLineString/Point/PointNotTouchMultiLinestring.geojson",
                "booleans/touches/false/MultiLineString/Point/PointTouchesMidLineString.geojson",
                "booleans/touches/false/MultiLineString/Polygon/MultiLineInsidePoly.geojson",
                "booleans/touches/false/MultiLineString/Polygon/MultiLineNotTouchPoly.geojson",
            )
            .forEach { path ->
                val (multiLineString, other) =
                    FeatureCollection.fromJson<Geometry?, JsonObject?>(readResourceFile(path))
                        .features
                        .map { it.geometry }

                assertFalse(
                    touches(multiLineString!!, other!!),
                    "assertion failed for path $path",
                )
            }
    }

    @Test
    fun testPolygonTrue() {
        listOf(
                "booleans/touches/true/Polygon/LineString/PolygonTouchesLines.geojson",
                "booleans/touches/true/Polygon/MultiLineString/PolygonTouchesMultiline.geojson",
                "booleans/touches/true/Polygon/MultiPoint/PolygonTouchesMultiPoint.geojson",
                "booleans/touches/true/Polygon/MultiPolygon/PolyTouchMultiPolys.geojson",
                "booleans/touches/true/Polygon/Point/PolygonTouchesPoint.geojson",
                "booleans/touches/true/Polygon/Point/PolygonTouchesPointVertice.geojson",
                "booleans/touches/true/Polygon/Polygon/PolygonsTouchVertices.geojson",
                "booleans/touches/true/Polygon/Polygon/PolygonTouchesEdges.geojson",
            )
            .forEach { path ->
                val (multiLineString, other) =
                    FeatureCollection.fromJson<Geometry?, JsonObject?>(readResourceFile(path))
                        .features
                        .map { it.geometry }

                assertTrue(
                    touches(multiLineString!!, other!!),
                    "assertion failed for path $path",
                )
            }
    }

    @Test
    fun testPolygonFalse() {
        listOf(
                "booleans/touches/false/Polygon/LineString/PolyDoesNotTouchLine.geojson",
                "booleans/touches/false/Polygon/MultiLineString/PolyNotTouchMultiLine.geojson",
                "booleans/touches/false/Polygon/MultiLineString/PolyOverlapMultiLine.geojson",
                "booleans/touches/false/Polygon/MultiPoint/PolygonNoTouchMultiPoint.geojson",
                "booleans/touches/false/Polygon/MultiPoint/PolygonOverlapsMultiPoint.geojson",
                "booleans/touches/false/Polygon/MultiPolygon/PolyNotTouchMultipoly.geojson",
                "booleans/touches/false/Polygon/Point/PolygonDoesNotTouchPoint.geojson",
                "booleans/touches/false/Polygon/Point/PolygonOverlapsPoint.geojson",
                "booleans/touches/false/Polygon/Polygon/PolygonsDontTouch.geojson",
                "booleans/touches/false/Polygon/Polygon/PolygonsOverlap.geojson",
            )
            .forEach { path ->
                val (multiLineString, other) =
                    FeatureCollection.fromJson<Geometry?, JsonObject?>(readResourceFile(path))
                        .features
                        .map { it.geometry }

                assertFalse(
                    touches(multiLineString!!, other!!),
                    "assertion failed for path $path",
                )
            }
    }

    @Test
    fun testMultiPolygonTrue() {
        listOf(
                "booleans/touches/true/MultiPolygon/MultiLineString/MultiLineTouchesMultiPoly.geojson",
                "booleans/touches/true/MultiPolygon/MultiPoint/MultiPolyTouchesMultiPoint.geojson",
                "booleans/touches/true/MultiPolygon/MultiPolygon/MultiPolyTouchesMultiPoly.geojson",
                "booleans/touches/true/MultiPolygon/Point/MpTouchesPoint.geojson",
                "booleans/touches/true/MultiPolygon/Polygon/MultiPolyTouchesPoly.geojson",
            )
            .forEach { path ->
                val (multiPolygon, other) =
                    FeatureCollection.fromJson<Geometry?, JsonObject?>(readResourceFile(path))
                        .features
                        .map { it.geometry }

                assertTrue(
                    touches(multiPolygon!!, other!!),
                    "assertion failed for path $path",
                )
            }
    }

    @Test
    fun testMultiPolygonFalse() {
        listOf(
                "booleans/touches/false/MultiPolygon/LineString/MultiPolyNotTouchLineString.geojson",
                "booleans/touches/false/MultiPolygon/MultiLineString/MultiPolyOverlapsMultiLine.geojson",
                "booleans/touches/false/MultiPolygon/MultiPoint/MultiPolyNotTouchMultiPoint.geojson",
                "booleans/touches/false/MultiPolygon/MultiPolygon/MultiPolysDoNotTouch.geojson",
                "booleans/touches/false/MultiPolygon/MultiPolygon/MultiPolysOverlap.geojson",
                "booleans/touches/false/MultiPolygon/Point/MultiPolyNotTouchPoint.geojson",
            )
            .forEach { path ->
                val (multiPolygon, other) =
                    FeatureCollection.fromJson<Geometry?, JsonObject?>(readResourceFile(path))
                        .features
                        .map { it.geometry }

                assertFalse(
                    touches(multiPolygon!!, other!!),
                    "assertion failed for path $path",
                )
            }
    }

    @Test
    fun testGeometryCollection() {
        val point = Point(0.1, 0.2)
        val multiPoint = MultiPoint(point, point)
        val lineString = LineString(point, point)
        val multiLineString = MultiLineString(lineString)
        val polygon =
            Polygon(
                listOf(
                    listOf(
                        point.coordinates,
                        Point(0.2, 0.1).coordinates,
                        Point(0.3, 0.4).coordinates,
                        point.coordinates,
                    )
                )
            )
        val multiPolygon = MultiPolygon(polygon)
        val geometryCollection =
            GeometryCollection(
                point,
                multiPoint,
                lineString,
                multiLineString,
                polygon,
                multiPolygon,
            )
        assertFailsWith<IllegalStateException> { touches(geometryCollection, geometryCollection) }
        assertFailsWith<IllegalStateException> { touches(point, geometryCollection) }
        assertFailsWith<IllegalStateException> { touches(multiPoint, geometryCollection) }
        assertFailsWith<IllegalStateException> { touches(lineString, geometryCollection) }
        assertFailsWith<IllegalStateException> { touches(multiLineString, geometryCollection) }
        assertFailsWith<IllegalStateException> { touches(polygon, geometryCollection) }
        assertFailsWith<IllegalStateException> { touches(multiPolygon, geometryCollection) }
    }

    // The LineString vs MultiPoint fold used to be order-dependent: an interior point followed by
    // an endpoint returned true, but an interior point anywhere must make touches false (Turf.js
    // semantics). Regression test for the order-independence fix.
    @Test
    fun testLineStringTouchesMultiPointInteriorBeforeEndReturnsFalse() {
        val line = LineString(Position(0.0, 0.0), Position(0.0, 10.0))
        val multiPoint = MultiPoint(Position(0.0, 5.0), Position(0.0, 0.0))
        assertFalse(touches(line, multiPoint))
    }

    @Test
    fun testLineStringTouchesMultiPointEndBeforeInteriorReturnsFalse() {
        val line = LineString(Position(0.0, 0.0), Position(0.0, 10.0))
        val multiPoint = MultiPoint(Position(0.0, 0.0), Position(0.0, 5.0))
        assertFalse(touches(line, multiPoint))
    }

    @Test
    fun testLineStringTouchesMultiPointOnlyEndsReturnsTrue() {
        val line = LineString(Position(0.0, 0.0), Position(0.0, 10.0))
        val multiPoint = MultiPoint(Position(0.0, 10.0), Position(10.0, 10.0))
        assertTrue(touches(line, multiPoint))
    }

    // Same order-dependence in LineString vs Polygon: a vertex strictly inside the polygon must
    // make touches false regardless of coordinate order.
    @Test
    fun testLineStringTouchesPolygonInteriorVertexBeforeRingVertexReturnsFalse() {
        val line = LineString(Position(5.0, 5.0), Position(0.0, 5.0))
        val polygon =
            Polygon(
                listOf(
                    listOf(
                        Position(0.0, 0.0),
                        Position(0.0, 10.0),
                        Position(10.0, 10.0),
                        Position(10.0, 0.0),
                        Position(0.0, 0.0),
                    )
                )
            )
        assertFalse(touches(line, polygon))
    }

    @Test
    fun testLineStringTouchesPolygonRingVertexBeforeInteriorVertexReturnsFalse() {
        val line = LineString(Position(0.0, 5.0), Position(5.0, 5.0))
        val polygon =
            Polygon(
                listOf(
                    listOf(
                        Position(0.0, 0.0),
                        Position(0.0, 10.0),
                        Position(10.0, 10.0),
                        Position(10.0, 0.0),
                        Position(0.0, 0.0),
                    )
                )
            )
        assertFalse(touches(line, polygon))
    }

    @Test
    fun testLineStringTouchesPolygonOnlyRingVertexReturnsTrue() {
        val line = LineString(Position(0.0, 5.0), Position(-10.0, 5.0))
        val polygon =
            Polygon(
                listOf(
                    listOf(
                        Position(0.0, 0.0),
                        Position(0.0, 10.0),
                        Position(10.0, 10.0),
                        Position(10.0, 0.0),
                        Position(0.0, 0.0),
                    )
                )
            )
        assertTrue(touches(line, polygon))
    }

    // Kotlin-only branches not covered by the GeoKJSON fixtures: Point/Point,
    // MultiPoint/MultiPoint,
    // and Point/MultiPoint.
    // Greptile review: the MultiPolygon/MultiLineString branch checked interior points against the
    // first polygon instead of the polygon being tested. A line touching one component while a
    // different line enters the interior of another component must not be reported as touching.
    @Test
    fun testMultiPolygonTouchesMultiLineStringEnteringSecondComponentReturnsFalse() {
        val multiPolygon =
            MultiPolygon(
                listOf(
                    listOf(
                        listOf(
                            Position(0.0, 0.0),
                            Position(0.0, 10.0),
                            Position(10.0, 10.0),
                            Position(10.0, 0.0),
                            Position(0.0, 0.0),
                        )
                    ),
                    listOf(
                        listOf(
                            Position(20.0, 20.0),
                            Position(20.0, 30.0),
                            Position(30.0, 30.0),
                            Position(30.0, 20.0),
                            Position(20.0, 20.0),
                        )
                    ),
                )
            )
        val multiLineString =
            MultiLineString(
                listOf(
                    listOf(Position(0.0, 5.0), Position(0.0, -5.0)),
                    listOf(Position(25.0, 25.0), Position(25.0, 29.0)),
                )
            )
        assertFalse(touches(multiPolygon, multiLineString))
    }

    @Test
    fun testPointTouchesPoint() {
        assertTrue(touches(Point(1.0, 2.0), Point(1.0, 2.0)))
        assertFalse(touches(Point(1.0, 2.0), Point(3.0, 4.0)))
    }

    @Test
    fun testPointTouchesMultiPoint() {
        assertTrue(touches(Point(1.0, 2.0), MultiPoint(Position(3.0, 4.0), Position(1.0, 2.0))))
        assertFalse(touches(Point(1.0, 2.0), MultiPoint(Position(3.0, 4.0), Position(5.0, 6.0))))
    }

    @Test
    fun testMultiPointTouchesMultiPoint() {
        val shared = MultiPoint(Position(1.0, 2.0), Position(3.0, 4.0))
        val other = MultiPoint(Position(3.0, 4.0), Position(5.0, 6.0))
        assertTrue(touches(shared, other))
        assertFalse(touches(shared, MultiPoint(Position(7.0, 8.0), Position(9.0, 10.0))))
    }

    // LineString vs MultiLineString: a component overlapping the line's interior makes touches
    // false even if another component only touches an endpoint. The endpoint check must not
    // short-circuit before the overlap is considered.
    @Test
    fun testLineStringTouchesMultiLineStringWithOverlappingComponentReturnsFalse() {
        val line = LineString(Position(0.0, 0.0), Position(2.0, 0.0))
        val multiLineString =
            MultiLineString(
                listOf(
                    listOf(Position(0.0, 0.0), Position(0.0, 1.0)),
                    listOf(Position(0.5, 0.0), Position(1.5, 0.0)),
                )
            )
        assertFalse(touches(line, multiLineString))
    }

    @Test
    fun testLineStringTouchesMultiLineStringOnlyEndpointTouchesReturnsTrue() {
        val line = LineString(Position(0.0, 0.0), Position(2.0, 0.0))
        val multiLineString =
            MultiLineString(
                listOf(
                    listOf(Position(0.0, 0.0), Position(0.0, 1.0)),
                    listOf(Position(-1.0, 0.0), Position(-2.0, 0.0)),
                )
            )
        assertTrue(touches(line, multiLineString))
    }

    // Endpoint contact is two-dimensional: the optional altitude must not break a touch that the
    // other predicates (pointOnLine, polygon contains) would accept.
    @Test
    fun testPointTouchesLineEndWithDifferentAltitude() {
        val line = LineString(Position(0.0, 0.0, 20.0), Position(1.0, 0.0, 20.0))
        assertTrue(touches(Point(Position(0.0, 0.0, 10.0)), line))
        assertTrue(touches(Point(Position(1.0, 0.0, 10.0)), line))
        assertFalse(
            touches(
                Point(Position(0.0, 0.0, 10.0)),
                LineString(Position(0.0, 1.0), Position(0.0, 2.0)),
            )
        )
    }
}
