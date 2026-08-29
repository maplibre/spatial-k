package org.maplibre.spatialk.geojson

import kotlin.jvm.JvmStatic
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import org.intellij.lang.annotations.Language
import org.maplibre.spatialk.geojson.serialization.GeoJsonObjectSerializer

/**
 * A [GeoJsonObject] represents a [Geometry], [Feature], or [FeatureCollection].
 *
 * @property bbox An optional [BoundingBox] used to represent the limits of the object's [Geometry].
 * @property foreignMembers Additional members as defined in
 *   [RFC 7946 §6.1](https://tools.ietf.org/html/rfc7946#section-6.1). Empty when none are present.
 *   JSON encode flattens these as sibling keys. CBOR and Protobuf drop them.
 */
@Serializable(with = GeoJsonObjectSerializer::class)
public sealed interface GeoJsonObject {
    public val bbox: BoundingBox?

    public val foreignMembers: JsonObject

    /** Factory methods for creating and serializing [GeoJsonObject] objects. */
    public companion object {
        /**
         * Returns the foreign member with the given [key], or null if it is not present.
         *
         * @param key The foreign member name.
         * @return The value, or null if [key] is not present.
         * @receiver The object to read from.
         */
        @JvmStatic
        public fun GeoJsonObject.getForeignMember(key: String): JsonElement? = foreignMembers[key]

        /**
         * Decodes a JSON string into a [GeoJsonObject].
         *
         * @param json The JSON string to decode.
         * @return The decoded [GeoJsonObject].
         * @throws SerializationException if the JSON string is invalid or cannot be deserialized.
         * @throws IllegalArgumentException if the JSON contains an invalid [GeoJsonObject].
         */
        @JvmStatic
        public fun fromJson(@Language("json") json: String): GeoJsonObject =
            GeoJson.decodeFromString(json)

        /**
         * Decodes a JSON string into a [GeoJsonObject], or returns null if deserialization fails.
         *
         * @param json The JSON string to decode.
         * @return The decoded [GeoJsonObject], or null if the string could not be deserialized.
         */
        @JvmStatic
        public fun fromJsonOrNull(@Language("json") json: String): GeoJsonObject? =
            GeoJson.decodeFromStringOrNull(json)

        /**
         * Encodes a [GeoJsonObject] into a JSON string.
         *
         * The restrictions described in [org.maplibre.spatialk.geojson.toJson] apply.
         *
         * @param geoJsonObject The object to encode.
         * @return The encoded JSON string.
         * @throws SerializationException if serialization fails.
         */
        @JvmStatic public fun toJson(geoJsonObject: GeoJsonObject): String = geoJsonObject.toJson()
    }
}
