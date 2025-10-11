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

@GeoJsonDsl
public inline fun buildLineString(builderAction: LineStringBuilder.() -> Unit): LineString {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
    return LineStringBuilder().apply { builderAction() }.build()
}

@GeoJsonDsl
public inline fun buildPolygon(builderAction: PolygonBuilder.() -> Unit): Polygon {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
    return PolygonBuilder().apply { builderAction() }.build()
}

@GeoJsonDsl
public inline fun buildMultiPoint(builderAction: MultiPointBuilder.() -> Unit): MultiPoint {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
    return MultiPointBuilder().apply { builderAction() }.build()
}

@GeoJsonDsl
public inline fun buildMultiLineString(
    builderAction: MultiLineStringBuilder.() -> Unit
): MultiLineString {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
    return MultiLineStringBuilder().apply { builderAction() }.build()
}

@GeoJsonDsl
public inline fun buildMultiPolygon(builderAction: MultiPolygonBuilder.() -> Unit): MultiPolygon {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
    return MultiPolygonBuilder().apply { builderAction() }.build()
}

@GeoJsonDsl
public inline fun <T : Geometry> buildGeometryCollection(
    @BuilderInference builderAction: GeometryCollectionBuilder<T>.() -> Unit
): GeometryCollection<T> {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
    return GeometryCollectionBuilder<T>().apply { builderAction() }.build()
}

@GeoJsonDsl
public inline fun <T : Geometry?> buildFeature(
    geometry: T,
    @BuilderInference builderAction: FeatureBuilder<T>.() -> Unit = {},
): Feature<T> {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
    return FeatureBuilder(geometry).apply { builderAction() }.build()
}

@GeoJsonDsl
public inline fun buildFeature(
    @BuilderInference builderAction: FeatureBuilder<Nothing?>.() -> Unit = {}
): Feature<Nothing?> {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
    return FeatureBuilder(null).apply { builderAction() }.build()
}

@GeoJsonDsl
public inline fun <T : Geometry?> buildFeatureCollection(
    @BuilderInference builderAction: FeatureCollectionBuilder<T>.() -> Unit
): FeatureCollection<T> {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
    return FeatureCollectionBuilder<T>().apply { builderAction() }.build()
}

// inner builders

public fun MultiPointBuilder.addPoint(coordinates: Position, bbox: BoundingBox? = null) {
    add(Point(coordinates, bbox))
}

public fun LineStringBuilder.addPoint(coordinates: Position, bbox: BoundingBox? = null) {
    add(Point(coordinates, bbox))
}

public fun GeometryCollectionBuilder<in Point>.addPoint(
    coordinates: Position,
    bbox: BoundingBox? = null,
) {
    add(Point(coordinates, bbox))
}

@GeoJsonDsl
public inline fun MultiLineStringBuilder.addLineString(
    builderAction: LineStringBuilder.() -> Unit
) {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
    add(LineStringBuilder().apply { builderAction() }.build())
}

@GeoJsonDsl
public inline fun GeometryCollectionBuilder<in LineString>.addLineString(
    builderAction: LineStringBuilder.() -> Unit
) {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
    add(LineStringBuilder().apply { builderAction() }.build())
}

@GeoJsonDsl
public inline fun PolygonBuilder.addRing(builderAction: LineStringBuilder.() -> Unit) {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
    add(LineStringBuilder().apply { builderAction() }.build())
}

@GeoJsonDsl
public inline fun MultiPolygonBuilder.addPolygon(builderAction: PolygonBuilder.() -> Unit) {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
    add(PolygonBuilder().apply { builderAction() }.build())
}

@GeoJsonDsl
public inline fun GeometryCollectionBuilder<in Polygon>.addPolygon(
    builderAction: PolygonBuilder.() -> Unit
) {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
    add(PolygonBuilder().apply { builderAction() }.build())
}

@GeoJsonDsl
public inline fun GeometryCollectionBuilder<in MultiPoint>.addMultiPoint(
    builderAction: MultiPointBuilder.() -> Unit
) {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
    add(MultiPointBuilder().apply { builderAction() }.build())
}

@GeoJsonDsl
public inline fun GeometryCollectionBuilder<in MultiLineString>.addMultiLineString(
    builderAction: MultiLineStringBuilder.() -> Unit
) {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
    add(MultiLineStringBuilder().apply { builderAction() }.build())
}

@GeoJsonDsl
public inline fun GeometryCollectionBuilder<in MultiPolygon>.addMultiPolygon(
    builderAction: MultiPolygonBuilder.() -> Unit
) {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
    add(MultiPolygonBuilder().apply { builderAction() }.build())
}

@GeoJsonDsl
public inline fun <T : Geometry> GeometryCollectionBuilder<in GeometryCollection<T>>
    .addGeometryCollection(
    @BuilderInference builderAction: GeometryCollectionBuilder<T>.() -> Unit
) {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
    add(GeometryCollectionBuilder<T>().apply { builderAction() }.build())
}

@GeoJsonDsl
public inline fun <T : Geometry?> FeatureCollectionBuilder<in T>.addFeature(
    geometry: T,
    @BuilderInference builderAction: FeatureBuilder<T>.() -> Unit = {},
) {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
    add(FeatureBuilder(geometry).apply { builderAction() }.build())
}

@GeoJsonDsl
public inline fun FeatureCollectionBuilder<in Nothing?>.addFeature(
    @BuilderInference builderAction: FeatureBuilder<Nothing?>.() -> Unit = {}
) {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
    add(FeatureBuilder(null).apply { builderAction() }.build())
}

// multi geometry from singles

public fun multiPointOf(vararg points: Point): MultiPoint = MultiPoint(*points)

public fun multiLineStringOf(vararg lineStrings: LineString): MultiLineString =
    MultiLineString(*lineStrings)

public fun multiPolygonOf(vararg points: Point): MultiPoint = MultiPoint(*points)

// all geometry from coordinates

public fun multiPointOf(vararg coordinates: Position): MultiPoint = MultiPoint(*coordinates)

public fun lineStringOf(vararg coordinates: Position): LineString = LineString(*coordinates)

public fun multiLineStringOf(vararg coordinates: List<Position>): MultiLineString =
    MultiLineString(*coordinates)

public fun polygonOf(vararg coordinates: List<Position>): Polygon = Polygon(*coordinates)

public fun multiPolygonOf(vararg coordinates: List<List<Position>>): MultiPolygon =
    MultiPolygon(*coordinates)

// collections

public fun <T : Geometry> geometryCollectionOf(vararg geometries: T): GeometryCollection<T> =
    GeometryCollection(geometries.toList())

public fun <T : Geometry?> featureCollectionOf(vararg features: Feature<T>): FeatureCollection<T> =
    FeatureCollection(features.toList())
