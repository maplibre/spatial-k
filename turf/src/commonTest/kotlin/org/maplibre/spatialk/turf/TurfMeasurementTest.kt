@file:Suppress("MagicNumber")

package org.maplibre.spatialk.turf

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertIs
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.MultiLineString
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Polygon
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.geojson.dsl.featureCollection
import org.maplibre.spatialk.geojson.dsl.geometryCollection
import org.maplibre.spatialk.geojson.dsl.lineString
import org.maplibre.spatialk.geojson.dsl.point
import org.maplibre.spatialk.geojson.dsl.polygon
import org.maplibre.spatialk.testutil.readResourceFile
import org.maplibre.spatialk.turf.utils.assertDoubleEquals

@ExperimentalTurfApi
class TurfMeasurementTest {

    @Test
    fun testAlong() {
        val geometry = LineString.fromJson(readResourceFile("measurement/along/lineString.json"))

        assertEquals(Position(-79.4179672644524, 43.636029126566484), along(geometry, 1.0))
        assertEquals(Position(-79.39973865844715, 43.63797943080659), along(geometry, 2.5))
        assertEquals(Position(-79.37493324279785, 43.64470906117713), along(geometry, 100.0))
        assertEquals(geometry.coordinates.last(), along(geometry, 100.0))
    }

    @Test
    fun testArea() {
        val geometry = Polygon.fromJson(readResourceFile("measurement/area/polygon.json"))
        assertDoubleEquals(236446.506, area(geometry), 0.001, "Single polygon")

        val other = Polygon.fromJson(readResourceFile("measurement/area/other.json"))
        val collection = geometryCollection {
            +geometry
            +other
        }
        assertDoubleEquals(4173831.866, area(collection), 0.001, "Geometry Collection")
    }

    @Test
    fun testBbox() {
        val point = Point.fromJson(readResourceFile("measurement/bbox/point.json"))
        assertEquals(BoundingBox(point.coordinates, point.coordinates), bbox(point))

        val lineString = LineString.fromJson(readResourceFile("measurement/bbox/lineString.json"))
        assertEquals(
            BoundingBox(-79.376220703125, 43.65197548731187, -73.58642578125, 45.4986468234261),
            bbox(lineString),
        )

        val polygon = Polygon.fromJson(readResourceFile("measurement/bbox/polygon.json"))
        assertEquals(
            BoundingBox(-64.44580078125, 45.9511496866914, -61.973876953125, 47.07012182383309),
            bbox(polygon),
        )
    }

    @Test
    fun testBboxPolygon() {
        val bbox = BoundingBox(12.1, 34.3, 56.5, 78.7)

        val polygon = polygon {
            ring {
                +Position(12.1, 34.3)
                +Position(56.5, 34.3)
                +Position(56.5, 78.7)
                +Position(12.1, 78.7)
                complete()
            }
        }

        assertEquals(polygon, bboxPolygon(bbox))
    }

    @Test
    fun testBearing() {
        val start = Position(-75.0, 45.0)
        val end = Position(20.0, 60.0)

        assertDoubleEquals(37.75, bearing(start, end), 0.01, "Initial Bearing")
        assertDoubleEquals(120.01, bearing(start, end, final = true), 0.01, "Final Bearing")
    }

    @Test
    fun testDestination() {
        val point0 = Position(-75.0, 38.10096062273525)
        val (longitude, latitude) = destination(point0, 100.0, 0.0)

        assertDoubleEquals(-75.0, longitude, 0.1)
        assertDoubleEquals(39.000281, latitude, 0.000001)
    }

    @Test
    fun testDistance() {
        val a = Position(-73.67, 45.48)
        val b = Position(-79.48, 43.68)

        assertEquals(501.64563403765925, distance(a, b))
    }

    @Test
    fun testLength() {
        val geometry = LineString.fromJson(readResourceFile("measurement/length/lineString.json"))

        assertEquals(42.560767589197006, length(geometry, Units.Kilometers))
    }

    @Test
    fun testMidpoint() {
        val point1 = Position(-79.3801, 43.6463)
        val point2 = Position(-74.0071, 40.7113)

        val midpoint = midpoint(point1, point2)

        assertDoubleEquals(-76.6311, midpoint.longitude, 0.0001)
        assertDoubleEquals(42.2101, midpoint.latitude, 0.0001)
    }

    @Test
    fun testCenterFromFeature() {
        val geometry = Polygon.fromJson(readResourceFile("measurement/area/other.json"))

        val centerPoint = center(Feature(geometry))

        assertDoubleEquals(-75.71805238723755, centerPoint.coordinates.longitude, 0.0001)
        assertDoubleEquals(45.3811030151199, centerPoint.coordinates.latitude, 0.0001)
    }

    @Test
    fun testCenterFromGeometry() {
        val geometry = Polygon.fromJson(readResourceFile("measurement/area/other.json"))

        val centerPoint = center(geometry)

        assertDoubleEquals(-75.71805238723755, centerPoint.coordinates.longitude, 0.0001)
        assertDoubleEquals(45.3811030151199, centerPoint.coordinates.latitude, 0.0001)
    }

    @Test
    fun testGreatCircleBasic() {
        val startLon = -122.0
        val startLat = 48.0
        val endLon = 28.125
        val endLat = 43.32517767999296

        val start = Position(startLon, startLat)
        val end = Position(endLon, endLat)

        val greatCircle = greatCircle(start, end, pointCount = 99)

        assertEquals(99, greatCircle.coordAll().size)
        assertIs<LineString>(greatCircle)
    }

    @Test
    fun testGreatCirclePassesAntimeridian() {
        val startLon = -122.349358
        val startLat = 47.620422
        val endLon = 77.036560
        val endLat = 38.897957

        val start = Position(startLon, startLat)
        val end = Position(endLon, endLat)
        val geometry = greatCircle(start, end, pointCount = 100)
        assertIs<MultiLineString>(geometry)
    }

    @Test
    fun testGreatCircleAntipodals() {
        val startLat = 47.620422

        val start = Position(-122.349358, startLat)
        val antipodal = Position(106.33, startLat)

        assertFails { greatCircle(start, antipodal) }
    }

    @Test
    fun envelopeProcessesFeatureCollection() {
        val fc = featureCollection {
            feature(geometry = point(102.0, 0.5))
            feature(
                geometry =
                    lineString {
                        point(102.0, -10.0)
                        point(103.0, 1.0)
                        point(104.0, 0.0)
                        point(130.0, 4.0)
                    }
            )
            feature(
                geometry =
                    polygon {
                        ring {
                            point(102.0, -10.0)
                            point(103.0, 1.0)
                            point(104.0, 0.0)
                            point(130.0, 4.0)
                            point(20.0, 0.0)
                            point(101.0, 0.0)
                            point(101.0, 1.0)
                            point(100.0, 1.0)
                            point(100.0, 0.0)
                        }
                    }
            )
        }

        val enveloped = envelope(fc)

        assertIs<Polygon>(enveloped.geometry, "geometry type should be Polygon")
        assertEquals(
            listOf(
                listOf(
                    Position(20.0, -10.0),
                    Position(130.0, -10.0),
                    Position(130.0, 4.0),
                    Position(20.0, 4.0),
                    Position(20.0, -10.0),
                )
            ),
            (enveloped.geometry as Polygon).coordinates,
            "positions should be correct",
        )
    }

    fun testPointToLineDistance() {
        val point = Position(-0.54931640625, 0.7470491450051796)
        val line =
            LineString(
                Position(1.0, 3.0),
                Position(2.0, 2.0),
                Position(2.0, 0.0),
                Position(-1.5, -1.5),
            )

        val distance = pointToLineDistance(point, line)
        assertDoubleEquals(188.01568693725255, distance, 0.000001)
    }

    @Test
    fun testRhumbDistance() {
        val distance =
            rhumbDistance(Position(-75.343, 39.984), Position(-75.534, 39.123), Units.Kilometers)

        assertDoubleEquals(97.12923942772163, distance, 0.000001)
    }
}
