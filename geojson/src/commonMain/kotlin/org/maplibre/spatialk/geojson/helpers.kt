@file:JvmSynthetic

package org.maplibre.spatialk.geojson

import kotlin.jvm.JvmSynthetic
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonObject

/**
 * Converts this [GeoJsonObject] to a JSON string representation.
 *
 * When called on [Feature] or [FeatureCollection] objects where the type is not fully known at
 * compile time (e.g., `(feature as GeoJsonObject).toJson()`), the serializer for custom property
 * types is selected at runtime using kotlinx.serialization's runtime serializer lookup. This has
 * important constraints:
 * - **Classes with generic type parameters will fail** (e.g., property type `Map<String, String>`)
 * - External serializers generated with `@Serializer(forClass = )` are not looked up consistently
 * - Serializers for classes with named companion objects are not looked up consistently
 * - Behavior may differ between JVM, JS, and Native platforms
 *
 * To work around these constraints when the compile-time type is not available, either:
 * - Use [JsonObject] as the property type, or
 * - Call [GeoJson.encodeToString] with explicit serializer parameters instead of this extension
 *   function
 *
 * @return A JSON string representing this GeoJSON object.
 * @throws SerializationException if serialization fails.
 * @see GeoJson.encodeToString
 */
public inline fun <reified T : GeoJsonObject> T.toJson(): String = GeoJson.encodeToString<T>(this)

/**
 * Marks GeoJSON API that should be used with caution.
 *
 * This annotation indicates API elements that have specific constraints or edge cases that require
 * careful consideration before use. Always check the documentation of the marked API element to
 * understand the potential issues and proper usage.
 */
@RequiresOptIn(
    level = RequiresOptIn.Level.ERROR,
    message = "This API should be used with caution; please check the documentation.",
)
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.CONSTRUCTOR,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
)
public annotation class SensitiveGeoJsonApi
