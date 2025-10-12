package org.maplibre.spatialk.geojson

import kotlin.jvm.JvmStatic
import kotlinx.serialization.Serializable
import org.intellij.lang.annotations.Language
import org.maplibre.spatialk.geojson.serialization.PolygonGeometrySerializer

/**
 * A [Geometry] that contains a single or multiple surfaces, i.e. a union type for [Polygon] and
 * [MultiPolygon].
 */
@Serializable(with = PolygonGeometrySerializer::class)
public sealed interface PolygonGeometry : Geometry {
    public companion object {
        @JvmStatic
        public fun fromJson(@Language("json") json: String): PolygonGeometry =
            GeoJson.decodeFromString(json)

        @JvmStatic
        public fun fromJsonOrNull(@Language("json") json: String): PolygonGeometry? =
            GeoJson.decodeFromStringOrNull(json)
    }
}
