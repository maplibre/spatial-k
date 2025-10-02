package org.maplibre.spatialk.geojson

import kotlin.jvm.JvmStatic
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import org.intellij.lang.annotations.Language

/**
 * A Geometry object represents points, curves, and surfaces in coordinate space.
 *
 * @see <a href="https://tools.ietf.org/html/rfc7946#section-3.1">
 *   https://tools.ietf.org/html/rfc7946#section-3.1</a>
 * @see GeometryCollection
 */
@Serializable
@OptIn(ExperimentalSerializationApi::class)
@JsonClassDiscriminator("type")
public sealed interface Geometry : GeoJsonObject {

    public companion object {
        @JvmStatic
        @OptIn(SensitiveGeoJsonApi::class)
        public fun fromJson(@Language("json") json: String): Geometry =
            GeoJson.decodeFromString(json)

        @JvmStatic
        @OptIn(SensitiveGeoJsonApi::class)
        public fun fromJsonOrNull(@Language("json") json: String): Geometry? =
            GeoJson.decodeFromStringOrNull(json)
    }
}
