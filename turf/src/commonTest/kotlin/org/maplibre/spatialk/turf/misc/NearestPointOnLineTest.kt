package org.maplibre.spatialk.turf.misc

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.json.JsonObject
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Geometry
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.LineStringGeometry
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.geojson.dsl.lineStringOf
import org.maplibre.spatialk.testutil.assertDoubleEquals
import org.maplibre.spatialk.testutil.assertPositionEquals
import org.maplibre.spatialk.testutil.readResourceFile
import org.maplibre.spatialk.units.extensions.inKilometers

class NearestPointOnLineTest {

    @Test
    fun testNearestPointOnLineMultiLine() {
        val fc =
            FeatureCollection.fromJson<Geometry?, JsonObject?>(
                readResourceFile("misc/nearestPointOnLine/multiLine.json")
            )

        val multiline = fc[0].geometry as LineStringGeometry
        val point = fc[1].geometry as Point
        val (nearestPoint, props) = multiline.nearestPointTo(point.coordinates)

        assertDoubleEquals(123.924613, nearestPoint.longitude)
        assertDoubleEquals(-19.025117, nearestPoint.latitude)
        assertDoubleEquals(120.886021, props.distance.inKilometers)
        assertDoubleEquals(214.548785, props.location.inKilometers)
        assertEquals(0, props.index)
    }

    @Test
    fun testNearestPointOnLineFromTurfJs() {
        val fc =
            FeatureCollection.fromJson<Geometry?, JsonObject?>(
                readResourceFile("misc/nearestPointOnLine/line1.json")
            )

        val line = fc[0].geometry as LineString
        val point = fc[1].geometry as Point
        val (nearestPoint, props) = line.nearestPointTo(point.coordinates)

        assertDoubleEquals(-97.856502, nearestPoint.longitude)
        assertDoubleEquals(22.270017, nearestPoint.latitude)
        assertDoubleEquals(2.556271, props.distance.inKilometers, epsilon = 0.001)
        assertDoubleEquals(22.137494, props.location.inKilometers, epsilon = 0.01)
        assertEquals(1, props.index)
    }

    @Test
    fun testNearestPointOnLineAtStartVertex() {
        val line =
            lineStringOf(
                Position(-122.457175, 37.720033),
                Position(-122.457175, 37.718242),
            )
        val target = Position(-122.457175, 37.720033)

        val (nearestPoint, props) = line.nearestPointTo(target)

        assertPositionEquals(target, nearestPoint.coordinates)
        assertDoubleEquals(0.0, props.location.inKilometers)
        assertDoubleEquals(0.0, props.distance.inKilometers)
    }

    @Test
    fun testNearestPointOnLinePointOnVertex() {
        val line =
            lineStringOf(
                Position(-122.456161, 37.721259),
                Position(-122.457175, 37.720033),
                Position(-122.457175, 37.718242),
            )
        val target = Position(-122.457175, 37.720033)

        val (nearestPoint, props) = line.nearestPointTo(target)

        assertPositionEquals(target, nearestPoint.coordinates)
        assertDoubleEquals(0.0, props.distance.inKilometers)
    }

    @Test
    fun testNearestPointOnLineDuplicateVertices() {
        // From turf.js issue #2808: redundant consecutive vertices must not break snapping.
        val line =
            lineStringOf(
                Position(10.57846, 49.8463959),
                Position(10.57846, 49.8468386),
                Position(10.57846, 49.8468386),
                Position(10.57846, 49.8468386),
                Position(10.57846, 49.8472814),
                Position(10.57846, 49.8472814),
            )
        val target = Position(10.57846, 49.8468386)

        val (_, props) = line.nearestPointTo(target)

        assertDoubleEquals(0.0, props.distance.inKilometers)
    }
}
