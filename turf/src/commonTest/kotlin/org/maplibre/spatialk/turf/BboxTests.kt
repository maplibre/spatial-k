@file:Suppress("MagicNumber")

package org.maplibre.spatialk.turf

import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.dsl.feature
import org.maplibre.spatialk.geojson.dsl.featureCollection
import org.maplibre.spatialk.geojson.dsl.lineString
import org.maplibre.spatialk.geojson.dsl.multiLineString
import org.maplibre.spatialk.geojson.dsl.multiPolygon
import org.maplibre.spatialk.geojson.dsl.point
import org.maplibre.spatialk.geojson.dsl.polygon
import kotlin.test.Test
import kotlin.test.assertEquals

private val point = point(102.0, 0.5)
private val line = lineString {
    point(102.0, -10.0)
    point(103.0, 1.0)
    point(104.0, 0.0)
    point(130.0, 4.0)
}
private val polygon = polygon {
    ring {
        point(101.0, 0.0)
        point(101.0, 1.0)
        point(100.0, 1.0)
        point(100.0, 0.0)
        complete()
    }
}
private val multiLine = multiLineString {
    lineString {
        point(100.0, 0.0)
        point(101.0, 1.0)
    }
    lineString {
        point(102.0, 2.0)
        point(103.0, 3.0)
    }
}
private val multiPolygon = multiPolygon {
    polygon {
        ring {
            point(102.0, 2.0)
            point(103.0, 2.0)
            point(103.0, 3.0)
            point(102.0, 3.0)
            complete()
        }
    }
    polygon {
        ring {
            point(100.0, 0.0)
            point(101.0, 0.0)
            point(101.0, 1.0)
            point(100.0, 1.0)
            complete()
        }
        ring {
            point(100.2, 0.2)
            point(101.8, 0.2)
            point(101.8, 0.8)
            point(100.2, 0.8)
            complete()
        }
    }
}

private val featureCollection = featureCollection {
    feature(geometry = point)
    feature(geometry = line)
    feature(geometry = polygon)
    feature(geometry = multiLine)
    feature(geometry = multiPolygon)
}

@ExperimentalTurfApi
class BboxTests {

    @Test
    fun testFeatureCollectionBbox() {
        assertEquals(BoundingBox(100.0, -10.0, 130.0, 4.0), bbox(featureCollection))
    }

    @Test
    fun testPointBbox() {
        assertEquals(BoundingBox(102.0, 0.5, 102.0, 0.5), bbox(point))
    }

    @Test
    fun testLineBbox() {
        assertEquals(BoundingBox(102.0, -10.0, 130.0, 4.0), bbox(line))
    }

    @Test
    fun testPolygonBbox() {
        assertEquals(BoundingBox(100.0, 0.0, 101.0, 1.0), bbox(polygon))
    }

    @Test
    fun testMultiLineBbox() {
        assertEquals(BoundingBox(100.0, 0.0, 103.0, 3.0), bbox(multiLine))
    }

    @Test
    fun testMultiPolygonBbox() {
        assertEquals(BoundingBox(100.0, 0.0, 103.0, 3.0), bbox(multiPolygon))
    }

    @Test
    fun testEmptyFeatures() {
        val emptyBbox = BoundingBox(
            Double.POSITIVE_INFINITY,
            Double.POSITIVE_INFINITY,
            Double.NEGATIVE_INFINITY,
            Double.NEGATIVE_INFINITY
        )

        assertEquals(emptyBbox, bbox(feature()))

        assertEquals(emptyBbox, bbox(featureCollection { }))
    }
}
