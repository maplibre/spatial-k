package org.maplibre.spatialk.geojson

import kotlin.jvm.JvmStatic
import kotlinx.serialization.Serializable
import org.intellij.lang.annotations.Language
import org.maplibre.spatialk.geojson.serialization.PointGeometrySerializer

/**
 * A [Geometry] that contains a single or multiple points, i.e. a union type for [Point] and
 * [MultiPoint].
 */
@Serializable(with = PointGeometrySerializer::class)
public sealed interface PointGeometry : Geometry {
    public companion object {
        @JvmStatic
        public fun fromJson(@Language("json") json: String): PointGeometry =
            GeoJson.decodeFromString(json)

        @JvmStatic
        public fun fromJsonOrNull(@Language("json") json: String): PointGeometry? =
            GeoJson.decodeFromStringOrNull(json)

        @JvmStatic public fun toJson(pointGeometry: PointGeometry): String = pointGeometry.toJson()
    }
}
