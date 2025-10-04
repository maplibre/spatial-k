package org.maplibre.spatialk.turf.measurement

import kotlin.test.Test
import kotlin.test.assertEquals
import org.maplibre.spatialk.geojson.MultiPoint
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.geojson.dsl.featureCollection
import org.maplibre.spatialk.geojson.dsl.lineString
import org.maplibre.spatialk.geojson.dsl.point
import org.maplibre.spatialk.geojson.dsl.polygon
import org.maplibre.spatialk.turf.meta.flattenCoordinates

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

        val envelope = MultiPoint(fc.flattenCoordinates()).envelope()

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
            envelope.coordinates,
            "positions should be correct",
        )
    }
}
