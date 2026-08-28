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
 * Decodes [foreignMembers] as [T].
 *
 * @return The decoded foreign members.
 * @throws SerializationException if [foreignMembers] cannot be decoded as [T].
 */
@HiddenFromObjC
public inline fun <reified T : Any> GeoJsonObject.decodeForeignMembers(): T =
    GeoJson.jsonFormat.decodeFromJsonElement(foreignMembers)

/**
 * Encodes this object as a [JsonObject] for [GeoJsonObject.foreignMembers].
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
