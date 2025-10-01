package org.maplibre.spatialk.geojson

import kotlin.jvm.JvmStatic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.intellij.lang.annotations.Language

/**
 * A FeatureCollection object is a collection of [Feature] objects. This class implements the
 * [Collection] interface and can be used as a Collection directly. The list of features contained
 * in this collection is also accessible through the [features] property.
 *
 * @property features The collection of [Feature] objects stored in this collection
 * @see <a href="https://tools.ietf.org/html/rfc7946#section-3.3">
 *   https://tools.ietf.org/html/rfc7946#section-3.2</a>
 */
@Serializable
@SerialName("FeatureCollection")
public data class FeatureCollection(
    public val features: List<Feature<*>> = emptyList(),
    override val bbox: BoundingBox? = null,
) : Collection<Feature<*>> by features, GeoJsonObject {
    public constructor(
        vararg features: Feature<*>,
        bbox: BoundingBox? = null,
    ) : this(features.toMutableList(), bbox)

    public companion object {
        @JvmStatic
        @OptIn(SensitiveGeoJsonApi::class)
        public fun fromJson(@Language("json") json: String): FeatureCollection =
            GeoJson.decodeFromString(json)

        @JvmStatic
        @OptIn(SensitiveGeoJsonApi::class)
        public fun fromJsonOrNull(@Language("json") json: String): FeatureCollection? =
            GeoJson.decodeFromStringOrNull(json)
    }
}
