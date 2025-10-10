package org.maplibre.spatialk.geojson

import kotlin.jvm.JvmStatic
import kotlinx.serialization.Serializable
import org.intellij.lang.annotations.Language
import org.maplibre.spatialk.geojson.serialization.GeometrySerializer
import org.maplibre.spatialk.geojson.serialization.LineStringGeometrySerializer
import org.maplibre.spatialk.geojson.serialization.MultiGeometrySerializer
import org.maplibre.spatialk.geojson.serialization.PointGeometrySerializer
import org.maplibre.spatialk.geojson.serialization.PolygonGeometrySerializer
import org.maplibre.spatialk.geojson.serialization.SingleGeometrySerializer

/**
 * A Geometry object represents points, curves, and surfaces in coordinate space.
 *
 * @see <a href="https://tools.ietf.org/html/rfc7946#section-3.1">
 *   https://tools.ietf.org/html/rfc7946#section-3.1</a>
 */
@Serializable(with = GeometrySerializer::class)
public sealed interface Geometry : GeoJsonObject {
    public companion object {
        @JvmStatic
        public fun fromJson(@Language("json") json: String): Geometry =
            GeoJson.decodeFromString(json)

        @JvmStatic
        public fun fromJsonOrNull(@Language("json") json: String): Geometry? =
            GeoJson.decodeFromStringOrNull(json)
    }
}

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
    }
}

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
    }
}

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
    }
}

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
