@file:JvmName("FeatureConversion")
@file:JvmMultifileClass

package org.maplibre.spatialk.turf.featureconversion

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Geometry

public fun <T : Geometry?, P1, P2> Feature<T, P1>.mapProperties(
    transform: (P1) -> P2
): Feature<T, P2> =
    Feature(geometry = geometry, properties = transform(properties), id = id, bbox = bbox)

public fun <T : Geometry?, P1, P2> FeatureCollection<T, P1>.mapProperties(
    transform: (P1) -> P2
): FeatureCollection<T, P2> =
    FeatureCollection(features = features.map { it.mapProperties(transform) }, bbox = bbox)
