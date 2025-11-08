package org.maplibre.spatialk.turf.measurement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Polygon
import org.maplibre.spatialk.geojson.dsl.addFeature
import org.maplibre.spatialk.geojson.dsl.addLineString
import org.maplibre.spatialk.geojson.dsl.addPolygon
import org.maplibre.spatialk.geojson.dsl.addRing
import org.maplibre.spatialk.geojson.dsl.buildFeatureCollection
import org.maplibre.spatialk.geojson.dsl.buildLineString
import org.maplibre.spatialk.geojson.dsl.buildMultiLineString
import org.maplibre.spatialk.geojson.dsl.buildMultiPolygon
import org.maplibre.spatialk.geojson.dsl.buildPolygon
import org.maplibre.spatialk.geojson.dsl.featureCollectionOf
import org.maplibre.spatialk.testutil.readResourceFile

private val point = Point(102.0, 0.5)
private val line = buildLineString {
    add(102.0, -10.0)
    add(103.0, 1.0)
    add(104.0, 0.0)
    add(130.0, 4.0)
}
private val polygon = buildPolygon {
    addRing {
        add(101.0, 0.0)
        add(101.0, 1.0)
        add(100.0, 1.0)
        add(100.0, 0.0)
    }
}
private val multiLine = buildMultiLineString {
    addLineString {
        add(100.0, 0.0)
        add(101.0, 1.0)
    }
    addLineString {
        add(102.0, 2.0)
        add(103.0, 3.0)
    }
}
private val multiPolygon = buildMultiPolygon {
    addPolygon {
        addRing {
            add(102.0, 2.0)
            add(103.0, 2.0)
            add(103.0, 3.0)
            add(102.0, 3.0)
        }
    }
    addPolygon {
        addRing {
            add(100.0, 0.0)
            add(101.0, 0.0)
            add(101.0, 1.0)
            add(100.0, 1.0)
        }
        addRing {
            add(100.2, 0.2)
            add(101.8, 0.2)
            add(101.8, 0.8)
            add(100.2, 0.8)
        }
    }
}

private val featureCollection = buildFeatureCollection {
    addFeature(point, null)
    addFeature(line)
    addFeature(polygon)
    addFeature(multiLine)
    addFeature(multiPolygon)
}

class BboxTest {

    @Test
    fun testEmptyFeatures() {
        assertNull(Feature(null, null).withComputedBbox().bbox)
        assertNull(featureCollectionOf().withComputedBbox().bbox)
    }

    @Test
    fun testFeatureCollectionBbox() {
        assertEquals(
            BoundingBox(100.0, -10.0, 130.0, 4.0),
            featureCollection.withComputedBbox().bbox,
        )
    }

    @Test
    fun testPointBbox() {
        assertEquals(BoundingBox(102.0, 0.5, 102.0, 0.5), point.computeBbox())
        assertEquals(point.computeBbox(), point.withComputedBbox().bbox)
    }

    @Test
    fun testLineBbox() {
        assertEquals(BoundingBox(102.0, -10.0, 130.0, 4.0), line.computeBbox())
    }

    @Test
    fun testPolygonBbox() {
        assertEquals(BoundingBox(100.0, 0.0, 101.0, 1.0), polygon.computeBbox())
    }

    @Test
    fun testMultiLineBbox() {
        assertEquals(BoundingBox(100.0, 0.0, 103.0, 3.0), multiLine.computeBbox())
    }

    @Test
    fun testMultiPolygonBbox() {
        assertEquals(BoundingBox(100.0, 0.0, 103.0, 3.0), multiPolygon.computeBbox())
    }

    @Test
    fun testBbox() {
        val point = Point.fromJson(readResourceFile("measurement/bbox/point.json"))
        assertEquals(BoundingBox(point.coordinates, point.coordinates), point.computeBbox())

        val lineString = LineString.fromJson(readResourceFile("measurement/bbox/lineString.json"))
        assertEquals(
            BoundingBox(-79.376220703125, 43.65197548731187, -73.58642578125, 45.4986468234261),
            lineString.computeBbox(),
        )

        val polygon = Polygon.fromJson(readResourceFile("measurement/bbox/polygon.json"))
        assertEquals(
            BoundingBox(-64.44580078125, 45.9511496866914, -61.973876953125, 47.07012182383309),
            polygon.computeBbox(),
        )
    }
}
