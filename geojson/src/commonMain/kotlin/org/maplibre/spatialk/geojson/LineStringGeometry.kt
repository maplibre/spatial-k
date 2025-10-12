package org.maplibre.spatialk.geojson

import kotlin.jvm.JvmStatic
import kotlinx.serialization.Serializable
import org.intellij.lang.annotations.Language
import org.maplibre.spatialk.geojson.serialization.LineStringGeometrySerializer

/**
 * A [Geometry] that contains a single or multiple curves, i.e. a union type for [LineString] and
 * [MultiLineString].
 */
@Serializable(with = LineStringGeometrySerializer::class)
public sealed interface LineStringGeometry : Geometry {
    public companion object {
        @JvmStatic
        public fun fromJson(@Language("json") json: String): LineStringGeometry =
            GeoJson.decodeFromString(json)

        @JvmStatic
        public fun fromJsonOrNull(@Language("json") json: String): LineStringGeometry? =
            GeoJson.decodeFromStringOrNull(json)
    }
}
