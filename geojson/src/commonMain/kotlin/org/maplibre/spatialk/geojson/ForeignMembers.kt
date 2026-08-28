@file:OptIn(ExperimentalObjCRefinement::class)
@file:JvmSynthetic

package org.maplibre.spatialk.geojson

import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.jvm.JvmSynthetic
import kotlin.native.HiddenFromObjC
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

/** Shared empty foreign-member bag used as the constructor default. */
internal val EmptyForeignMembers: JsonObject = JsonObject(emptyMap())

/**
 * Decodes [foreignMembers] into a typed object.
 *
 * This is **not** [Feature.properties]. Foreign members are leftover JSON keys on the GeoJSON
 * object itself (RFC 7946 §6.1). GeoJSON semantics do not apply to these values: a foreign member
 * that looks like a geometry (RFC example: `centerline`) is not a [Geometry].
 *
 * GOFS example: `decodeForeignMembers<GofsZoneMembers>()` where `@Serializable data class
 * GofsZoneMembers(val zone_id: String)`.
 *
 * @return The decoded foreign members.
 * @throws SerializationException if [foreignMembers] cannot be decoded as [T].
 */
@HiddenFromObjC
public inline fun <reified T : Any> GeoJsonObject.decodeForeignMembers(): T =
    GeoJson.jsonFormat.decodeFromJsonElement(foreignMembers)

/**
 * Encodes this object as a [JsonObject] suitable for [GeoJsonObject.foreignMembers].
 *
 * This is **not** [Feature.properties]. The receiver must serialize to a JSON object, not a
 * primitive or array. Foreign values are not GeoJSON-typed (RFC 7946 §6.1).
 *
 * GOFS example: `GofsZoneMembers(zone_id = "zone-123").toForeignMembers()` where `@Serializable
 * data class GofsZoneMembers(val zone_id: String)`.
 *
 * @return This object encoded as a [JsonObject].
 * @throws SerializationException if this value does not encode to a JSON object.
 */
@HiddenFromObjC
public inline fun <reified T : Any> T.toForeignMembers(): JsonObject {
    val element = GeoJson.jsonFormat.encodeToJsonElement(this)
    return element as? JsonObject
        ?: throw SerializationException(
            "Foreign members must encode to a JSON object, got $element"
        )
}
