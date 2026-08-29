package org.maplibre.spatialk.geojson

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.intellij.lang.annotations.Language
import org.maplibre.spatialk.geojson.GeoJsonObject.Companion.getForeignMember
import org.maplibre.spatialk.geojson.dsl.buildFeature
import org.maplibre.spatialk.geojson.dsl.buildFeatureCollection
import org.maplibre.spatialk.geojson.dsl.buildLineString
import org.maplibre.spatialk.geojson.dsl.buildMultiPoint
import org.maplibre.spatialk.geojson.utils.assertJsonEquals

class ForeignMembersTest {

    @Serializable private data class ZoneMembers(val zone_id: String)

    @Serializable private data class NameProp(val name: String)

    @Language("json")
    private val featureWithZoneJson =
        """
        {
            "type": "Feature",
            "geometry": { "type": "Point", "coordinates": [-75.0, 45.0] },
            "properties": { "name": "Station" },
            "zone_id": "zone-123"
        }
        """

    @Language("json")
    private val featureCollectionWithZoneJson =
        """
        {
            "type": "FeatureCollection",
            "features": [
                {
                    "type": "Feature",
                    "geometry": { "type": "Point", "coordinates": [-75.0, 45.0] },
                    "properties": { "name": "Station" },
                    "zone_id": "zone-123"
                }
            ]
        }
        """

    @Test
    fun featureZoneId() {
        val feature = Feature.fromJson<Point, NameProp>(featureWithZoneJson)
        assertEquals("Station", feature.properties.name)
        assertEquals(JsonPrimitive("zone-123"), feature.foreignMembers["zone_id"])
        assertEquals(JsonPrimitive("zone-123"), feature.getForeignMember("zone_id"))
        assertEquals(ZoneMembers("zone-123"), feature.decodeForeignMembers<ZoneMembers>())

        val collection = FeatureCollection.fromJson<Point, NameProp>(featureCollectionWithZoneJson)
        assertEquals(
            ZoneMembers("zone-123"),
            collection.features.single().decodeForeignMembers<ZoneMembers>(),
        )
    }

    @Test
    fun roundTripPreservesZoneId() {
        val decoded = Feature.fromJson<Point, NameProp>(featureWithZoneJson)
        assertJsonEquals(featureWithZoneJson, decoded.toJson())
    }

    @Test
    fun writePathFlattensForeignMembers() {
        val feature =
            Feature(
                geometry = Point(-75.0, 45.0),
                properties = NameProp("Station"),
                foreignMembers = foreignMembersOf(ZoneMembers("zone-123")),
            )
        val json = GeoJson.jsonFormat.parseToJsonElement(feature.toJson()).jsonObject
        assertEquals(JsonPrimitive("zone-123"), json["zone_id"])
        assertNull(json["foreignMembers"])
        assertEquals(JsonPrimitive("Station"), json["properties"]?.jsonObject?.get("name"))
        assertNull(json["properties"]?.jsonObject?.get("zone_id"))

        val fromBuilder =
            Feature(
                geometry = Point(-75.0, 45.0),
                properties = buildJsonObject { put("name", "Station") },
                foreignMembers = buildJsonObject { put("zone_id", "zone-123") },
            )
        assertJsonEquals(featureWithZoneJson, fromBuilder.toJson())
    }

    @Test
    fun rfcTitleForeignMember() {
        @Language("json")
        val json =
            """
            {
                "type": "Feature",
                "geometry": { "type": "Point", "coordinates": [1.0, 2.0] },
                "properties": null,
                "title": "Example Feature"
            }
            """
        val feature = Feature.fromJson<Point, Nothing?>(json)
        assertEquals(JsonPrimitive("Example Feature"), feature.foreignMembers["title"])
        assertJsonEquals(json, feature.toJson())
    }

    @Test
    fun rfcCenterlineIsNotParsedAsGeometry() {
        @Language("json")
        val json =
            """
            {
                "type": "Feature",
                "geometry": { "type": "Point", "coordinates": [1.0, 2.0] },
                "properties": null,
                "centerline": {
                    "type": "LineString",
                    "coordinates": [[0.0, 0.0], [1.0, 1.0]]
                }
            }
            """
        val feature = Feature.fromJson<Point, Nothing?>(json)
        assertIs<Point>(feature.geometry)
        val centerline = assertIs<JsonObject>(feature.foreignMembers["centerline"])
        assertEquals("LineString", centerline["type"]?.jsonPrimitive?.content)
        assertJsonEquals(json, feature.toJson())
    }

    @Test
    fun geometryExtrasRoundTrip() {
        @Language("json")
        val json =
            """
            {
                "type": "Point",
                "coordinates": [12.3, 45.6],
                "crs": { "type": "name", "properties": { "name": "EPSG:4326" } },
                "title": "origin"
            }
            """
        val point = Point.fromJson(json)
        assertEquals("origin", point.foreignMembers["title"]?.jsonPrimitive?.content)
        assertIs<JsonObject>(point.foreignMembers["crs"])
        assertJsonEquals(json, point.toJson())
    }

    @Test
    fun nestedFeatureAndGeometryExtrasAreIndependent() {
        @Language("json")
        val json =
            """
            {
                "type": "Feature",
                "geometry": {
                    "type": "Point",
                    "coordinates": [1.0, 2.0],
                    "crs": { "type": "name", "properties": { "name": "EPSG:4326" } }
                },
                "properties": { "name": "Station" },
                "zone_id": "zone-123"
            }
            """
        val feature = Feature.fromJson<Point, NameProp>(json)
        assertEquals(JsonPrimitive("zone-123"), feature.foreignMembers["zone_id"])
        assertNull(feature.foreignMembers["crs"])
        assertIs<JsonObject>(feature.geometry.foreignMembers["crs"])
        assertNull(feature.geometry.foreignMembers["zone_id"])
        assertJsonEquals(json, feature.toJson())
    }

    @Test
    fun featureCollectionExtras() {
        @Language("json")
        val json =
            """
            {
                "type": "FeatureCollection",
                "features": [],
                "title": "All zones"
            }
            """
        val collection = FeatureCollection.fromJson<Nothing?, Nothing?>(json)
        assertEquals(JsonPrimitive("All zones"), collection.foreignMembers["title"])
        assertJsonEquals(json, collection.toJson())
    }

    @Test
    fun explicitNullsStillWritesRequiredFeatureMembers() {
        val json = Json { explicitNulls = false }
        val empty = Feature(geometry = null, properties = null)
        val withTitle =
            Feature(
                geometry = null,
                properties = null,
                foreignMembers = buildJsonObject { put("title", "Example Feature") },
            )
        val emptyEncoded = json.encodeToString<Feature<Nothing?, Nothing?>>(empty)
        val titledEncoded = json.encodeToString<Feature<Nothing?, Nothing?>>(withTitle)
        assertTrue(emptyEncoded.contains("\"geometry\""))
        assertTrue(emptyEncoded.contains("\"properties\""))
        assertTrue(titledEncoded.contains("\"geometry\""))
        assertTrue(titledEncoded.contains("\"properties\""))
        assertTrue(titledEncoded.contains("\"title\""))

        val decoded = json.decodeFromString<Feature<Nothing?, Nothing?>>(titledEncoded)
        assertNull(decoded.geometry)
        assertNull(decoded.properties)
        assertEquals(JsonPrimitive("Example Feature"), decoded.foreignMembers["title"])
    }

    @Test
    fun foreignMemberBeforeTypeDecodes() {
        @Language("json")
        val json =
            """
            {
                "title": "origin",
                "type": "Point",
                "coordinates": [1.0, 2.0]
            }
            """
        val point = Point.fromJson(json)
        assertEquals(JsonPrimitive("origin"), point.foreignMembers["title"])
        assertEquals(1.0, point.longitude)
        assertEquals(2.0, point.latitude)
    }

    @Test
    fun featureCollectionExtrasAfterFeatures() {
        @Language("json")
        val json =
            """
            {
                "type": "FeatureCollection",
                "features": [
                    {
                        "type": "Feature",
                        "geometry": { "type": "Point", "coordinates": [-75.0, 45.0] },
                        "properties": { "name": "Station" },
                        "zone_id": "zone-123"
                    }
                ],
                "title": "All zones"
            }
            """
        val collection = FeatureCollection.fromJson<Point, NameProp>(json)
        assertEquals(JsonPrimitive("All zones"), collection.foreignMembers["title"])
        assertEquals(
            ZoneMembers("zone-123"),
            collection.features.single().decodeForeignMembers<ZoneMembers>(),
        )
        assertEquals("Station", collection.features.single().properties.name)
        assertJsonEquals(json, collection.toJson())
    }

    @Test
    fun geometryCollectionExtras() {
        @Language("json")
        val json =
            """
            {
                "type": "GeometryCollection",
                "geometries": [
                    { "type": "Point", "coordinates": [1.0, 2.0] }
                ],
                "title": "points"
            }
            """
        val collection = GeometryCollection.fromJson<Point>(json)
        assertEquals(JsonPrimitive("points"), collection.foreignMembers["title"])
        assertEquals(1, collection.geometries.size)
        assertJsonEquals(json, collection.toJson())
    }

    @Test
    fun emptyBagOmittedFromJson() {
        val json = Point(12.3, 45.6).toJson()
        assertFalse(json.contains("foreignMembers"))
        assertEquals("""{"type":"Point","coordinates":[12.3,45.6]}""", json)
        assertEquals(EmptyForeignMembers, Point(12.3, 45.6).foreignMembers)
    }

    @Test
    fun forbiddenReservedKeysRejectedOnDecode() {
        assertFailsWith<SerializationException> {
            Feature.fromJson<Point, Nothing?>(
                """
                {
                    "type": "Feature",
                    "geometry": { "type": "Point", "coordinates": [1.1, 2.2] },
                    "properties": null,
                    "coordinates": [1.1, 2.2]
                }
                """
            )
        }
        assertFailsWith<SerializationException> {
            Point.fromJson(
                """
                {
                    "type": "Point",
                    "coordinates": [1.1, 2.2],
                    "properties": { "name": "nope" }
                }
                """
            )
        }
        assertFailsWith<SerializationException> {
            FeatureCollection.fromJson<Nothing?, Nothing?>(
                """
                {
                    "type": "FeatureCollection",
                    "features": [],
                    "geometry": { "type": "Point", "coordinates": [1.1, 2.2] }
                }
                """
            )
        }
        assertFailsWith<SerializationException> {
            Point.fromJson(
                """
                {
                    "type": "Point",
                    "coordinates": [1.1, 2.2],
                    "geometries": []
                }
                """
            )
        }
        assertFailsWith<SerializationException> {
            GeometryCollection.fromJson<Point>(
                """
                {
                    "type": "GeometryCollection",
                    "geometries": [],
                    "coordinates": [1.1, 2.2]
                }
                """
            )
        }
    }

    @Test
    fun explicitNullIdAndBboxDecode() {
        val feature =
            Feature.fromJson<Point, Nothing?>(
                """
                {
                    "type": "Feature",
                    "geometry": { "type": "Point", "coordinates": [1.1, 2.2] },
                    "properties": null,
                    "id": null,
                    "bbox": null,
                    "title": "Example Feature"
                }
                """
            )
        assertNull(feature.id)
        assertNull(feature.bbox)
        assertEquals(JsonPrimitive("Example Feature"), feature.foreignMembers["title"])

        val point =
            Point.fromJson(
                """
                {
                    "type": "Point",
                    "coordinates": [1.1, 2.2],
                    "bbox": null,
                    "title": "origin"
                }
                """
            )
        assertNull(point.bbox)
        assertEquals(JsonPrimitive("origin"), point.foreignMembers["title"])
    }

    @Test
    fun reservedNameRejectedOnConstruct() {
        assertFailsWith<IllegalArgumentException> {
            Feature(
                Point(1.0, 2.0),
                null,
                foreignMembers = buildJsonObject { put("geometry", "nope") },
            )
        }
        assertFailsWith<IllegalArgumentException> {
            Point(1.0, 2.0, foreignMembers = buildJsonObject { put("coordinates", "nope") })
        }
        assertFailsWith<IllegalArgumentException> {
            FeatureCollection<Nothing?, Nothing?>(
                emptyList(),
                foreignMembers = buildJsonObject { put("features", "nope") },
            )
        }
        assertFailsWith<IllegalArgumentException> {
            Point(1.0, 2.0, foreignMembers = buildJsonObject { put("geometries", "nope") })
        }
        assertFailsWith<IllegalArgumentException> {
            GeometryCollection<Point>(
                emptyList(),
                foreignMembers = buildJsonObject { put("coordinates", "nope") },
            )
        }
        Point(1.0, 2.0, foreignMembers = buildJsonObject { put("id", "ok") })
        Point(1.0, 2.0, foreignMembers = buildJsonObject { put("crs", "ok") })
    }

    @Test
    fun equalityIncludesForeignMembers() {
        val empty = Feature(Point(1.0, 2.0), null)
        assertEquals(Feature(Point(1.0, 2.0), null), empty)
        assertEquals(EmptyForeignMembers, empty.foreignMembers)

        val withZone =
            Feature(
                Point(1.0, 2.0),
                null,
                foreignMembers = buildJsonObject { put("zone_id", "zone-123") },
            )
        assertNotEquals(empty, withZone)
    }

    @Test
    fun typedPropertiesStillIgnoreUnknownPropertyKeys() {
        @Language("json")
        val json =
            """
            {
                "type": "Feature",
                "geometry": { "type": "Point", "coordinates": [1.0, 2.0] },
                "properties": { "name": "Station", "extra": "ignored" },
                "zone_id": "zone-123"
            }
            """
        val feature = Feature.fromJson<Point, NameProp>(json)
        assertEquals("Station", feature.properties.name)
        assertEquals(JsonPrimitive("zone-123"), feature.foreignMembers["zone_id"])
        assertNull(feature.foreignMembers["extra"])
    }

    @Test
    fun polymorphicDecodersKeepExtras() {
        val asObject = GeoJsonObject.fromJson(featureWithZoneJson)
        val feature = assertIs<Feature<*, *>>(asObject)
        assertEquals(JsonPrimitive("zone-123"), feature.foreignMembers["zone_id"])

        @Language("json")
        val pointJson =
            """
            {
                "type": "Point",
                "coordinates": [1.0, 2.0],
                "title": "origin"
            }
            """
        val asGeometry = Geometry.fromJson(pointJson)
        val point = assertIs<Point>(asGeometry)
        assertEquals(JsonPrimitive("origin"), point.foreignMembers["title"])
    }

    @Test
    fun dslSetsForeignMembers() {
        val feature =
            buildFeature(Point(-75.0, 45.0), NameProp("Station")) {
                foreignMembers = foreignMembersOf(ZoneMembers("zone-123"))
            }
        assertEquals(ZoneMembers("zone-123"), feature.decodeForeignMembers<ZoneMembers>())
        assertJsonEquals(featureWithZoneJson, feature.toJson())

        val line = buildLineString {
            add(0.0, 0.0)
            add(1.0, 1.0)
            foreignMembers = buildJsonObject { put("title", "center") }
        }
        assertEquals(JsonPrimitive("center"), line.foreignMembers["title"])

        val multiPoint = buildMultiPoint {
            add(1.0, 2.0)
            foreignMembers = buildJsonObject { put("title", "points") }
        }
        assertEquals(JsonPrimitive("points"), multiPoint.foreignMembers["title"])

        val collection =
            buildFeatureCollection<Nothing?, Nothing?> {
                foreignMembers = buildJsonObject { put("title", "All zones") }
            }
        assertEquals(JsonPrimitive("All zones"), collection.foreignMembers["title"])
    }
}
