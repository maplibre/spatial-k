package org.maplibre.spatialk.geojson

import kotlin.jvm.JvmStatic
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import org.intellij.lang.annotations.Language
import org.maplibre.spatialk.geojson.serialization.GeoJson

/**
 * A Geometry object represents points, curves, and surfaces in coordinate space.
 *
 * @see <a href="https://tools.ietf.org/html/rfc7946#section-3.1">
 *   https://tools.ietf.org/html/rfc7946#section-3.1</a>
 * @see GeometryCollection
 */
@Serializable
@JsonClassDiscriminator("type")
public sealed class Geometry() : GeoJsonObject {
    abstract override val bbox: BoundingBox?

    override fun toString(): String = json()

    public companion object {
        @JvmStatic
        public fun fromJson(@Language("json") json: String): Geometry =
            GeoJson.decodeFromString(json)

        @JvmStatic
        internal inline fun <reified T : Geometry> fromJson(@Language("json") json: String): T =
            GeoJsonObject.fromJson<T>(json)

        @JvmStatic
        public fun fromJsonOrNull(@Language("json") json: String): Geometry? =
            try {
                fromJson(json)
            } catch (_: Exception) {
                null
            }
    }
}
