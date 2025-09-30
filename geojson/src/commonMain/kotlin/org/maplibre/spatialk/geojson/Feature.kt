package org.maplibre.spatialk.geojson

import kotlin.jvm.JvmStatic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import org.intellij.lang.annotations.Language
import org.maplibre.spatialk.geojson.serialization.GeoJson

/**
 * A feature object represents a spatially bounded thing.
 *
 * @property geometry A [Geometry] object contained within the feature.
 * @property properties Additional properties about this feature. When serialized, any non-simple
 *   types will be serialized into JSON objects.
 * @property id An optionally included string that commonly identifies this feature.
 * @see <a href="https://tools.ietf.org/html/rfc7946#section-3.2">
 *   https://tools.ietf.org/html/rfc7946#section-3.2</a>
 * @see FeatureCollection
 */
@Serializable
@SerialName("Feature")
public data class Feature<out T : Geometry>(
    public val geometry: T?,
    public val properties: JsonObject? = null,
    public val id: String? = null,
    override val bbox: BoundingBox? = null,
) : GeoJsonObject {
    override fun json(): String = GeoJson.encodeToString(this)

    public companion object {
        @JvmStatic
        public fun <T : Geometry> fromJson(@Language("json") json: String): Feature<T> =
            GeoJsonObject.fromJson<Feature<T>>(json)

        @JvmStatic
        public fun <T : Geometry> fromJsonOrNull(@Language("json") json: String): Feature<T>? =
            try {
                fromJson(json)
            } catch (_: Exception) {
                null
            }
    }
}
