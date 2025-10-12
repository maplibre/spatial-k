package org.maplibre.spatialk.turf.misc

import kotlin.test.Test
import kotlinx.serialization.json.JsonObject
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Geometry
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.testutil.assertPositionEquals
import org.maplibre.spatialk.testutil.readResourceFile

class LineSliceTest {

    @Test
    fun testLineSlice() {
        val features =
            FeatureCollection.fromJson<Geometry?, JsonObject?>(
                readResourceFile("misc/lineSlice/route.json")
            )
        val slice = LineString.fromJson(readResourceFile("misc/lineSlice/slice.json"))

        val (lineString, start, stop) = features.features

        val result =
            (lineString.geometry as LineString).slice(
                (start.geometry as Point).coordinates,
                (stop.geometry as Point).coordinates,
            )
        slice.coordinates.forEachIndexed { i, position ->
            assertPositionEquals(position, result.coordinates[i])
        }
    }
}
