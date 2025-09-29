package org.maplibre.spatialk.geojson

import kotlin.jvm.JvmStatic
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import org.intellij.lang.annotations.Language
import org.maplibre.spatialk.geojson.serialization.GeoJson

/**
 * A GeoJSON object represents a [Geometry], [Feature], or
 * [collection of Features][FeatureCollection].
 *
 * @property bbox An optional bounding box used to represent the limits of the object's geometry.
 */
@Serializable
public sealed interface GeoJsonObject {
    public val bbox: BoundingBox?

    /** @return A JSON representation of this object. */
    public fun json(): String

    public companion object {
        @JvmStatic
        public fun fromJson(@Language("json") json: String): GeoJsonObject =
            GeoJson.decodeFromString(serializer(), json)

        internal inline fun <reified T : GeoJsonObject> fromJson(
            @Language("json") json: String
        ): T =
            try {
                GeoJson.decodeFromString(serializer(), json) as T
            } catch (_: ClassCastException) {
                throw SerializationException("Object is not a ${T::class.simpleName}")
            }
    }
}
