package org.maplibre.spatialk.geojson

import kotlin.jvm.JvmStatic
import kotlinx.serialization.Serializable
import org.intellij.lang.annotations.Language
import org.maplibre.spatialk.geojson.serialization.SingleGeometrySerializer

/**
 * A [Geometry] that contains a single point, curve, or surface, i.e. a union type for [Point],
 * [LineString], and [Polygon].
 */
@Serializable(with = SingleGeometrySerializer::class)
public sealed interface SingleGeometry : Geometry {
    public companion object {
        @JvmStatic
        public fun fromJson(@Language("json") json: String): SingleGeometry =
            GeoJson.decodeFromString(json)

        @JvmStatic
        public fun fromJsonOrNull(@Language("json") json: String): SingleGeometry? =
            GeoJson.decodeFromStringOrNull(json)

        @JvmStatic
        public fun toJson(singleGeometry: SingleGeometry): String = singleGeometry.toJson()
    }
}
