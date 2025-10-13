package org.maplibre.spatialk.geojson

import kotlin.jvm.JvmStatic
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import org.intellij.lang.annotations.Language
import org.maplibre.spatialk.geojson.serialization.GeometrySerializer

/**
 * A [Geometry] object represents points, curves, and surfaces in coordinate space.
 *
 * See [RFC 7946 Section 3.1](https://tools.ietf.org/html/rfc7946#section-3.1) for the full
 * specification.
 */
@Serializable(with = GeometrySerializer::class)
public sealed interface Geometry : GeoJsonObject {
    /** Factory methods for creating and serializing [Geometry] objects. */
    public companion object {
        /**
         * Decodes a JSON string into a [Geometry].
         *
         * @param json The JSON string to decode.
         * @return The decoded [Geometry].
         * @throws SerializationException if the JSON string is invalid or cannot be deserialized.
         * @throws IllegalArgumentException if the JSON contains an invalid [Geometry].
         */
        @JvmStatic
        public fun fromJson(@Language("json") json: String): Geometry =
            GeoJson.decodeFromString(json)

        /**
         * Decodes a JSON string into a [Geometry], or returns null if deserialization fails.
         *
         * @param json The JSON string to decode.
         * @return The decoded [Geometry], or null if the string could not be deserialized.
         */
        @JvmStatic
        public fun fromJsonOrNull(@Language("json") json: String): Geometry? =
            GeoJson.decodeFromStringOrNull(json)

        /**
         * Encodes a [Geometry] into a JSON string.
         *
         * @param geometry The object to encode.
         * @return The encoded JSON string.
         */
        @JvmStatic public fun toJson(geometry: Geometry): String = geometry.toJson()
    }
}
