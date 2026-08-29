@file:OptIn(ExperimentalObjCRefinement::class)
@file:JvmSynthetic

package org.maplibre.spatialk.geojson

import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.jvm.JvmSynthetic
import kotlin.native.HiddenFromObjC
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

/** Shared empty foreign-member bag used as the constructor default. */
internal val EmptyForeignMembers: JsonObject = JsonObject(emptyMap())

/**
 * Decodes [GeoJsonObject.foreignMembers] as [T].
 *
 * @param json The format used to decode. Defaults to [GeoJson.jsonFormat].
 * @return The decoded foreign members.
 * @throws SerializationException if [GeoJsonObject.foreignMembers] cannot be decoded as [T].
 */
@HiddenFromObjC
public inline fun <reified T : Any> GeoJsonObject.decodeForeignMembers(
    json: Json = GeoJson.jsonFormat
): T = json.decodeFromJsonElement(foreignMembers)

/**
 * Encodes [value] as a [JsonObject] for [GeoJsonObject.foreignMembers].
 *
 * @param value The object to encode. It must serialize to a JSON object.
 * @param json The format used to encode. Defaults to [GeoJson.jsonFormat].
 * @return [value] encoded as a [JsonObject].
 * @throws SerializationException if [value] does not encode to a JSON object.
 */
@HiddenFromObjC
public inline fun <reified T : Any> foreignMembersOf(
    value: T,
    json: Json = GeoJson.jsonFormat,
): JsonObject {
    val element = json.encodeToJsonElement(value)
    return element as? JsonObject
        ?: throw SerializationException(
            "Foreign members must encode to a JSON object, got $element"
        )
}
