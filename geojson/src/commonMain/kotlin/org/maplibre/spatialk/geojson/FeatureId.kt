package org.maplibre.spatialk.geojson

import kotlin.jvm.JvmInline
import kotlinx.serialization.Serializable
import org.maplibre.spatialk.geojson.serialization.FeatureIdSerializer

/**
 * Represents the identifier for a GeoJSON Feature.
 *
 * According to the GeoJSON specification, a Feature may have an identifier that can be a string or
 * number. This sealed interface provides type-safe representations of the supported identifier
 * types.
 *
 * If serializing to/from a non-Json format, only [StringFeatureId] is supported. The other types
 * will be encoded as strings.
 */
@Serializable(with = FeatureIdSerializer::class)
public sealed interface FeatureId {
    /** The underlying value of the identifier. */
    public val value: Any
}

/**
 * A string-based Feature identifier.
 *
 * @param value The string identifier value.
 */
@Serializable
@JvmInline
public value class StringFeatureId(public override val value: String) : FeatureId

/**
 * A long integer-based Feature identifier.
 *
 * @param value The long integer identifier value.
 */
@Serializable
@JvmInline
public value class LongFeatureId(public override val value: Long) : FeatureId

/**
 * A double-based Feature identifier.
 *
 * @param value The double identifier value.
 */
@Serializable
@JvmInline
public value class DoubleFeatureId(public override val value: Double) : FeatureId
