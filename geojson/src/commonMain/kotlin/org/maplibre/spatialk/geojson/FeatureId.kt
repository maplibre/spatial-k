package org.maplibre.spatialk.geojson

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive
import org.maplibre.spatialk.geojson.serialization.FeatureIdSerializer

/**
 * Represents the identifier for a GeoJSON Feature. According to the GeoJSON specification, a
 * Feature may have an identifier that can be a string or number.
 *
 * When serializing to/from a non-JSON format, the identifier will be serialized as a string,
 * including quotes if it's not a number.
 */
public typealias FeatureId = @Serializable(with = FeatureIdSerializer::class) JsonPrimitive
