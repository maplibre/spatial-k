@file:JvmName("Meta")
@file:JvmMultifileClass

package org.maplibre.spatialk.turf.meta

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.GeoJsonObject
import org.maplibre.spatialk.geojson.Geometry
import org.maplibre.spatialk.turf.measurement.computeBbox

/**
 * Returns a [Feature] containing a [Geometry] by applying the given [transform] function to the
 * original geometry. The original feature's properties are preserved in the result.
 *
 * If the original geometry is `null`, then the resulting feature also has Geometry `null`.
 *
 * If the original feature has a [Feature.bbox], then the resulting feature has a new `bbox`
 * computed using the new geometry.
 */
public fun <T : Geometry, U : Geometry> Feature<T>.mapGeometry(transform: (T) -> U): Feature<U> {
    val newGeometry = geometry?.let { transform(it) }
    return Feature(
        geometry = newGeometry,
        properties = properties,
        id = id,
        bbox = this.bbox?.let { newGeometry?.computeBbox() ?: it },
    )
}

/** Returns a [FeatureCollection] by applying [mapGeometry] to each feature in this collection. */
public fun FeatureCollection.mapGeometry(transform: (Geometry) -> Geometry): GeoJsonObject {
    val newFeatures = features.map { it.mapGeometry(transform) }
    return FeatureCollection(
        features = newFeatures,
        bbox = bbox?.let { computeBbox(newFeatures.flatMap { it.flattenCoordinates() }) },
    )
}
