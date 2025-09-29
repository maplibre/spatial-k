package org.maplibre.spatialk.geojson.serialization

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.protobuf.ProtoBuf
import org.maplibre.spatialk.geojson.GeometryCollection
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position

class ProtoBufSerializationTests {
    fun assertFormat(serialize: (GeometryCollection) -> GeometryCollection) {
        val points = arrayOf(Point(1.0, 2.0), Point(2.0, 3.0))

        val lineString = LineString(*points)
        val geometries = listOf(points[0], lineString)

        val geometryCollection = GeometryCollection(geometries)

        val actualGeometryCollection = serialize(geometryCollection)

        val expectedGeometryCollection =
            GeometryCollection.fromJson(
                """
            {
                "type": "GeometryCollection",
                "geometries": [
                    {
                        "type": "Point",
                        "coordinates": [1.0, 2.0]
                    },
                    {
                        "type": "LineString",
                        "coordinates": [
                            [1.0, 2.0],
                            [2.0, 3.0]
                        ]
                    }
                ]
            }
            """
            )
        assertEquals(expectedGeometryCollection, actualGeometryCollection)

        val point = actualGeometryCollection.geometries[0]
        assertTrue(point is Point)
        assertEquals(Position(1.0, 2.0), point.coordinates)

        val ls = actualGeometryCollection.geometries[1]
        assertTrue(ls is LineString)
    }

    @Test
    fun testProtoBufSerialization() {
        assertFormat { obj ->
            @OptIn(ExperimentalSerializationApi::class)
            ProtoBuf.decodeFromByteArray(
                GeometryCollection.serializer(),
                ProtoBuf.encodeToByteArray(GeometryCollection.serializer(), obj),
            )
        }
    }

    @Test
    fun testCborSerialization() {
        assertFormat { obj ->
            @OptIn(ExperimentalSerializationApi::class)
            Cbor.decodeFromByteArray(
                GeometryCollection.serializer(),
                Cbor.encodeToByteArray(GeometryCollection.serializer(), obj),
            )
        }
    }
}
