package org.maplibre.spatialk.geojson.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonIgnoreUnknownKeys
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.Geometry

public object FeatureSerializer : KSerializer<Feature> {
    override val descriptor: SerialDescriptor =
        SerialDescriptor("Feature", FeatureSurrogate.serializer().descriptor)

    override fun deserialize(decoder: Decoder): Feature =
        Surrogate.deserialize(FeatureSurrogate.serializer(), decoder) { surrogate ->
            Feature(
                surrogate.geometry,
                surrogate.properties,
                surrogate.id,
                surrogate.bbox?.let { BoundingBox(it.toDoubleArray()) },
            )
        }

    override fun serialize(encoder: Encoder, value: Feature): Unit =
        Surrogate.serialize(FeatureSurrogate.serializer(), encoder) {
            FeatureSurrogate(
                geometry = value.geometry,
                properties = value.properties,
                bbox = value.bbox?.coordinates?.toList(),
                id = value.id,
            )
        }
}

@Serializable
@JsonIgnoreUnknownKeys
private data class FeatureSurrogate(
    val bbox: List<Double>? = null,
    val geometry: Geometry?,
    val id: String? = null,
    val properties: Map<String, JsonElement> = emptyMap(),
)
