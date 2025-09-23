package org.maplibre.spatialk.geojson.serialization

import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.Position
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

internal fun DoubleArray.jsonJoin(transform: ((Double) -> CharSequence)? = null) =
    joinToString(separator = ",", prefix = "[", postfix = "]", transform = transform)

internal fun <T> Iterable<T>.jsonJoin(transform: ((T) -> CharSequence)? = null) =
    joinToString(separator = ",", prefix = "[", postfix = "]", transform = transform)

internal fun BoundingBox?.jsonProp(): String = if (this == null) "" else """"bbox":${this.json()},"""

internal fun Feature.idProp(): String = if (this.id == null) "" else """"id":"${this.id}","""

internal fun JsonArray.toPosition(): Position =
    Position(this[0].jsonPrimitive.double, this[1].jsonPrimitive.double, this.getOrNull(2)?.jsonPrimitive?.double)

internal fun JsonArray.toBbox(): BoundingBox =
    BoundingBox(this.map { it.jsonPrimitive.double }.toDoubleArray())
