@file:JvmName("FeatureConversion")
@file:JvmMultifileClass

package org.maplibre.spatialk.turf.featureconversion

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Geometry
import org.maplibre.spatialk.turf.coordinatemutation.flattenCoordinates
import org.maplibre.spatialk.turf.measurement.computeBbox

/**
 * Returns a [Feature] containing a [Geometry] by applying the given [transform] function to the
 * original geometry. The original feature's properties are preserved in the result.
 *
 * If the original feature has a [Feature.bbox], then the resulting feature has a new `bbox`
 * computed using the new geometry, if present.
 */
public fun <T : Geometry?, U : Geometry?, P> Feature<T, P>.mapGeometry(
    transform: (T) -> U
): Feature<U, P> {
    val newGeometry = transform(geometry)
    return Feature(
        geometry = newGeometry,
        properties = properties,
        id = id,
        bbox = this.bbox?.let { newGeometry?.computeBbox() ?: it },
    )
}

/** Returns a [FeatureCollection] by applying [mapGeometry] to each feature in this collection. */
public fun <T : Geometry?, U : Geometry?, P> FeatureCollection<T, P>.mapGeometry(
    transform: (T) -> U
): FeatureCollection<U, P> {
    val newFeatures: List<Feature<U, P>> = features.map { it.mapGeometry(transform) }
    return FeatureCollection(
        features = newFeatures,
        bbox = bbox?.let { computeBbox(newFeatures.flatMap { it.flattenCoordinates() }) },
    )
}
