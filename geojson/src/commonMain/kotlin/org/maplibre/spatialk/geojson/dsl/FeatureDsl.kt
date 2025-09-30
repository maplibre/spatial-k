@file:JvmSynthetic

package org.maplibre.spatialk.geojson.dsl

import kotlin.jvm.JvmSynthetic
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.buildJsonObject
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.Geometry

@GeoJsonDsl
public inline fun <reified T : Geometry> feature(
    geometry: T? = null,
    id: String? = null,
    bbox: BoundingBox? = null,
    noinline properties: (JsonObjectBuilder.() -> Unit)? = null,
): Feature<T> = Feature(geometry, properties?.let { buildJsonObject { properties() } }, id, bbox)
