package org.maplibre.spatialk.geojson

import kotlin.jvm.JvmStatic
import kotlinx.serialization.Serializable
import org.intellij.lang.annotations.Language
import org.maplibre.spatialk.geojson.serialization.MultiGeometrySerializer

/**
 * A [Geometry] that contains multiple homogenous points, curves, or surfaces, i.e. a union type for
 * [MultiPoint], [MultiLineString], and [MultiPolygon].
 */
@Serializable(with = MultiGeometrySerializer::class)
public sealed interface MultiGeometry : Geometry {
    public companion object {
        @JvmStatic
        public fun fromJson(@Language("json") json: String): MultiGeometry =
            GeoJson.decodeFromString(json)

        @JvmStatic
        public fun fromJsonOrNull(@Language("json") json: String): MultiGeometry? =
            GeoJson.decodeFromStringOrNull(json)

        @JvmStatic public fun toJson(multiGeometry: MultiGeometry): String = multiGeometry.toJson()
    }
}
