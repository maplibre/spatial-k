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
public class Feature<out T : Geometry>(
    public val geometry: T?,
    public val properties: JsonObject? = null,
    public val id: String? = null,
    override val bbox: BoundingBox? = null,
) : GeoJsonObject {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Feature<*>

        if (geometry != other.geometry) return false
        if (id != other.id) return false
        if (bbox != other.bbox) return false
        if (properties != other.properties) return false

        return true
    }

    override fun hashCode(): Int {
        var result = geometry?.hashCode() ?: 0
        result = 31 * result + (id?.hashCode() ?: 0)
        result = 31 * result + (bbox?.hashCode() ?: 0)
        result = 31 * result + properties.hashCode()
        return result
    }

    public operator fun component1(): Geometry? = geometry

    public operator fun component2(): JsonObject? = properties

    public operator fun component3(): String? = id

    public operator fun component4(): BoundingBox? = bbox

    override fun toString(): String = json()

    override fun json(): String = GeoJson.encodeToString(this)

    @Suppress("UNCHECKED_CAST")
    public fun <T : Geometry> xcopy(
        geometry: Geometry? = this.geometry,
        properties: JsonObject? = this.properties,
        id: String? = this.id,
        bbox: BoundingBox? = this.bbox,
    ): Feature<T> = Feature(geometry as T, properties, id, bbox)

    public companion object {
        @JvmStatic
        public fun <T : Geometry> fromJson(@Language("json") json: String): Feature<T> =
            GeoJsonObject.fromJson<Feature<T>>(json)

        @JvmStatic
        public fun <T : Geometry> fromJsonOrNull(json: String): Feature<T>? =
            try {
                fromJson(json)
            } catch (_: Exception) {
                null
            }
    }
}
