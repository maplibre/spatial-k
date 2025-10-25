package org.maplibre.spatialk.geojson.dsl

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureId
import org.maplibre.spatialk.geojson.Geometry

/**
 * Builder for constructing [Feature] objects using a DSL.
 *
 * @param G The type of [Geometry] associated with this [Feature].
 * @param P The type of properties. This can be any type that serializes to a JSON object. For
 *   dynamic or unknown property schemas, use [JsonObject]. For known schemas, use a [Serializable]
 *   data class.
 * @property geometry The [Geometry] associated with this [Feature].
 * @property properties Additional properties about this [Feature].
 * @property id An optional identifier for this [Feature].
 * @property bbox An optional [BoundingBox] for this [Feature].
 * @see Feature
 * @see buildFeature
 */
@GeoJsonDsl
public class FeatureBuilder<G : Geometry?, P : @Serializable Any?>(
    public var geometry: G,
    public var properties: P,
) {
    public var id: FeatureId? = null
    public var bbox: BoundingBox? = null

    /**
     * Sets the Feature identifier using a string value.
     *
     * @param value The string identifier value.
     */
    public fun setId(value: String) {
        this.id = JsonPrimitive(value)
    }

    /**
     * Sets the Feature identifier using a long value.
     *
     * @param value The long integer identifier value.
     */
    public fun setId(value: Long) {
        this.id = JsonPrimitive(value)
    }

    /**
     * Sets the Feature identifier using a double value.
     *
     * @param value The double identifier value.
     */
    public fun setId(value: Double) {
        this.id = JsonPrimitive(value)
    }

    /**
     * Builds the [Feature] from the configured values.
     *
     * @return The constructed [Feature].
     */
    public fun build(): Feature<G, P> {
        return Feature(geometry, properties, id, bbox)
    }
}
