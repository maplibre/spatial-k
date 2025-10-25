package org.maplibre.spatialk.geojson

import kotlin.jvm.JvmInline
import kotlinx.serialization.Serializable
import org.maplibre.spatialk.geojson.serialization.FeatureIdSerializer

@Serializable(with = FeatureIdSerializer::class)
public sealed interface FeatureId {
    public val value: Any
}

@Serializable
@JvmInline
public value class StringFeatureId(public override val value: String) : FeatureId

@Serializable
@JvmInline
public value class LongFeatureId(public override val value: Long) : FeatureId

@Serializable
@JvmInline
public value class DoubleFeatureId(public override val value: Double) : FeatureId
