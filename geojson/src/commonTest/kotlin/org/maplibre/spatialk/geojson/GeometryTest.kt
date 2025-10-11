package org.maplibre.spatialk.geojson

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.maplibre.spatialk.geojson.utils.DELTA

class GeometryTest {

    @Test
    fun geometryCollectionFromJson() {
        val json =
            """
            {
                "type": "GeometryCollection",
                "bbox": [120, 40, -120, -40],
                "geometries": [
                    {
                        "type": "Point",
                        "bbox": [110, 30, -110, -30],
                        "coordinates": [100, 0]
                    },
                    {
                        "type": "LineString",
                        "bbox": [110, 30, -110, -30],
                        "coordinates": [[101, 0], [102, 1]]
                    }
                ]
            }
            """
                .trimIndent()

        val geometry = Geometry.fromJson(json)
        assertTrue(geometry is GeometryCollection<*>)
    }

    @Test
    fun pointFromJson() {
        val geometry =
            Geometry.fromJson(
                """
                {
                    "coordinates": [2, 3],
                    "type": "Point",
                    "bbox": [1.0, 2.0, 3.0, 4.0]
                }
                """
                    .trimIndent()
            )

        assertNotNull(geometry)
        val bbox = geometry.bbox
        assertNotNull(bbox)
        assertEquals(1.0, bbox.southwest.longitude, DELTA)
        assertEquals(2.0, bbox.southwest.latitude, DELTA)
        assertEquals(3.0, bbox.northeast.longitude, DELTA)
        assertEquals(4.0, bbox.northeast.latitude, DELTA)
        assertTrue(geometry is Point)
        assertEquals(2.0, geometry.coordinates.longitude, DELTA)
        assertEquals(3.0, geometry.coordinates.latitude, DELTA)
    }

    @Test
    fun pointToJson() {
        val geometry: Geometry = Point(2.0, 3.0, bbox = BoundingBox(1.0, 2.0, 3.0, 4.0))

        val actualPoint = Point.fromJson(geometry.toJson())
        val expectedPoint =
            Point.fromJson(
                """
                {
                    "coordinates": [2.0, 3.0],
                    "type": "Point",
                    "bbox": [1.0, 2.0, 3.0, 4.0]
                }
                """
                    .trimIndent()
            )
        assertEquals(expectedPoint, actualPoint)
    }

    @Test
    fun lineStringFromJson() {
        val lineString =
            Geometry.fromJson(
                """
                {
                    "coordinates": [
                        [1, 2],
                        [2, 3],
                        [3, 4]
                    ],
                    "type": "LineString",
                    "bbox": [1.0, 2.0, 3.0, 4.0]
                }
                """
                    .trimIndent()
            )

        assertNotNull(lineString)
        val bbox = lineString.bbox
        assertNotNull(bbox)
        assertEquals(1.0, bbox.southwest.longitude, DELTA)
        assertEquals(2.0, bbox.southwest.latitude, DELTA)
        assertEquals(3.0, bbox.northeast.longitude, DELTA)
        assertEquals(4.0, bbox.northeast.latitude, DELTA)
        assertTrue(lineString is LineString)
        assertEquals(1.0, lineString.coordinates[0].longitude, DELTA)
        assertEquals(2.0, lineString.coordinates[0].latitude, DELTA)
        assertEquals(2.0, lineString.coordinates[1].longitude, DELTA)
        assertEquals(3.0, lineString.coordinates[1].latitude, DELTA)
        assertEquals(3.0, lineString.coordinates[2].longitude, DELTA)
        assertEquals(4.0, lineString.coordinates[2].latitude, DELTA)
    }

    @Test
    fun lineStringToJson() {
        val geometry: Geometry =
            LineString(
                listOf(Position(1.0, 2.0), Position(2.0, 3.0), Position(3.0, 4.0)),
                BoundingBox(1.0, 2.0, 3.0, 4.0),
            )

        val actualLineString = LineString.fromJson(geometry.toJson())
        val expectedLineString =
            LineString.fromJson(
                """
                {
                    "coordinates": [
                        [1.0, 2.0],
                        [2.0, 3.0],
                        [3.0, 4.0]
                    ],
                    "type": "LineString",
                    "bbox": [1.0, 2.0, 3.0, 4.0]
                }
                """
                    .trimIndent()
            )

        assertEquals(expectedLineString, actualLineString)
    }

    @Test
    fun polymorphicRoundTrip() {
        val geometry: Geometry =
            LineString(
                listOf(Position(1.0, 2.0), Position(2.0, 3.0), Position(3.0, 4.0)),
                BoundingBox(1.0, 2.0, 3.0, 4.0),
            )
        val json = GeoJson.encodeToString(geometry)
        val actualGeometry = GeoJson.decodeFromString<Geometry>(json)
        assertEquals(geometry, actualGeometry)
    }
}
