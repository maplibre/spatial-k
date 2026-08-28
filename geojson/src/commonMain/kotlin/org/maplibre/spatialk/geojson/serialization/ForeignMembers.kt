package org.maplibre.spatialk.geojson.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import org.maplibre.spatialk.geojson.EmptyForeignMembers

internal val GeometryCoordinateSchemaKeys: Set<String> = setOf("type", "bbox", "coordinates")
internal val GeometryCoordinateForbiddenKeys: Set<String> =
    setOf("geometry", "properties", "features")
internal val GeometryCoordinateReservedKeys: Set<String> =
    GeometryCoordinateSchemaKeys + GeometryCoordinateForbiddenKeys

internal val GeometryCollectionSchemaKeys: Set<String> = setOf("type", "bbox", "geometries")
internal val GeometryCollectionForbiddenKeys: Set<String> =
    setOf("geometry", "properties", "features")
internal val GeometryCollectionReservedKeys: Set<String> =
    GeometryCollectionSchemaKeys + GeometryCollectionForbiddenKeys

internal val FeatureSchemaKeys: Set<String> = setOf("type", "bbox", "geometry", "properties", "id")
internal val FeatureForbiddenKeys: Set<String> = setOf("coordinates", "geometries", "features")
internal val FeatureReservedKeys: Set<String> = FeatureSchemaKeys + FeatureForbiddenKeys

internal val FeatureCollectionSchemaKeys: Set<String> = setOf("type", "bbox", "features")
internal val FeatureCollectionForbiddenKeys: Set<String> =
    setOf("coordinates", "geometries", "geometry", "properties")
internal val FeatureCollectionReservedKeys: Set<String> =
    FeatureCollectionSchemaKeys + FeatureCollectionForbiddenKeys

internal fun validateForeignMembers(foreignMembers: JsonObject, reserved: Set<String>) {
    val conflict = foreignMembers.keys.firstOrNull { it in reserved }
    require(conflict == null) {
        "Foreign member \"$conflict\" conflicts with a reserved GeoJSON member name"
    }
}

internal fun JsonObject.extractForeignMembers(
    schema: Set<String>,
    forbidden: Set<String>,
): JsonObject {
    val extras = filterKeys { it !in schema }
    val conflict = extras.keys.firstOrNull { it in forbidden }
    if (conflict != null) {
        throw SerializationException(
            "Foreign member \"$conflict\" conflicts with a reserved GeoJSON member name"
        )
    }
    return if (extras.isEmpty()) EmptyForeignMembers else JsonObject(extras)
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
