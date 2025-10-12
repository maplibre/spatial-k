package org.maplibre.spatialk.geojson.dsl

import kotlin.test.Test
import kotlin.test.assertEquals
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Geometry
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.MultiPoint
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Polygon
import org.maplibre.spatialk.geojson.Position

class GeoJsonDslTests {

    private val collectionDsl = buildFeatureCollection {
        val simplePoint = Point(-75.0, 45.0, 100.0)

        // Point
        addFeature(simplePoint) {
            id = "point1"
            properties = mapOf("name" to "Hello World")
        }

        // MultiPoint
        addFeature(
            buildMultiPoint {
                add(simplePoint)
                add(Position(45.0, 45.0))
                add(Position(0.0, 0.0))
            }
        )

        val simpleLine = buildLineString {
            add(Position(45.0, 45.0))
            add(Position(0.0, 0.0))
        }

        // LineString
        addFeature(simpleLine)

        // MultiLineString
        addFeature(
            buildMultiLineString {
                add(simpleLine)
                addLineString {
                    add(Position(44.4, 55.5))
                    add(Position(55.5, 66.6))
                }
            }
        )

        val simplePolygon = buildPolygon {
            addRing {
                add(Position(45.0, 45.0))
                add(Position(0.0, 0.0))
                add(12.0, 12.0)
            }
            addRing {
                add(4.0, 4.0)
                add(2.0, 2.0)
                add(3.0, 3.0)
            }
        }

        // Polygon
        addFeature(simplePolygon)

        addFeature(
            buildMultiPolygon {
                add(simplePolygon)
                addPolygon {
                    addRing {
                        add(12.0, 0.0)
                        add(0.0, 12.0)
                        add(-12.0, 0.0)
                        add(5.0, 5.0)
                    }
                }
            }
        )

        addFeature(
            buildGeometryCollection {
                add(simplePoint)
                add(simpleLine)
                add(simplePolygon)
            }
        )
    }

    private val collectionJson =
        """
        |{"type":"FeatureCollection","features":[
        |{"type":"Feature","geometry":{"type":"Point","coordinates":[-75.0,45.0,100.0]},"properties":{"name":"Hello World"},"id":"point1"},
        |{"type":"Feature","geometry":{"type":"MultiPoint","coordinates":[[-75.0,45.0,100.0],[45.0,45.0],[0.0,0.0]]},"properties":null},
        |{"type":"Feature","geometry":{"type":"LineString","coordinates":[[45.0,45.0],[0.0,0.0]]},"properties":null},
        |{"type":"Feature","geometry":{"type":"MultiLineString","coordinates":[[[45.0,45.0],[0.0,0.0]],[[44.4,55.5],[55.5,66.6]]]},"properties":null},
        |{"type":"Feature","geometry":{"type":"Polygon","coordinates":[[[45.0,45.0],[0.0,0.0],[12.0,12.0],[45.0,45.0]],[[4.0,4.0],[2.0,2.0],[3.0,3.0],[4.0,4.0]]]},"properties":null},
        |{"type":"Feature","geometry":{"type":"MultiPolygon","coordinates":[[[[45.0,45.0],[0.0,0.0],[12.0,12.0],[45.0,45.0]],[[4.0,4.0],[2.0,2.0],[3.0,3.0],[4.0,4.0]]],[[[12.0,0.0],[0.0,12.0],[-12.0,0.0],[5.0,5.0],[12.0,0.0]]]]},"properties":null},
        |{"type":"Feature","geometry":{"type":"GeometryCollection","geometries":[
        |{"type":"Point","coordinates":[-75.0,45.0,100.0]},
        |{"type":"LineString","coordinates":[[45.0,45.0],[0.0,0.0]]},
        |{"type":"Polygon","coordinates":[[[45.0,45.0],[0.0,0.0],[12.0,12.0],[45.0,45.0]],[[4.0,4.0],[2.0,2.0],[3.0,3.0],[4.0,4.0]]]}]},"properties":null}
        |]}"""
            .trimMargin()

    @Test
    fun testDslConstruction() {
        assertEquals(
            FeatureCollection.fromJson<Geometry?, Map<String, String>?>(collectionJson),
            collectionDsl,
        )
    }

    @Test
    fun testNoInlinePositionRequirements() {
        assertEquals(
            MultiPoint(Position(-200.0, 0.0), Position(200.0, 99.0)),
            buildMultiPoint {
                add(-200.0, 0.0)
                add(200.0, 99.0)
            },
        )

        assertEquals(
            LineString(Position(-200.0, 0.0), Position(200.0, 99.0)),
            buildLineString {
                add(-200.0, 0.0)
                add(200.0, 99.0)
            },
        )

        assertEquals(
            Polygon(
                listOf(
                    Position(-200.0, 0.0),
                    Position(200.0, 99.0),
                    Position(500.0, 99.0),
                    Position(-200.0, 0.0),
                )
            ),
            buildPolygon {
                addRing {
                    add(-200.0, 0.0)
                    add(200.0, 99.0)
                    add(500.0, 99.0)
                }
            },
        )
    }
}
