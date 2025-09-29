package org.maplibre.spatialk.turf.misc

import kotlin.test.Test
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.testutil.assertPositionEquals
import org.maplibre.spatialk.testutil.readResourceFile
import org.maplibre.spatialk.turf.ExperimentalTurfApi

@ExperimentalTurfApi
class LineSliceTest {

    @Test
    fun testLineSlice() {
        val features =
            FeatureCollection.Companion.fromJson(readResourceFile("misc/lineSlice/route.json"))
        val slice = LineString.Companion.fromJson(readResourceFile("misc/lineSlice/slice.json"))

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
}
