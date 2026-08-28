package org.maplibre.spatialk.geojson.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import org.maplibre.spatialk.geojson.EmptyForeignMembers

internal val GeometryCoordinateReservedKeys: Set<String> =
    setOf("type", "bbox", "coordinates", "geometry", "properties", "features")

internal val GeometryCollectionReservedKeys: Set<String> =
    setOf("type", "bbox", "geometries", "geometry", "properties", "features")

internal val FeatureReservedKeys: Set<String> =
    setOf("type", "bbox", "geometry", "properties", "id", "coordinates", "geometries", "features")

internal val FeatureCollectionReservedKeys: Set<String> =
    setOf("type", "bbox", "features", "coordinates", "geometries", "geometry", "properties")

internal fun validateForeignMembers(foreignMembers: JsonObject, reserved: Set<String>) {
    val conflict = foreignMembers.keys.firstOrNull { it in reserved }
    require(conflict == null) {
        "Foreign member \"$conflict\" conflicts with a reserved GeoJSON member name"
    }
}

internal fun JsonObject.without(reserved: Set<String>): JsonObject {
    val remaining = filterKeys { it !in reserved }
    return if (remaining.isEmpty()) EmptyForeignMembers else JsonObject(remaining)
}

internal fun encodeGeoJsonObject(
    encoder: JsonEncoder,
    specObject: JsonObject,
    foreignMembers: JsonObject,
) {
    val element =
        if (foreignMembers.isEmpty()) specObject else JsonObject(specObject + foreignMembers)
    encoder.encodeJsonElement(element)
}

internal fun decodeGeoJsonObject(decoder: JsonDecoder): JsonObject {
    val element = decoder.decodeJsonElement()
    return element as? JsonObject
        ?: throw SerializationException("Expected a JSON object but found $element")
}

@OptIn(ExperimentalSerializationApi::class)
internal fun JsonObject.requireMember(key: String, serialName: String): JsonElement =
    this[key] ?: throw MissingFieldException(key, serialName)
