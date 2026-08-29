package org.maplibre.spatialk.turf.featureconversion

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.GeometryCollection
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.testutil.assertPositionEquals
import org.maplibre.spatialk.turf.measurement.withComputedBbox

class ForeignMembersTest {

    private val zoneMembers = buildJsonObject { put("zone_id", "zone-123") }

    private val feature =
        Feature(geometry = Point(-75.0, 45.0), properties = null, foreignMembers = zoneMembers)

    @Test
    fun mapPropertiesPreservesForeignMembers() {
        val mapped = feature.mapProperties { "renamed" }
        assertEquals("renamed", mapped.properties)
        assertEquals(JsonPrimitive("zone-123"), mapped.foreignMembers["zone_id"])
    }

    @Test
    fun mapGeometryPreservesForeignMembers() {
        val mapped = feature.mapGeometry { Point(0.0, 0.0) }
        assertPositionEquals(Position(0.0, 0.0), mapped.geometry.coordinates)
        assertEquals(JsonPrimitive("zone-123"), mapped.foreignMembers["zone_id"])
    }

    @Test
    fun featureCollectionMapsPreserveForeignMembers() {
        val collection =
            FeatureCollection(
                listOf(feature),
                foreignMembers = buildJsonObject { put("title", "zones") },
            )
        val byProperties = collection.mapProperties { 1 }
        assertEquals(JsonPrimitive("zones"), byProperties.foreignMembers["title"])
        assertEquals(
            JsonPrimitive("zone-123"),
            byProperties.features.single().foreignMembers["zone_id"],
        )

        val byGeometry = collection.mapGeometry { Point(1.0, 1.0) }
        assertEquals(JsonPrimitive("zones"), byGeometry.foreignMembers["title"])
        assertEquals(
            JsonPrimitive("zone-123"),
            byGeometry.features.single().foreignMembers["zone_id"],
        )
    }

    @Test
    fun withComputedBboxPreservesForeignMembers() {
        val withBbox = feature.withComputedBbox()
        assertEquals(JsonPrimitive("zone-123"), withBbox.foreignMembers["zone_id"])
        val collection =
            FeatureCollection(
                    listOf(feature),
                    foreignMembers = buildJsonObject { put("title", "zones") },
                )
                .withComputedBbox()
        assertEquals(JsonPrimitive("zones"), collection.foreignMembers["title"])
        assertEquals(
            JsonPrimitive("zone-123"),
            collection.features.single().foreignMembers["zone_id"],
        )
    }

    @Test
    fun singleToMultiPreservesForeignMembers() {
        val point = Point(-75.0, 45.0, foreignMembers = zoneMembers)
        val multi = point.toMultiPoint()
        assertEquals(JsonPrimitive("zone-123"), multi.foreignMembers["zone_id"])
        assertPositionEquals(Position(-75.0, 45.0), multi.coordinates.single())
    }

    @Test
    fun collectionConversionsPreserveForeignMembers() {
        val title = buildJsonObject { put("title", "zones") }
        val bbox = BoundingBox(-75.0, 45.0, -75.0, 45.0)
        val collection = FeatureCollection(listOf(feature), bbox = bbox, foreignMembers = title)
        val geometries = collection.toGeometryCollection()
        assertEquals(JsonPrimitive("zones"), geometries.foreignMembers["title"])
        assertEquals(bbox, geometries.bbox)

        val features = geometries.toFeatureCollection { properties = null }
        assertEquals(JsonPrimitive("zones"), features.foreignMembers["title"])
        assertEquals(bbox, features.bbox)
    }

    @Test
    fun explodeAndCombineDropForeignMembers() {
        val point = Point(-75.0, 45.0, foreignMembers = zoneMembers)
        assertTrue(point.explode().foreignMembers.isEmpty())

        val collection = GeometryCollection(point, foreignMembers = zoneMembers)
        assertTrue(collection.combine().foreignMembers.isEmpty())
    }
}
