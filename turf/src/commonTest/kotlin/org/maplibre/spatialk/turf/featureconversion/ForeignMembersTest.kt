package org.maplibre.spatialk.turf.featureconversion

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Point
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
        assertEquals(Point(0.0, 0.0), mapped.geometry)
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
}
