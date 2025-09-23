package org.maplibre.spatialk.geojson

import org.maplibre.spatialk.geojson.serialization.FeatureSerializer
import org.maplibre.spatialk.geojson.serialization.idProp
import org.maplibre.spatialk.geojson.serialization.jsonProp
import org.maplibre.spatialk.geojson.serialization.toBbox
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.collections.set
import kotlin.jvm.JvmName
import kotlin.jvm.JvmStatic

/**
 * A feature object represents a spatially bounded thing.
 *
 * @see <a href="https://tools.ietf.org/html/rfc7946#section-3.2">https://tools.ietf.org/html/rfc7946#section-3.2</a>
 *
 * @property geometry A [Geometry] object contained within the feature.
 * @property properties Additional properties about this feature.
 * When serialized, any non-simple types will be serialized into JSON objects.
 * @property id An optionally included string that commonly identifies this feature.
 */
@Suppress("TooManyFunctions")
@Serializable(with = FeatureSerializer::class)
public class Feature(
    public val geometry: Geometry?,
    properties: Map<String, JsonElement> = emptyMap(),
    public val id: String? = null,
    override val bbox: BoundingBox? = null
) : GeoJson {
    private val _properties: MutableMap<String, JsonElement> = properties.toMutableMap()
    public val properties: Map<String, JsonElement> get() = _properties

    public fun setStringProperty(key: String, value: String?) {
        _properties[key] = JsonPrimitive(value)
    }

    public fun setNumberProperty(key: String, value: Number?) {
        _properties[key] = JsonPrimitive(value)
    }

    public fun setBooleanProperty(key: String, value: Boolean?) {
        _properties[key] = JsonPrimitive(value)
    }

    public fun setJsonProperty(key: String, value: JsonElement) {
        _properties[key] = value
    }

    public fun getStringProperty(key: String): String? = properties[key]?.jsonPrimitive?.content

    public fun getNumberProperty(key: String): Number? = properties[key]?.jsonPrimitive?.double

    public fun getBooleanProperty(key: String): Boolean? = properties[key]?.jsonPrimitive?.boolean

    public fun getJsonProperty(key: String): JsonElement? = properties[key]

    public fun removeProperty(key: String): Any? = _properties.remove(key)

    /**
     * Gets the value of the property with the given [key].
     *
     * @param key The string key for the property
     * @return The value of the property cast to [T]?, or null if the key had no value.
     */
    @JvmName("getPropertyCast")
    public inline fun <reified T : Any?> getProperty(key: String): T? = properties[key] as T?

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Feature

        if (geometry != other.geometry) return false
        if (id != other.id) return false
        if (bbox != other.bbox) return false
        if (_properties != other._properties) return false

        return true
    }

    override fun hashCode(): Int {
        var result = geometry?.hashCode() ?: 0
        result = 31 * result + (id?.hashCode() ?: 0)
        result = 31 * result + (bbox?.hashCode() ?: 0)
        result = 31 * result + _properties.hashCode()
        return result
    }

    public operator fun component1(): Geometry? = geometry
    public operator fun component2(): Map<String, JsonElement> = properties
    public operator fun component3(): String? = id
    public operator fun component4(): BoundingBox? = bbox

    override fun toString(): String = json()

    override fun json(): String =
        """{"type":"Feature",${bbox.jsonProp()}"geometry":${geometry?.json()},${idProp()}"properties":${
            Json.encodeToString(
                MapSerializer(
                    String.serializer(),
                    JsonElement.serializer()
                ), properties
            )
        }}"""

    public fun copy(
        geometry: Geometry? = this.geometry,
        properties: Map<String, JsonElement> = this.properties,
        id: String? = this.id,
        bbox: BoundingBox? = this.bbox
    ): Feature = Feature(geometry, properties, id, bbox)

    public companion object {
        @JvmStatic
        public fun fromJson(json: String): Feature = fromJson(Json.decodeFromString(JsonObject.serializer(), json))

        @JvmStatic
        public fun fromJsonOrNull(json: String): Feature? = try {
            fromJson(json)
        } catch (_: Exception) {
            null
        }

        @JvmStatic
        public fun fromJson(json: JsonObject): Feature {
            require(json.getValue("type").jsonPrimitive.content == "Feature") {
                "Object \"type\" is not \"Feature\"."
            }

            val bbox = json["bbox"]?.jsonArray?.toBbox()
            val id = json["id"]?.jsonPrimitive?.content

            val geom = json["geometry"]?.jsonObject
            val geometry: Geometry? = if (geom != null) Geometry.fromJson(geom) else null

            return Feature(geometry, json["properties"]?.jsonObject ?: emptyMap(), id, bbox)
        }
    }
}
