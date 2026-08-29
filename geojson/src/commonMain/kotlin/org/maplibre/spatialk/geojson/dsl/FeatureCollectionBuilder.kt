package org.maplibre.spatialk.geojson.dsl

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.EmptyForeignMembers
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Geometry

/**
 * Builder for constructing [FeatureCollection] objects using a DSL.
 *
 * @property bbox An optional [BoundingBox] for this [FeatureCollection].
 * @property foreignMembers Members not defined by RFC 7946.
 * @see FeatureCollection
 * @see buildFeatureCollection
 * @see addFeature
 */
@GeoJsonDsl
public class FeatureCollectionBuilder<G : Geometry?, P : @Serializable Any?> {
    public var bbox: BoundingBox? = null
    public var foreignMembers: JsonObject = EmptyForeignMembers
    private val features: MutableList<Feature<G, P>> = mutableListOf()

    /**
     * Adds a [Feature] to this [FeatureCollection].
     *
     * @param feature The [Feature] to add.
     */
    public fun add(feature: Feature<G, P>) {
        features.add(feature)
    }

    /**
     * Builds the [FeatureCollection] from the configured values.
     *
     * @return The constructed [FeatureCollection].
     */
    public fun build(): FeatureCollection<G, P> = FeatureCollection(features, bbox, foreignMembers)
}
