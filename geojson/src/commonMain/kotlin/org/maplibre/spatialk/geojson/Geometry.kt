package org.maplibre.spatialk.geojson

import kotlin.jvm.JvmStatic
import kotlinx.serialization.Serializable
import org.intellij.lang.annotations.Language
import org.maplibre.spatialk.geojson.serialization.GeometrySerializer

/**
 * A Geometry object represents points, curves, and surfaces in coordinate space.
 *
 * @see <a href="https://tools.ietf.org/html/rfc7946#section-3.1">
 *   https://tools.ietf.org/html/rfc7946#section-3.1</a>
 * @see GeometryCollection
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
