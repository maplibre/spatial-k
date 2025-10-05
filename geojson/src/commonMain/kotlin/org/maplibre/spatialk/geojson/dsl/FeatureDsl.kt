@file:JvmSynthetic

package org.maplibre.spatialk.geojson.dsl

import kotlin.jvm.JvmSynthetic
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.buildJsonObject
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.Geometry

@GeoJsonDsl
public fun <T : Geometry?> feature(
    geometry: T,
    id: String? = null,
    bbox: BoundingBox? = null,
    properties: (JsonObjectBuilder.() -> Unit)? = null,
): Feature<T> = Feature(geometry, properties?.let { buildJsonObject { properties() } }, id, bbox)

@GeoJsonDsl
public fun feature(
    id: String? = null,
    bbox: BoundingBox? = null,
    properties: (JsonObjectBuilder.() -> Unit)? = null,
): Feature<Nothing?> = Feature(null, properties?.let { buildJsonObject { properties() } }, id, bbox)
