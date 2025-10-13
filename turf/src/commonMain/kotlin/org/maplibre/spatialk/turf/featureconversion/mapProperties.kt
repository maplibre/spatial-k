@file:JvmName("FeatureConversion")
@file:JvmMultifileClass

package org.maplibre.spatialk.turf.featureconversion

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Geometry

public fun <G : Geometry?, P1, P2> Feature<G, P1>.mapProperties(
    transform: (P1) -> P2
): Feature<G, P2> =
    Feature(geometry = geometry, properties = transform(properties), id = id, bbox = bbox)

public fun <G : Geometry?, P1, P2> FeatureCollection<G, P1>.mapProperties(
    transform: (P1) -> P2
): FeatureCollection<G, P2> =
    FeatureCollection(features = features.map { it.mapProperties(transform) }, bbox = bbox)
