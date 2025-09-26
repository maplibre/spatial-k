package org.maplibre.spatialk.turf

import kotlin.test.Test
import kotlin.test.assertEquals
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.MultiLineString
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.testutil.readResourceFile
import org.maplibre.spatialk.turf.utils.assertDoubleEquals
import org.maplibre.spatialk.turf.utils.assertPositionEquals

@ExperimentalTurfApi
class TurfMiscTest {

    @Test
    fun testLineIntersect() {
        val features =
            FeatureCollection.fromJson(readResourceFile("misc/lineIntersect/twoPoints.json"))
        val intersect =
            lineIntersect(
                features.features[0].geometry as LineString,
                features.features[1].geometry as LineString,
            )

        assertEquals(Position(-120.93653884065287, 51.287945374086675), intersect[0])
    }

    @Test
    fun testLineSlice() {
        val features = FeatureCollection.fromJson(readResourceFile("misc/lineSlice/route.json"))
        val slice = LineString.fromJson(readResourceFile("misc/lineSlice/slice.json"))

        val (lineString, start, stop) = features.features

        val result =
            lineSlice(
                (start.geometry as Point).coordinates,
                (stop.geometry as Point).coordinates,
                lineString.geometry as LineString,
            )
        slice.coordinates.forEachIndexed { i, position ->
            assertPositionEquals(position, result.coordinates[i])
        }
    }

    @Test
    fun testNearestPointOnLine() {
        val (multiLine, point) =
            FeatureCollection.fromJson(readResourceFile("misc/nearestPointOnLine/multiLine.json"))
                .features

        val result =
            nearestPointOnLine(
                multiLine.geometry as MultiLineString,
                (point.geometry as Point).coordinates,
            )
        assertDoubleEquals(123.924613, result.point.longitude, 0.00001)
        assertDoubleEquals(-19.025117, result.point.latitude, 0.00001)
        assertDoubleEquals(120.886021, result.distance, 0.00001)
        assertDoubleEquals(214.548785, result.location, 0.00001)
        assertEquals(0, result.index)
    }
}
