@file:OptIn(ExperimentalTypeInference::class, ExperimentalContracts::class)
@file:JvmSynthetic

// TODO: After Kotlin 2.3, add @file:MustUseReturnValues

package org.maplibre.spatialk.geojson.dsl

import kotlin.apply
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.experimental.ExperimentalTypeInference
import kotlin.jvm.JvmSynthetic
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Geometry
import org.maplibre.spatialk.geojson.GeometryCollection
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.MultiLineString
import org.maplibre.spatialk.geojson.MultiPoint
import org.maplibre.spatialk.geojson.MultiPolygon
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Polygon
import org.maplibre.spatialk.geojson.Position

@DslMarker internal annotation class GeoJsonDsl

// outer builders

/**
 * Builds a [LineString] using a DSL.
 *
 * @param builderAction The builder configuration block.
 * @return The constructed [LineString].
 * @throws IllegalArgumentException if fewer than two positions have been added.
 * @see LineStringBuilder
 */
public inline fun buildLineString(builderAction: LineStringBuilder.() -> Unit): LineString {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
    return LineStringBuilder().apply { builderAction() }.build()
}

/**
 * Builds a [Polygon] using a DSL.
 *
 * @param builderAction The builder configuration block.
 * @return The constructed [Polygon].
 * @throws IllegalArgumentException if no rings have been added or any ring contains fewer than 4
 *   [Position] objects.
 * @see PolygonBuilder
 */
public inline fun buildPolygon(builderAction: PolygonBuilder.() -> Unit): Polygon {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
    return PolygonBuilder().apply { builderAction() }.build()
}

/**
 * Builds a [MultiPoint] using a DSL.
 *
 * @param builderAction The builder configuration block.
 * @return The constructed [MultiPoint].
 * @see MultiPointBuilder
 */
public inline fun buildMultiPoint(builderAction: MultiPointBuilder.() -> Unit): MultiPoint {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
    return MultiPointBuilder().apply { builderAction() }.build()
}

/**
 * Builds a [MultiLineString] using a DSL.
 *
 * @param builderAction The builder configuration block.
 * @return The constructed [MultiLineString].
 * @see MultiLineStringBuilder
 */
public inline fun buildMultiLineString(
    builderAction: MultiLineStringBuilder.() -> Unit
): MultiLineString {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
    return MultiLineStringBuilder().apply { builderAction() }.build()
}

/**
 * Builds a [MultiPolygon] using a DSL.
 *
 * @param builderAction The builder configuration block.
 * @return The constructed [MultiPolygon].
 * @see MultiPolygonBuilder
 */
public inline fun buildMultiPolygon(builderAction: MultiPolygonBuilder.() -> Unit): MultiPolygon {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
    return MultiPolygonBuilder().apply { builderAction() }.build()
}

/**
 * Builds a [GeometryCollection] using a DSL.
 *
 * @param builderAction The builder configuration block.
 * @return The constructed [GeometryCollection].
 * @see GeometryCollectionBuilder
 */
public inline fun <G : Geometry> buildGeometryCollection(
    builderAction: GeometryCollectionBuilder<G>.() -> Unit
): GeometryCollection<G> {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
    return GeometryCollectionBuilder<G>().apply { builderAction() }.build()
}

/**
 * Builds a [Feature] using a DSL with both geometry and properties provided upfront.
 *
 * Providing values upfront allows them to have non-nullable types.
 *
 * @param G The type of [Geometry] for the feature.
 * @param P The type of properties. This can be any type that serializes to a JSON object. For
 *   dynamic or unknown property schemas, use [JsonObject]. For known schemas, use a [Serializable]
 *   data class.
 * @param geometry The geometry associated with the feature.
 * @param properties The properties associated with the feature.
 * @param builderAction The builder configuration block where [id][FeatureBuilder.id] and
 *   [bbox][FeatureBuilder.bbox] can be set.
 * @return The constructed [Feature].
 * @see FeatureBuilder
 */
public inline fun <G : Geometry?, P : @Serializable Any?> buildFeature(
    geometry: G,
    properties: P,
    builderAction: FeatureBuilder<G, P>.() -> Unit = {},
): Feature<G, P> {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
    return FeatureBuilder(geometry, properties).apply { builderAction() }.build()
}

/**
 * Builds a [Feature] using a DSL with [Geometry] provided upfront.
 *
 * Properties default to null but can be set in the builder block. Providing geometry upfront allows
 * it to have a non-nullable type.
 *
 * @param G The type of [Geometry] for the feature.
 * @param P The type of properties. This can be any type that serializes to a JSON object. For
 *   dynamic or unknown property schemas, use [JsonObject]. For known schemas, use a [Serializable]
 *   data class.
 * @param geometry The geometry associated with the feature.
 * @param builderAction The builder configuration block where
 *   [properties][FeatureBuilder.properties], [id][FeatureBuilder.id], and
 *   [bbox][FeatureBuilder.bbox] can be set.
 * @return The constructed [Feature].
 * @see FeatureBuilder
 */
public inline fun <G : Geometry?, P : @Serializable Any> buildFeature(
    geometry: G,
    builderAction: FeatureBuilder<G, P?>.() -> Unit = {},
): Feature<G, P?> {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
    return FeatureBuilder<G, P?>(geometry, null).apply { builderAction() }.build()
}

/**
 * Builds a [Feature] using a DSL.
 *
 * Both geometry and properties default to null but can be set in the builder block.
 *
 * @param G The type of [Geometry] for the feature.
 * @param P The type of properties. This can be any type that serializes to a JSON object. For
 *   dynamic or unknown property schemas, use [JsonObject]. For known schemas, use a [Serializable]
 *   data class.
 * @param builderAction The builder configuration block where [geometry][FeatureBuilder.geometry],
 *   [properties][FeatureBuilder.properties], [id][FeatureBuilder.id], and
 *   [bbox][FeatureBuilder.bbox] can be set.
 * @return The constructed [Feature].
 * @see FeatureBuilder
 */
public inline fun <G : Geometry, P : @Serializable Any> buildFeature(
    builderAction: FeatureBuilder<G?, P?>.() -> Unit = {}
): Feature<G?, P?> {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
    return FeatureBuilder<G?, P?>(null, null).apply { builderAction() }.build()
}

/**
 * Builds a [FeatureCollection] using a DSL.
 *
 * @param builderAction The builder configuration block.
 * @return The constructed [FeatureCollection].
 * @see FeatureCollectionBuilder
 */
public inline fun <G : Geometry?, P : @Serializable Any?> buildFeatureCollection(
    builderAction: FeatureCollectionBuilder<G, P>.() -> Unit
): FeatureCollection<G, P> {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
    return FeatureCollectionBuilder<G, P>().apply { builderAction() }.build()
}

// inner builders

/**
 * Adds a [Point] to a [MultiPointBuilder].
 *
 * @param coordinates The position of the point.
 * @param bbox An optional bounding box for the point.
 */
public fun MultiPointBuilder.addPoint(coordinates: Position, bbox: BoundingBox? = null) {
    add(Point(coordinates, bbox))
}

/**
 * Adds a [Point] to a [LineStringBuilder].
 *
 * @param coordinates The position of the point.
 * @param bbox An optional bounding box for the point.
 */
public fun LineStringBuilder.addPoint(coordinates: Position, bbox: BoundingBox? = null) {
    add(Point(coordinates, bbox))
}

/**
 * Adds a [Point] to a [GeometryCollectionBuilder].
 *
 * @param coordinates The position of the point.
 * @param bbox An optional bounding box for the point.
 */
public fun GeometryCollectionBuilder<in Point>.addPoint(
    coordinates: Position,
    bbox: BoundingBox? = null,
) {
    add(Point(coordinates, bbox))
}

/**
 * Adds a [LineString] to a [MultiLineStringBuilder] using a DSL.
 *
 * @param builderAction The builder configuration block for the line string.
 */
public inline fun MultiLineStringBuilder.addLineString(
    builderAction: LineStringBuilder.() -> Unit
) {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
    add(LineStringBuilder().apply { builderAction() }.build())
}

/**
 * Adds a [LineString] to a [GeometryCollectionBuilder] using a DSL.
 *
 * @param builderAction The builder configuration block for the line string.
 */
public inline fun GeometryCollectionBuilder<in LineString>.addLineString(
    builderAction: LineStringBuilder.() -> Unit
) {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
    add(LineStringBuilder().apply { builderAction() }.build())
}

/**
 * Adds a ring to a [PolygonBuilder] using a DSL.
 *
 * @param builderAction The builder configuration block for the ring.
 */
public inline fun PolygonBuilder.addRing(builderAction: LineStringBuilder.() -> Unit) {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
    add(LineStringBuilder().apply { builderAction() }.build())
}

/**
 * Adds a [Polygon] to a [MultiPolygonBuilder] using a DSL.
 *
 * @param builderAction The builder configuration block for the polygon.
 */
public inline fun MultiPolygonBuilder.addPolygon(builderAction: PolygonBuilder.() -> Unit) {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
    add(PolygonBuilder().apply { builderAction() }.build())
}

/**
 * Adds a [Polygon] to a [GeometryCollectionBuilder] using a DSL.
 *
 * @param builderAction The builder configuration block for the polygon.
 */
public inline fun GeometryCollectionBuilder<in Polygon>.addPolygon(
    builderAction: PolygonBuilder.() -> Unit
) {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
    add(PolygonBuilder().apply { builderAction() }.build())
}

/**
 * Adds a [MultiPoint] to a [GeometryCollectionBuilder] using a DSL.
 *
 * @param builderAction The builder configuration block for the multi-point.
 */
public inline fun GeometryCollectionBuilder<in MultiPoint>.addMultiPoint(
    builderAction: MultiPointBuilder.() -> Unit
) {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
    add(MultiPointBuilder().apply { builderAction() }.build())
}

/**
 * Adds a [MultiLineString] to a [GeometryCollectionBuilder] using a DSL.
 *
 * @param builderAction The builder configuration block for the multi-line string.
 */
public inline fun GeometryCollectionBuilder<in MultiLineString>.addMultiLineString(
    builderAction: MultiLineStringBuilder.() -> Unit
) {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
    add(MultiLineStringBuilder().apply { builderAction() }.build())
}

/**
 * Adds a [MultiPolygon] to a [GeometryCollectionBuilder] using a DSL.
 *
 * @param builderAction The builder configuration block for the multi-polygon.
 */
public inline fun GeometryCollectionBuilder<in MultiPolygon>.addMultiPolygon(
    builderAction: MultiPolygonBuilder.() -> Unit
) {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
    add(MultiPolygonBuilder().apply { builderAction() }.build())
}

/**
 * Adds a [GeometryCollection] to a [GeometryCollectionBuilder] using a DSL.
 *
 * @param builderAction The builder configuration block for the geometry collection.
 */
public inline fun <T : Geometry> GeometryCollectionBuilder<in GeometryCollection<T>>
    .addGeometryCollection(builderAction: GeometryCollectionBuilder<T>.() -> Unit) {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
    add(GeometryCollectionBuilder<T>().apply { builderAction() }.build())
}

/**
 * Adds a [Feature] to a [FeatureCollectionBuilder] with both geometry and properties provided
 * upfront.
 *
 * Providing values upfront allows them to have non-nullable types.
 *
 * @param geometry The geometry associated with the feature.
 * @param properties The properties associated with the feature.
 * @param builderAction The builder configuration block for the feature.
 */
public inline fun <G : Geometry?, P : @Serializable Any?> FeatureCollectionBuilder<in G, in P>
    .addFeature(geometry: G, properties: P, builderAction: FeatureBuilder<G, P>.() -> Unit = {}) {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
    add(FeatureBuilder(geometry, properties).apply { builderAction() }.build())
}

/**
 * Adds a [Feature] to a [FeatureCollectionBuilder] with geometry provided upfront.
 *
 * Properties default to null but can be set in the builder block.
 *
 * @param geometry The geometry associated with the feature.
 * @param builderAction The builder configuration block for the feature.
 */
public inline fun <G : Geometry?, P : @Serializable Any> FeatureCollectionBuilder<in G, in P?>
    .addFeature(geometry: G, builderAction: FeatureBuilder<G, P?>.() -> Unit = {}) {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
    add(FeatureBuilder<G, P?>(geometry, null).apply { builderAction() }.build())
}

/**
 * Adds a [Feature] to a [FeatureCollectionBuilder].
 *
 * Both geometry and properties default to null but can be set in the builder block.
 *
 * @param builderAction The builder configuration block for the feature.
 */
public inline fun <G : Geometry, P : @Serializable Any> FeatureCollectionBuilder<in G?, in P?>
    .addFeature(builderAction: FeatureBuilder<G?, P?>.() -> Unit = {}) {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
    add(FeatureBuilder<G?, P?>(null, null).apply { builderAction() }.build())
}

// multi geometry from singles

/**
 * Creates a [MultiPoint] from multiple [Point] objects.
 *
 * @param points The points to include in the multi-point.
 * @return A [MultiPoint] containing the specified points.
 */
public fun multiPointOf(vararg points: Point): MultiPoint = MultiPoint(*points)

/**
 * Creates a [MultiLineString] from multiple [LineString] objects.
 *
 * @param lineStrings The line strings to include in the multi-line string.
 * @return A [MultiLineString] containing the specified line strings.
 */
public fun multiLineStringOf(vararg lineStrings: LineString): MultiLineString =
    MultiLineString(*lineStrings)

/**
 * Creates a [MultiPolygon] from multiple [Polygon] objects.
 *
 * @param polygons The polygons to include in the multi-polygon.
 * @return A [MultiPolygon] containing the specified polygons.
 */
public fun multiPolygonOf(vararg polygons: Polygon): MultiPolygon = MultiPolygon(*polygons)

// all geometry from coordinates

/**
 * Creates a [MultiPoint] from multiple [Position] objects.
 *
 * @param coordinates The positions to include in the multi-point.
 * @return A [MultiPoint] containing the specified positions.
 */
public fun multiPointOf(vararg coordinates: Position): MultiPoint = MultiPoint(*coordinates)

/**
 * Creates a [LineString] from multiple [Position] objects.
 *
 * @param coordinates The positions to include in the line string.
 * @return A [LineString] containing the specified positions.
 * @throws IllegalArgumentException if fewer than two positions are provided.
 */
public fun lineStringOf(vararg coordinates: Position): LineString = LineString(*coordinates)

/**
 * Creates a [MultiLineString] from multiple lists of [Position] objects.
 *
 * @param coordinates The coordinate lists (line strings) to include in the multi-line string.
 * @return A [MultiLineString] containing the specified line strings.
 * @throws IllegalArgumentException if any list contains fewer than 2 [Position] objects.
 */
public fun multiLineStringOf(vararg coordinates: List<Position>): MultiLineString =
    MultiLineString(*coordinates)

/**
 * Creates a [Polygon] from multiple lists of [Position] objects (rings).
 *
 * Each list of positions represents a linear ring. The first ring is the exterior ring, and any
 * additional rings are interior rings (holes). See [Polygon] for details on linear ring structure.
 *
 * @param coordinates The coordinate lists (rings) to include in the [Polygon]. The first ring is
 *   the exterior, subsequent rings are holes.
 * @return A [Polygon] containing the specified rings.
 * @throws IllegalArgumentException if no coordinates are provided or any ring is not closed or
 *   contains fewer than 4 [Position] objects.
 */
public fun polygonOf(vararg coordinates: List<Position>): Polygon = Polygon(*coordinates)

/**
 * Creates a [MultiPolygon] from multiple lists of lists of [Position] objects.
 *
 * Each list of lists of positions represents one polygon, where each inner list is a linear ring.
 * Within each polygon, the first ring is the exterior ring and subsequent rings are interior rings
 * (holes). See [Polygon] for details on linear ring structure.
 *
 * @param coordinates The polygons to include in the [MultiPolygon]. Each polygon is a list of rings
 *   (lists of positions), where the first ring is the exterior and subsequent rings are holes.
 * @return A [MultiPolygon] containing the specified polygons.
 * @throws IllegalArgumentException if any polygon is empty or any ring is not closed or contains
 *   fewer than 4 [Position] objects.
 */
public fun multiPolygonOf(vararg coordinates: List<List<Position>>): MultiPolygon =
    MultiPolygon(*coordinates)

// collections

/**
 * Creates a [GeometryCollection] from multiple [Geometry] objects.
 *
 * @param geometries The geometries to include in the collection.
 * @return A [GeometryCollection] containing the specified geometries.
 */
public fun <G : Geometry> geometryCollectionOf(vararg geometries: G): GeometryCollection<G> =
    GeometryCollection(geometries.toList())

/**
 * Creates an empty [FeatureCollection].
 *
 * @return An empty [FeatureCollection].
 */
public fun featureCollectionOf(): FeatureCollection<Nothing?, Nothing?> =
    FeatureCollection(emptyList())

/**
 * Creates a [FeatureCollection] from multiple [Feature] objects.
 *
 * @param features The features to include in the collection.
 * @return A [FeatureCollection] containing the specified features.
 */
public fun <T : Geometry?, P : @Serializable Any?> featureCollectionOf(
    vararg features: Feature<T, P>
): FeatureCollection<T, P> = FeatureCollection(features.toList())
