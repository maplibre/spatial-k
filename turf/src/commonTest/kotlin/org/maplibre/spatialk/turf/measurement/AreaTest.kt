package org.maplibre.spatialk.turf.measurement

import kotlin.test.Test
import org.maplibre.spatialk.geojson.Polygon
import org.maplibre.spatialk.geojson.dsl.buildGeometryCollection
import org.maplibre.spatialk.testutil.assertDoubleEquals
import org.maplibre.spatialk.testutil.readResourceFile
import org.maplibre.spatialk.units.extensions.inSquareMeters

class AreaTest {

    @Test
    fun testArea() {
        val geometry = Polygon.fromJson(readResourceFile("measurement/area/polygon.json"))
        assertDoubleEquals(236446.506, geometry.area().inSquareMeters, 0.001, "Single polygon")

        val other = Polygon.fromJson(readResourceFile("measurement/area/other.json"))
        val collection = buildGeometryCollection {
            add(geometry)
            add(other)
        }
        assertDoubleEquals(
            4173831.866,
            collection.area().inSquareMeters,
            0.001,
            "Geometry Collection",
        )
    }
}
