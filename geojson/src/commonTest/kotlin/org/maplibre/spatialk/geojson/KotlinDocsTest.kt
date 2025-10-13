@file:Suppress("UnusedVariable", "unused")

package org.maplibre.spatialk.geojson

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.*
import kotlinx.serialization.serializer
import org.intellij.lang.annotations.Language
import org.maplibre.spatialk.geojson.dsl.*

// These snippets are primarily intended to be included in documentation. Though they exist as
// part of the test suite, they are not intended to be comprehensive tests.

class KotlinDocsTest {

    @Test
    fun geometryExhaustiveTypeChecks() {
        fun getSomeGeometry(): Geometry = Point(0.0, 0.0)

        // --8<-- [start:geometryExhaustiveTypeChecks]
        val geometry: Geometry = getSomeGeometry()

        val type =
            when (geometry) {
                is Point -> "Point"
                is MultiPoint -> "MultiPoint"
                is LineString -> "LineString"
                is MultiLineString -> "MultiLineString"
                is Polygon -> "Polygon"
                is MultiPolygon -> "MultiPolygon"
                is GeometryCollection<*> -> "GeometryCollection"
            }
        // --8<-- [end:geometryExhaustiveTypeChecks]
    }

    private inline fun kotlinAndJsonExample(kotlin: () -> String, @Language("json5") json: String) {
        val jsonC = Json {
            @OptIn(ExperimentalSerializationApi::class)
            allowComments = true
        }
        // round trip to normalize property order across platforms
        val normalizedJsonExample = jsonC.encodeToString(jsonC.parseToJsonElement(json))
        val normalizedKotlinExample = jsonC.encodeToString(jsonC.parseToJsonElement(kotlin()))
        assertEquals(expected = normalizedKotlinExample, actual = normalizedJsonExample)
    }

    @Test
    fun positionExample() {
        kotlinAndJsonExample(
            kotlin = {
                // --8<-- [start:positionKt]
                val position = Position(-75.0, 45.0)
                val (longitude, latitude, altitude) = position

                // Access values
                position.longitude
                position.latitude
                position.altitude
                // --8<-- [end:positionKt]

                position.toJson()
            },
            json =
                """
                    // --8<-- [start:positionJson]
                    [-75.0, 45.0]
                    // --8<-- [end:positionJson]
                """,
        )
    }

    @Test
    fun pointExample() {
        kotlinAndJsonExample(
            kotlin = {
                // --8<-- [start:pointKt]
                val point = Point(Position(-75.0, 45.0))

                println(point.coordinates.longitude)
                // Prints: -75.0
                // --8<-- [end:pointKt]

                point.toJson()
            },
            json =
                """
                // --8<-- [start:pointJson]
                {
                    "type": "Point",
                    "coordinates": [-75.0, 45.0]
                }
                // --8<-- [end:pointJson]
            """,
        )
    }

    @Test
    fun multiPointExample() {
        kotlinAndJsonExample(
            kotlin = {
                // --8<-- [start:multiPointKt]
                val multiPoint = MultiPoint(Position(-75.0, 45.0), Position(-79.0, 44.0))
                // --8<-- [end:multiPointKt]
                multiPoint.toJson()
            },
            json =
                """
                // --8<-- [start:multiPointJson]
                {
                    "type": "MultiPoint",
                    "coordinates": [[-75.0, 45.0], [-79.0, 44.0]]
                }
                // --8<-- [end:multiPointJson]
            """,
        )
    }

    @Test
    fun lineStringExample() {
        kotlinAndJsonExample(
            kotlin = {
                // --8<-- [start:lineStringKt]
                val lineString = LineString(Position(-75.0, 45.0), Position(-79.0, 44.0))
                // --8<-- [end:lineStringKt]
                lineString.toJson()
            },
            json =
                """
                // --8<-- [start:lineStringJson]
                {
                    "type": "LineString",
                    "coordinates": [[-75.0, 45.0], [-79.0, 44.0]]
                }
                // --8<-- [end:lineStringJson]
            """,
        )
    }

    @Test
    fun multiLineStringExample() {
        kotlinAndJsonExample(
            kotlin = {
                // --8<-- [start:multiLineStringKt]
                val multiLineString =
                    MultiLineString(
                        listOf(Position(12.3, 45.6), Position(78.9, 12.3)),
                        listOf(Position(87.6, 54.3), Position(21.9, 56.4)),
                    )
                // --8<-- [end:multiLineStringKt]
                multiLineString.toJson()
            },
            json =
                """
                // --8<-- [start:multiLineStringJson]
                {
                    "type": "MultiLineString",
                    "coordinates": [
                        [[12.3, 45.6], [78.9, 12.3]],
                        [[87.6, 54.3], [21.9, 56.4]]
                    ]
                }
                // --8<-- [end:multiLineStringJson]
            """,
        )
    }

    @Test
    fun polygonExample() {
        kotlinAndJsonExample(
            kotlin = {
                // --8<-- [start:polygonKt]
                val polygon =
                    Polygon(
                        listOf(
                            Position(-79.87, 43.42),
                            Position(-78.89, 43.49),
                            Position(-79.07, 44.02),
                            Position(-79.95, 43.87),
                            Position(-79.87, 43.42),
                        ),
                        listOf(
                            Position(-79.75, 43.81),
                            Position(-79.56, 43.85),
                            Position(-79.7, 43.88),
                            Position(-79.75, 43.81),
                        ),
                    )
                // --8<-- [end:polygonKt]
                polygon.toJson()
            },
            json =
                """
                // --8<-- [start:polygonJson]
                {
                    "type": "Polygon",
                    "coordinates": [
                        [[-79.87, 43.42], [-78.89, 43.49], [-79.07, 44.02], [-79.95, 43.87], [-79.87, 43.42]],
                        [[-79.75, 43.81], [-79.56, 43.85], [-79.7, 43.88], [-79.75, 43.81]]
                    ]
                }
                // --8<-- [end:polygonJson]
            """,
        )
    }

    @Test
    fun multiPolygonExample() {
        kotlinAndJsonExample(
            kotlin = {
                // --8<-- [start:multiPolygonKt]
                val polygon =
                    listOf(
                        listOf(
                            Position(-79.87, 43.42),
                            Position(-78.89, 43.49),
                            Position(-79.07, 44.02),
                            Position(-79.95, 43.87),
                            Position(-79.87, 43.42),
                        ),
                        listOf(
                            Position(-79.75, 43.81),
                            Position(-79.56, 43.85),
                            Position(-79.7, 43.88),
                            Position(-79.75, 43.81),
                        ),
                    )
                val multiPolygon = MultiPolygon(polygon, polygon)
                // --8<-- [end:multiPolygonKt]
                multiPolygon.toJson()
            },
            json =
                """
                // --8<-- [start:multiPolygonJson]
                {
                    "type": "MultiPolygon",
                    "coordinates": [
                        [
                            [[-79.87, 43.42], [-78.89, 43.49], [-79.07, 44.02], [-79.95, 43.87], [-79.87, 43.42]],
                            [[-79.75, 43.81], [-79.56, 43.85], [-79.7, 43.88], [-79.75, 43.81]]
                        ],
                        [
                            [[-79.87, 43.42], [-78.89, 43.49], [-79.07, 44.02], [-79.95, 43.87], [-79.87, 43.42]],
                            [[-79.75, 43.81], [-79.56, 43.85], [-79.7, 43.88], [-79.75, 43.81]]
                        ]
                    ]
                }
                // --8<-- [end:multiPolygonJson]
            """,
        )
    }

    @Test
    fun geometryCollectionExample() {
        kotlinAndJsonExample(
            kotlin = {
                // --8<-- [start:geometryCollectionKt]
                val point = Point(Position(-75.0, 45.0))
                val lineString = LineString(Position(-75.0, 45.0), Position(-79.0, 44.0))
                val geometryCollection = GeometryCollection(point, lineString)

                // Can be iterated over and used in any way a Collection<T> can be
                geometryCollection.forEach { geometry ->
                    // ...
                }
                // --8<-- [end:geometryCollectionKt]

                geometryCollection.toJson()
            },
            json =
                """
                // --8<-- [start:geometryCollectionJson]
                {
                    "type": "GeometryCollection",
                    "geometries": [
                        {
                            "type": "Point",
                            "coordinates": [-75.0, 45.0]
                        },
                        {
                            "type": "LineString",
                            "coordinates": [[-75.0, 45.0], [-79.0, 44.0]]
                        }
                    ]
                }
                // --8<-- [end:geometryCollectionJson]
            """,
        )
    }

    @Test
    fun featureExample() {
        kotlinAndJsonExample(
            kotlin = {
                // --8<-- [start:featureKt]
                val point = Point(Position(-75.0, 45.0))
                val feature = Feature(point, properties = buildJsonObject { put("size", 9999) })

                val size: Number? = feature.properties["size"]?.jsonPrimitive?.doubleOrNull // 9999
                val geometry: Point = feature.geometry
                // --8<-- [end:featureKt]

                feature.toJson()
            },
            json =
                """
                // --8<-- [start:featureJson]
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [-75.0, 45.0]
                    },
                    "properties": {
                        "size": 9999
                    }
                }
                // --8<-- [end:featureJson]
            """,
        )
    }

    @Test
    fun featureCollectionExample() {
        kotlinAndJsonExample(
            kotlin = {
                // --8<-- [start:featureCollectionKt]
                val point = Point(Position(-75.0, 45.0))
                val pointFeature = Feature(point, null)
                val featureCollection = FeatureCollection(pointFeature)

                featureCollection.forEach { feature ->
                    // ...
                }
                // --8<-- [end:featureCollectionKt]

                featureCollection.toJson()
            },
            json =
                """
                // --8<-- [start:featureCollectionJson]
                {
                    "type": "FeatureCollection",
                    "features": [
                        {
                            "type": "Feature",
                            "geometry": {
                                "type": "Point",
                                "coordinates": [-75.0, 45.0]
                            },
                            "properties":null
                        }
                    ]
                }
                // --8<-- [end:featureCollectionJson]
            """,
        )
    }

    @Test
    fun boundingBoxExample() {
        kotlinAndJsonExample(
            kotlin = {
                // --8<-- [start:boundingBoxKt]
                val bbox = BoundingBox(west = 11.6, south = 45.1, east = 12.7, north = 45.7)
                val (southwest, northeast) = bbox // Two Positions
                // --8<-- [end:boundingBoxKt]
                bbox.toJson()
            },
            json =
                """
                    // --8<-- [start:boundingBoxJson]
                    [11.6,45.1,12.7,45.7]
                    // --8<-- [end:boundingBoxJson]
                """,
        )
    }

    @Test
    fun serializationToJsonExample() {
        // --8<-- [start:serializationToJson]
        val point = Point(Position(-75.0, 45.0))
        val feature = Feature(point, null)
        val featureCollection = FeatureCollection(feature)

        val json = featureCollection.toJson()
        println(json)
        // --8<-- [end:serializationToJson]
    }

    @Test
    fun serializationFromJsonExample() {
        assertFailsWith<SerializationException> {
            // --8<-- [start:serializationFromJson1]
            // Throws exception if the JSON cannot be deserialized to a Point
            val myPoint: Point =
                Point.fromJson("""{"type": "MultiPoint", "coordinates": [[-75.0, 45.0]]}""")
            // --8<-- [end:serializationFromJson1]
        }

        // --8<-- [start:serializationFromJson2]
        // Returns null if an error occurs
        val nullable: Point? =
            Point.fromJsonOrNull("""{"type": "MultiPoint", "coordinates": [[-75.0, 45.0]]}""")
        // --8<-- [end:serializationFromJson2]
        assertEquals(null, nullable)
    }

    @Test
    fun kotlinxSerializationExample() {
        // --8<-- [start:kotlinxSerialization]
        val feature: Feature<*, *> =
            GeoJson.jsonFormat.decodeFromString(
                serializer<Feature<Geometry, JsonObject?>>(),
                """
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [102.0, 20.0]
                    },
                    "properties": {
                        "name": "point name"
                    }
                }
                """,
            )
        // --8<-- [end:kotlinxSerialization]
    }

    @Test
    fun dslMultiPointExample() {
        kotlinAndJsonExample(
            kotlin = {
                // --8<-- [start:dslMultiPointKt]
                val myPoint = Point(88.0, 34.0)
                val multiPoint = buildMultiPoint {
                    add(-75.0, 45.0)
                    add(Position(-78.0, 44.0))
                    add(myPoint)
                }
                // --8<-- [end:dslMultiPointKt]

                multiPoint.toJson()
            },
            json =
                """
                // --8<-- [start:dslMultiPointJson]
                {
                  "type": "MultiPoint",
                  "coordinates": [
                    [-75.0, 45.0],
                    [-78.0, 44.0],
                    [88.0, 34.0]
                  ]
                }
                // --8<-- [end:dslMultiPointJson]
            """,
        )
    }

    @Test
    fun dslLineStringExample() {
        kotlinAndJsonExample(
            kotlin = {
                // --8<-- [start:dslLineStringKt]
                val lineString = buildLineString {
                    add(45.0, 45.0)
                    add(0.0, 0.0)
                }
                // --8<-- [end:dslLineStringKt]

                lineString.toJson()
            },
            json =
                """
                // --8<-- [start:dslLineStringJson]
                {
                  "type": "LineString",
                  "coordinates": [[45.0, 45.0], [0.0, 0.0]]
                }
                // --8<-- [end:dslLineStringJson]
            """,
        )
    }

    @Test
    fun dslMultiLineStringExample() {
        kotlinAndJsonExample(
            kotlin = {
                // --8<-- [start:dslMultiLineStringKt]
                val simpleLine = buildLineString {
                    add(45.0, 45.0)
                    add(0.0, 0.0)
                }

                val multiLineString = buildMultiLineString {
                    add(simpleLine)

                    // Inline LineString creation
                    addLineString {
                        add(44.4, 55.5)
                        add(55.5, 66.6)
                    }
                }
                // --8<-- [end:dslMultiLineStringKt]

                multiLineString.toJson()
            },
            json =
                """
                // --8<-- [start:dslMultiLineStringJson]
                {
                  "type": "MultiLineString",
                  "coordinates": [
                    [[45.0, 45.0], [0.0, 0.0]],
                    [[44.4, 55.5], [55.5, 66.6]]
                  ]
                }
                // --8<-- [end:dslMultiLineStringJson]
            """,
        )
    }

    @Test
    fun dslPolygonExample() {
        kotlinAndJsonExample(
            kotlin = {
                // --8<-- [start:dslPolygonKt]
                val simpleLine = buildLineString {
                    add(45.0, 45.0)
                    add(0.0, 0.0)
                }

                val polygon = buildPolygon {
                    addRing {
                        // LineStrings can be used as part of a ring
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
                // --8<-- [end:dslPolygonKt]

                polygon.toJson()
            },
            json =
                """
                // --8<-- [start:dslPolygonJson]
                {
                  "type": "Polygon",
                  "coordinates": [
                    [[45.0, 45.0], [0.0, 0.0], [12.0, 12.0], [45.0, 45.0]],
                    [[4.0, 4.0], [2.0, 2.0], [3.0, 3.0], [4.0, 4.0]]
                  ]
                }
                // --8<-- [end:dslPolygonJson]
            """,
        )
    }

    @Test
    fun dslMultiPolygonExample() {
        kotlinAndJsonExample(
            kotlin = {
                // --8<-- [start:dslMultiPolygonKt]
                val simplePolygon = buildPolygon {
                    addRing {
                        add(45.0, 45.0)
                        add(0.0, 0.0)
                        add(12.0, 12.0)
                    }
                    addRing {
                        add(4.0, 4.0)
                        add(2.0, 2.0)
                        add(3.0, 3.0)
                    }
                }

                val multiPolygon = buildMultiPolygon {
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
                // --8<-- [end:dslMultiPolygonKt]

                multiPolygon.toJson()
            },
            json =
                """
                // --8<-- [start:dslMultiPolygonJson]
                {
                  "type": "MultiPolygon",
                  "coordinates": [
                    [
                      [[45.0, 45.0], [0.0, 0.0], [12.0, 12.0], [45.0, 45.0]],
                      [[4.0, 4.0], [2.0, 2.0], [3.0, 3.0], [4.0, 4.0]]
                    ], [
                      [[12.0, 0.0], [0.0, 12.0], [-12.0, 0.0], [5.0, 5.0], [12.0, 0.0]]
                    ]
                  ]
                }
                // --8<-- [end:dslMultiPolygonJson]
            """,
        )
    }

    @Test
    fun dslGeometryCollectionExample() {
        kotlinAndJsonExample(
            kotlin = {
                // --8<-- [start:dslGeometryCollectionKt]
                val simplePoint = Point(-75.0, 45.0, 100.0)
                val simpleLine = buildLineString {
                    add(45.0, 45.0)
                    add(0.0, 0.0)
                }
                val simplePolygon = buildPolygon {
                    addRing {
                        add(45.0, 45.0)
                        add(0.0, 0.0)
                        add(12.0, 12.0)
                    }
                    addRing {
                        add(4.0, 4.0)
                        add(2.0, 2.0)
                        add(3.0, 3.0)
                    }
                }

                val geometryCollection = buildGeometryCollection {
                    add(simplePoint)
                    add(simpleLine)
                    add(simplePolygon)
                }
                // --8<-- [end:dslGeometryCollectionKt]

                geometryCollection.toJson()
            },
            json =
                """
                // --8<-- [start:dslGeometryCollectionJson]
                {
                  "type": "GeometryCollection",
                  "geometries": [
                    {
                      "type": "Point",
                      "coordinates": [-75.0, 45.0, 100.0]
                    },
                    {
                      "type": "LineString",
                      "coordinates": [[45.0, 45.0], [0.0, 0.0]]
                    },
                    {
                      "type": "Polygon",
                      "coordinates": [
                        [[45.0, 45.0], [0.0, 0.0], [12.0, 12.0], [45.0, 45.0]],
                        [[4.0, 4.0], [2.0, 2.0], [3.0, 3.0], [4.0, 4.0]]
                      ]
                    }
                  ]
                }
                // --8<-- [end:dslGeometryCollectionJson]
            """,
        )
    }

    @Test
    fun dslFeatureExample() {
        kotlinAndJsonExample(
            kotlin = {
                // --8<-- [start:dslFeatureKt]
                val feature =
                    buildFeature(geometry = Point(-75.0, 45.0)) {
                        id = "point1"
                        bbox = BoundingBox(-76.9, 44.1, -74.2, 45.7)
                        properties = buildJsonObject {
                            put("name", "Hello World")
                            put("value", 13)
                            put("cool", true)
                        }
                    }
                // --8<-- [end:dslFeatureKt]

                feature.toJson()
            },
            json =
                """
                // --8<-- [start:dslFeatureJson]
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [-75.0, 45.0]
                    },
                    "properties": {
                        "name": "Hello World",
                        "value": 13,
                        "cool": true
                    },
                    "id": "point1",
                    "bbox": [-76.9, 44.1, -74.2, 45.7]
                }
                // --8<-- [end:dslFeatureJson]
            """,
        )
    }

    @Test
    fun dslFeatureCollectionExample() {
        kotlinAndJsonExample(
            kotlin = {
                // --8<-- [start:dslFeatureCollectionKt]
                val featureCollection = buildFeatureCollection {
                    addFeature {
                        geometry = Point(-75.0, 45.0)
                        properties = buildJsonObject { put("name", "Hello") }
                    }
                }
                // --8<-- [end:dslFeatureCollectionKt]

                featureCollection.toJson()
            },
            json =
                """
                // --8<-- [start:dslFeatureCollectionJson]
                {
                  "type": "FeatureCollection",
                  "features": [
                    {
                      "type": "Feature",
                      "geometry": {
                        "type": "Point",
                        "coordinates": [-75.0, 45.0]
                      },
                      "properties": {
                        "name": "Hello"
                      }
                    }
                  ]
                }
                // --8<-- [end:dslFeatureCollectionJson]
            """,
        )
    }
}
