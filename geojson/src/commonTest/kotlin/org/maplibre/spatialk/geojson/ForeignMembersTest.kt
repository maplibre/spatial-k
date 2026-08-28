package org.maplibre.spatialk.geojson

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlinx.serialization.Serializable
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

    @Serializable private data class GofsZoneMembers(val zone_id: String)

    @Serializable private data class NameProp(val name: String)

    @Language("json")
    private val gofsFeatureJson =
        """
        {
            "type": "Feature",
            "geometry": { "type": "Point", "coordinates": [-75.0, 45.0] },
            "properties": { "name": "Station" },
            "zone_id": "zone-123"
        }
        """

    @Language("json")
    private val gofsFeatureCollectionJson =
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
    fun gofsFeatureZoneId() {
        val feature = Feature.fromJson<Point, NameProp>(gofsFeatureJson)
        assertEquals("Station", feature.properties.name)
        assertEquals(JsonPrimitive("zone-123"), feature.foreignMembers["zone_id"])
        assertEquals(JsonPrimitive("zone-123"), feature.getForeignMember("zone_id"))
        assertEquals(GofsZoneMembers("zone-123"), feature.decodeForeignMembers<GofsZoneMembers>())

        val collection = FeatureCollection.fromJson<Point, NameProp>(gofsFeatureCollectionJson)
        assertEquals(
            GofsZoneMembers("zone-123"),
            collection.features.single().decodeForeignMembers<GofsZoneMembers>(),
        )
    }

    @Test
    fun roundTripPreservesZoneId() {
        val decoded = Feature.fromJson<Point, NameProp>(gofsFeatureJson)
        assertJsonEquals(gofsFeatureJson, decoded.toJson())
    }

    @Test
    fun writePathFlattensForeignMembers() {
        val feature =
            Feature(
                geometry = Point(-75.0, 45.0),
                properties = NameProp("Station"),
                foreignMembers = GofsZoneMembers("zone-123").toForeignMembers(),
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
        assertJsonEquals(gofsFeatureJson, fromBuilder.toJson())
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
        assertIs<JsonObject>(feature.foreignMembers["centerline"])
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
    fun emptyBagOmittedFromJson() {
        val json = Point(12.3, 45.6).toJson()
        assertFalse(json.contains("foreignMembers"))
        assertEquals("""{"type":"Point","coordinates":[12.3,45.6]}""", json)
        assertEquals(EmptyForeignMembers, Point(12.3, 45.6).foreignMembers)
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
        val asObject = GeoJsonObject.fromJson(gofsFeatureJson)
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
                foreignMembers = GofsZoneMembers("zone-123").toForeignMembers()
            }
        assertEquals(GofsZoneMembers("zone-123"), feature.decodeForeignMembers<GofsZoneMembers>())
        assertJsonEquals(gofsFeatureJson, feature.toJson())

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
