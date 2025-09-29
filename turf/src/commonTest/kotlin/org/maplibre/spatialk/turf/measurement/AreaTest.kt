package org.maplibre.spatialk.turf.measurement

import kotlin.test.Test
import org.maplibre.spatialk.geojson.Polygon
import org.maplibre.spatialk.geojson.dsl.geometryCollection
import org.maplibre.spatialk.testutil.assertDoubleEquals
import org.maplibre.spatialk.testutil.readResourceFile
import org.maplibre.spatialk.turf.ExperimentalTurfApi
import org.maplibre.spatialk.units.inSquareMeters

@ExperimentalTurfApi
class AreaTest {

    @Test
    fun testArea() {
        val geometry = Polygon.fromJson(readResourceFile("measurement/area/polygon.json"))
        assertDoubleEquals(236446.506, area(geometry).inSquareMeters, 0.001, "Single polygon")

        val other = Polygon.fromJson(readResourceFile("measurement/area/other.json"))
        val collection = geometryCollection {
            +geometry
            +other
        }
        assertDoubleEquals(
            4173831.866,
            area(collection).inSquareMeters,
            0.001,
            "Geometry Collection",
        )
    }
}
