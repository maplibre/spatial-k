package org.maplibre.spatialk.geojson

import kotlin.jvm.JvmStatic
import kotlinx.serialization.Serializable
import org.intellij.lang.annotations.Language

/**
 * A GeoJSON object represents a [Geometry], [Feature], or
 * [collection of Features][FeatureCollection].
 *
 * @property bbox An optional bounding box used to represent the limits of the object's geometry.
 */
@Serializable
public sealed interface GeoJsonObject {
    public val bbox: BoundingBox?

    /** @return A GeoJSON representation of this object. */
    public fun toJson(): String = GeoJson.encodeToString(this)

    public companion object {
        @JvmStatic
        @OptIn(SensitiveGeoJsonApi::class)
        public fun fromJson(@Language("json") json: String): GeoJsonObject =
            GeoJson.decodeFromString(json)

        @JvmStatic
        @OptIn(SensitiveGeoJsonApi::class)
        public fun fromJsonOrNull(@Language("json") json: String): GeoJsonObject? =
            GeoJson.decodeFromStringOrNull(json)
    }
}
