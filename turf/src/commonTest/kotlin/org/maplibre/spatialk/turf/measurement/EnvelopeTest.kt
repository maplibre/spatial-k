package org.maplibre.spatialk.turf.measurement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import org.maplibre.spatialk.geojson.Polygon
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.geojson.dsl.featureCollection
import org.maplibre.spatialk.geojson.dsl.lineString
import org.maplibre.spatialk.geojson.dsl.point
import org.maplibre.spatialk.geojson.dsl.polygon

class EnvelopeTest {

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
                            point(102.0, -10.0)
                        }
                    }
            )
        }

        val enveloped = fc.envelope()!!

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
            (enveloped.geometry!!).coordinates,
            "positions should be correct",
        )
    }
}
