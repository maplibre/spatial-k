package org.maplibre.spatialk.geojson.serialization

import kotlin.reflect.KClass
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonPrimitive
import org.maplibre.spatialk.geojson.GeoJsonObject

@OptIn(ExperimentalSerializationApi::class)
internal abstract class GeoJsonTypePolymorphicSerializer<T : GeoJsonObject>(baseClass: KClass<T>) :
    KSerializer<T> {

    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor =
        buildSerialDescriptor(
            "GeoJsonTypePolymorphicSerializer<${baseClass.simpleName}>",
            PolymorphicKind.SEALED,
        )

    override fun serialize(encoder: Encoder, value: T) {
        val type = value::class.simpleName!!
        val actualSerializer = selectSerializer(type)
        @Suppress("UNCHECKED_CAST") (actualSerializer as KSerializer<T>).serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): T {
        val input =
            decoder as? JsonDecoder
                ?: error("Expected Decoder to be JsonDecoder but got ${this::class}")
        val element =
            input.decodeJsonElement() as? JsonObject
                ?: throw SerializationException("Expected JSON object")
        val type =
            element["type"]?.let { Json.decodeFromJsonElement<String>(it.jsonPrimitive) }
                ?: throw SerializationException("Expected 'type' property")
        val actualSerializer = selectSerializer(type)
        @Suppress("UNCHECKED_CAST")
        return input.json.decodeFromJsonElement(actualSerializer as KSerializer<T>, element)
    }

    abstract fun selectSerializer(type: String): KSerializer<*>
}
