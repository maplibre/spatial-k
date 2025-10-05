package org.maplibre.spatialk.geojson.serialization

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.protobuf.ProtoBuf
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.GeometryCollection
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.Point

class SerializationFormatTests {
    fun assertFormat(serialize: (Feature<GeometryCollection>) -> Feature<GeometryCollection>) {
        val points = arrayOf(Point(1.0, 2.0), Point(2.0, 3.0))

        val lineString = LineString(*points)
        val geometries = listOf(points[0], lineString)
        val geometryCollection = GeometryCollection(geometries)
        val feature =
            Feature(
                geometryCollection,
                JsonObject(mapOf("name" to JsonPrimitive("Dinagat Islands"))),
            )

        val actualFeature = serialize(feature)

        val expectedFeature =
            Feature.fromJson<GeometryCollection>(
                """
            {
              "type": "Feature",
              "geometry": {
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
                },
                "properties": {
                    "name": "Dinagat Islands"
                }
            }
            """
            )
        assertEquals(expectedFeature, actualFeature)
    }

    private val serializer = Feature.serializer(GeometryCollection.serializer())

    @OptIn(ExperimentalSerializationApi::class)
    @Test
    fun testProtoBufSerialization() {
        assertFormat { obj ->
            @OptIn(ExperimentalSerializationApi::class)
            ProtoBuf.decodeFromByteArray(serializer, ProtoBuf.encodeToByteArray(serializer, obj))
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Test
    fun testCborSerialization() {
        assertFormat { obj ->
            @OptIn(ExperimentalSerializationApi::class)
            Cbor.decodeFromByteArray(serializer, Cbor.encodeToByteArray(serializer, obj))
        }
    }
}
