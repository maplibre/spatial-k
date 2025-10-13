package org.maplibre.spatialk.geojson

import kotlin.jvm.JvmStatic
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import org.intellij.lang.annotations.Language
import org.maplibre.spatialk.geojson.serialization.LineStringGeometrySerializer

/**
 * A [Geometry] that contains a single or multiple curves, i.e. a union type for [LineString] and
 * [MultiLineString].
 */
@Serializable(with = LineStringGeometrySerializer::class)
public sealed interface LineStringGeometry : Geometry {
    /** Factory methods for creating and serializing [LineStringGeometry] objects. */
    public companion object {
        /**
         * Decodes a JSON string into a [LineStringGeometry].
         *
         * @param json The JSON string to decode.
         * @return The decoded [LineStringGeometry].
         * @throws SerializationException if the JSON string is invalid or cannot be deserialized.
         * @throws IllegalArgumentException if the geometry does not meet structural requirements.
         */
        @JvmStatic
        public fun fromJson(@Language("json") json: String): LineStringGeometry =
            GeoJson.decodeFromString(json)

        /**
         * Decodes a JSON string into a [LineStringGeometry], or returns null if deserialization
         * fails.
         *
         * @param json The JSON string to decode.
         * @return The decoded [LineStringGeometry], or null if the string could not be
         *   deserialized.
         */
        @JvmStatic
        public fun fromJsonOrNull(@Language("json") json: String): LineStringGeometry? =
            GeoJson.decodeFromStringOrNull(json)

        /**
         * Encodes a [LineStringGeometry] into a JSON string.
         *
         * @param lineStringGeometry The object to encode.
         * @return The encoded JSON string.
         */
        @JvmStatic
        public fun toJson(lineStringGeometry: LineStringGeometry): String =
            lineStringGeometry.toJson()
    }
}
