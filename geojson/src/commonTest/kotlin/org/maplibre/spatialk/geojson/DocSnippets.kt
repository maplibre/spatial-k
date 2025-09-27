@file:Suppress("UnusedVariable", "unused")

package org.maplibre.spatialk.geojson

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import org.intellij.lang.annotations.Language
import org.maplibre.spatialk.geojson.dsl.*

// These snippets are primarily intended to be included in docs/geojson.md. Though they exist as
// part of the test suite, they are not intended to be comprehensive tests.

class DocSnippets {

    @Test
    fun geometryExhaustiveTypeChecks() {
        fun getSomeGeometry(): Geometry = point(0.0, 0.0)

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
                is GeometryCollection -> "GeometryCollection"
            }
        // --8<-- [end:geometryExhaustiveTypeChecks]
    }

    private inline fun kotlinAndJsonExample(kotlin: () -> String, @Language("json5") json: String) {
        val jsonC = Json { allowComments = true }
        assertEquals<JsonElement>(
            expected = jsonC.decodeFromString(json),
            actual = jsonC.decodeFromString(kotlin()),
        )
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

                position.json()
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

                point.json()
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
                multiPoint.json()
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
                lineString.json()
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
                multiLineString.json()
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
                polygon.json()
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
                multiPolygon.json()
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

                // Can be iterated over, and used in any way a Collection<T> can be
                geometryCollection.forEach { geometry ->
                    // ...
                }
                // --8<-- [end:geometryCollectionKt]

                geometryCollection.json()
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
                val feature = Feature(point)
                feature.setNumberProperty("size", 9999)

                val size: Number? = feature.getNumberProperty("size") // 9999
                val geometry: Geometry? = feature.geometry // point
                // --8<-- [end:featureKt]

                feature.json()
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
                val pointFeature = Feature(point)
                val featureCollection = FeatureCollection(pointFeature)

                featureCollection.forEach { feature ->
                    // ...
                }
                // --8<-- [end:featureCollectionKt]

                featureCollection.json()
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
                            }
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
                bbox.json()
            },
            json =
                """
                    // --8<-- [start:boundingBoxJson]
                    [11.6,45.1,12.7,45.7]
                    // --8<-- [end:boundingBoxJson]
                """,
        )
    }
}
