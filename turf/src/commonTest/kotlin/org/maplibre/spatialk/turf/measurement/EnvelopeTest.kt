package org.maplibre.spatialk.turf.measurement

import kotlin.test.Test
import kotlin.test.assertEquals
import org.maplibre.spatialk.geojson.MultiPoint
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.geojson.dsl.addFeature
import org.maplibre.spatialk.geojson.dsl.addRing
import org.maplibre.spatialk.geojson.dsl.buildFeatureCollection
import org.maplibre.spatialk.geojson.dsl.buildLineString
import org.maplibre.spatialk.geojson.dsl.buildPolygon
import org.maplibre.spatialk.turf.coordinatemutation.flattenCoordinates

class EnvelopeTest {

    @Test
    fun envelopeProcessesFeatureCollection() {
        val fc = buildFeatureCollection {
            addFeature(Point(102.0, 0.5), null)
            addFeature(
                buildLineString {
                    add(102.0, -10.0)
                    add(103.0, 1.0)
                    add(104.0, 0.0)
                    add(130.0, 4.0)
                }
            )
            addFeature(
                buildPolygon {
                    addRing {
                        add(102.0, -10.0)
                        add(103.0, 1.0)
                        add(104.0, 0.0)
                        add(130.0, 4.0)
                        add(20.0, 0.0)
                        add(101.0, 0.0)
                        add(101.0, 1.0)
                        add(100.0, 1.0)
                        add(100.0, 0.0)
                        add(102.0, -10.0)
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
