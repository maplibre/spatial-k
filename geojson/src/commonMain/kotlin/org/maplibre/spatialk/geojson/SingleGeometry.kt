package org.maplibre.spatialk.geojson

import kotlin.jvm.JvmStatic
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import org.intellij.lang.annotations.Language
import org.maplibre.spatialk.geojson.serialization.SingleGeometrySerializer

/**
 * A [Geometry] that contains a single point, curve, or surface, i.e. a union type for [Point],
 * [LineString], and [Polygon].
 */
@Serializable(with = SingleGeometrySerializer::class)
public sealed interface SingleGeometry : Geometry {
    /** Factory methods for creating and serializing [SingleGeometry] objects. */
    public companion object {
        /**
         * Decodes a JSON string into a [SingleGeometry].
         *
         * @param json The JSON string to decode.
         * @return The decoded [SingleGeometry].
         * @throws SerializationException if the JSON string is invalid or cannot be deserialized.
         * @throws IllegalArgumentException if the geometry does not meet structural requirements.
         */
        @JvmStatic
        public fun fromJson(@Language("json") json: String): SingleGeometry =
            GeoJson.decodeFromString(json)

        /**
         * Decodes a JSON string into a [SingleGeometry], or returns null if deserialization fails.
         *
         * @param json The JSON string to decode.
         * @return The decoded [SingleGeometry], or null if the string could not be deserialized.
         */
        @JvmStatic
        public fun fromJsonOrNull(@Language("json") json: String): SingleGeometry? =
            GeoJson.decodeFromStringOrNull(json)

        /**
         * Encodes a [SingleGeometry] into a JSON string.
         *
         * @param singleGeometry The object to encode.
         * @return The encoded JSON string.
         */
        @JvmStatic
        public fun toJson(singleGeometry: SingleGeometry): String = singleGeometry.toJson()
    }
}
