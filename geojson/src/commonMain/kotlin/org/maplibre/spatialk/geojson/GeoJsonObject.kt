package org.maplibre.spatialk.geojson

import kotlin.jvm.JvmStatic
import kotlinx.serialization.Serializable
import org.intellij.lang.annotations.Language
import org.maplibre.spatialk.geojson.serialization.GeoJsonObjectSerializer

/**
 * A GeoJSON object represents a [Geometry], [Feature], or
 * [collection of Features][FeatureCollection].
 *
 * @property bbox An optional bounding box used to represent the limits of the object's geometry.
 */
@Serializable(with = GeoJsonObjectSerializer::class)
public sealed interface GeoJsonObject : GeoJsonElement {
    public val bbox: BoundingBox?

    public companion object {
        @JvmStatic
        public fun fromJson(@Language("json") json: String): GeoJsonObject =
            GeoJson.decodeFromString(json)

        @JvmStatic
        public fun fromJsonOrNull(@Language("json") json: String): GeoJsonObject? =
            GeoJson.decodeFromStringOrNull(json)
    }
}
