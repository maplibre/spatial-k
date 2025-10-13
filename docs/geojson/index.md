# GeoJSON

The `geojson` module contains an implementation of
[RFC 7946: The GeoJSON Format](https://tools.ietf.org/html/rfc7946).

See below for constructing GeoJSON objects using the DSL.

## Installation

=== "Multiplatform"

    ```kotlin
    commonMain {
        dependencies {
            implementation("org.maplibre.spatialk:geojson:{{ gradle.project_version }}")
        }
    }
    ```

=== "JVM"

    ```kotlin
    dependencies {
        implementation("org.maplibre.spatialk:geojson-jvm:{{ gradle.project_version }}")
    }
    ```

## GeoJSON Objects

The `GeoJsonObject` interface represents all GeoJSON objects. All GeoJSON
objects can have a `bbox` property specified on them which is a `BoundingBox`
that represents the bounds of that object's geometry.

### Geometry

Geometry objects are a sealed hierarchy of classes that inherit from the
`Geometry` class. This allows for exhaustive type checks in Kotlin using a
`when` block.

=== "Kotlin"

    ```kotlin
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/KotlinDocsTest.kt:geometryExhaustiveTypeChecks"
    ```

All seven types of GeoJSON geometries are implemented and summarized below. Full
documentation can be found in the [API pages](../api/geojson/index.html).

#### Position

`Position` is a `DoubleArray`-backed class where longitude, latitude, and
optionally an altitude are accessible as properties. Coordinates follow the
order specified in RFC 7946: `[longitude, latitude, altitude?]`. The class
supports destructuring in Kotlin.

=== "Kotlin"

    ```kotlin
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/KotlinDocsTest.kt:positionKt"
    ```

=== "Java"

    ```java
    --8<-- "geojson/src/jvmTest/java/org/maplibre/spatialk/geojson/JavaDocsTest.java:positionJava"
    ```

=== "JSON"

    ```json
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/KotlinDocsTest.kt:positionJson"
    ```

#### Point

A Point is a single Position.

=== "Kotlin"

    ```kotlin
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/KotlinDocsTest.kt:pointKt"
    ```

=== "Java"

    ```java
    --8<-- "geojson/src/jvmTest/java/org/maplibre/spatialk/geojson/JavaDocsTest.java:pointJava"
    ```

=== "JSON"

    ```json
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/KotlinDocsTest.kt:pointJson"
    ```

#### MultiPoint

A `MultiPoint` is an array of Positions.

=== "Kotlin"

    ```kotlin
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/KotlinDocsTest.kt:multiPointKt"
    ```

=== "Java"

    ```java
    --8<-- "geojson/src/jvmTest/java/org/maplibre/spatialk/geojson/JavaDocsTest.java:multiPointJava"
    ```

=== "JSON"

    ```json
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/KotlinDocsTest.kt:multiPointJson"
    ```

#### LineString

A `LineString` is a sequence of two or more Positions.

=== "Kotlin"

    ```kotlin
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/KotlinDocsTest.kt:lineStringKt"
    ```

=== "Java"

    ```java
    --8<-- "geojson/src/jvmTest/java/org/maplibre/spatialk/geojson/JavaDocsTest.java:lineStringJava"
    ```

=== "JSON"

    ```json
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/KotlinDocsTest.kt:lineStringJson"
    ```

#### MultiLineString

A `MultiLineString` is an array of LineStrings.

=== "Kotlin"

    ```kotlin
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/KotlinDocsTest.kt:multiLineStringKt"
    ```

=== "Java"

    ```java
    --8<-- "geojson/src/jvmTest/java/org/maplibre/spatialk/geojson/JavaDocsTest.java:multiLineStringJava"
    ```

=== "JSON"

    ```json
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/KotlinDocsTest.kt:multiLineStringJson"
    ```

#### Polygon

A `Polygon` is an array of rings. Each ring is a sequence of points with the
last point matching the first point to indicate a closed area. The first ring
defines the outer shape of the polygon, while all the following rings define
"holes" inside the polygon.

=== "Kotlin"

    ```kotlin
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/KotlinDocsTest.kt:polygonKt"
    ```

=== "Java"

    ```java
    --8<-- "geojson/src/jvmTest/java/org/maplibre/spatialk/geojson/JavaDocsTest.java:polygonJava"
    ```

=== "JSON"

    ```json
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/KotlinDocsTest.kt:polygonJson"
    ```

#### MultiPolygon

A `MultiPolygon` is an array of Polygons.

=== "Kotlin"

    ```kotlin
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/KotlinDocsTest.kt:multiPolygonKt"
    ```

=== "Java"

    ```java
    --8<-- "geojson/src/jvmTest/java/org/maplibre/spatialk/geojson/JavaDocsTest.java:multiPolygonJava"
    ```

=== "JSON"

    ```json
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/KotlinDocsTest.kt:multiPolygonJson"
    ```

#### GeometryCollection

A `GeometryCollection` contains multiple, heterogeneous geometries.

=== "Kotlin"

    ```kotlin
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/KotlinDocsTest.kt:geometryCollectionKt"
    ```

=== "Java"

    ```java
    --8<-- "geojson/src/jvmTest/java/org/maplibre/spatialk/geojson/JavaDocsTest.java:geometryCollectionJava"
    ```

=== "JSON"

    ```json
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/KotlinDocsTest.kt:geometryCollectionJson"
    ```

### Feature

A `Feature` can contain a `Geometry` object, as well as a set of data
properties, and optionally a commonly used identifier (`id`).

Properties can be any object that serializes into a JSON object. For dynamic or
unknown property schemas, use `JsonObject`. For known schemas, use a
`@Serializable` data class. Helper methods for accessing properties are
available when properties are of type `JsonObject` (see the
[API documentation](../api/geojson/index.html) for details).

=== "Kotlin"

    ```kotlin
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/KotlinDocsTest.kt:featureKt"
    ```

=== "Java"

    ```java
    --8<-- "geojson/src/jvmTest/java/org/maplibre/spatialk/geojson/JavaDocsTest.java:featureJava"
    ```

=== "JSON"

    ```json
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/KotlinDocsTest.kt:featureJson"
    ```

### FeatureCollection

A `FeatureCollection` is a collection of multiple features. It implements the
`Collection` interface and can be used in any place that a collection can be
used.

=== "Kotlin"

    ```kotlin
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/KotlinDocsTest.kt:featureCollectionKt"
    ```

=== "Java"

    ```java
    --8<-- "geojson/src/jvmTest/java/org/maplibre/spatialk/geojson/JavaDocsTest.java:featureCollectionJava"
    ```

=== "JSON"

    ```json
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/KotlinDocsTest.kt:featureCollectionJson"
    ```

### BoundingBox

The `BoundingBox` class is used to represent the bounding boxes that can be set
for any `GeoJsonObject`. Like the `Position` class, bounding boxes are backed by
a `DoubleArray` with each component accessible by its propery (`southwest` and
`northeast`). Bounding boxes also support destructuring.

=== "Kotlin"

    ```kotlin
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/KotlinDocsTest.kt:boundingBoxKt"
    ```

=== "Java"

    ```java
    --8<-- "geojson/src/jvmTest/java/org/maplibre/spatialk/geojson/JavaDocsTest.java:boundingBoxJava"
    ```

=== "JSON"

    ```json
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/KotlinDocsTest.kt:boundingBoxJson"
    ```

## Serialization

### To JSON

Any `GeoJsonObject` can be serialized to a JSON string using the `toJson()`
method.

=== "Kotlin"

    ```kotlin
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/KotlinDocsTest.kt:serializationToJson"
    ```

=== "Java"

    ```java
    --8<-- "geojson/src/jvmTest/java/org/maplibre/spatialk/geojson/JavaDocsTest.java:serializationToJsonJava"
    ```

### From JSON

The `fromJson` and `fromJsonOrNull` companion (or static) functions are
available on each `GeoJsonObject` class to decode each type of object from a
JSON string.

=== "Kotlin"

    ```kotlin
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/KotlinDocsTest.kt:serializationFromJson1"

    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/KotlinDocsTest.kt:serializationFromJson2"
    ```

=== "Java"

    ```java
    --8<-- "geojson/src/jvmTest/java/org/maplibre/spatialk/geojson/JavaDocsTest.java:serializationFromJsonJava1"

    --8<-- "geojson/src/jvmTest/java/org/maplibre/spatialk/geojson/JavaDocsTest.java:serializationFromJsonJava2"
    ```

Like with encoding, Spatial-K objects can also be decoded using
`kotlinx.serialization` using the `GeoJson` serializer.

=== "Kotlin"

    ```kotlin
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/KotlinDocsTest.kt:kotlinxSerialization"
    ```

## GeoJSON Builders

It's recommended to construct GeoJSON objects in-code using builder classes. In
Kotlin, these are available through a convenient DSL. In Java, use the builder
classes directly.

### Geometry

Each geometry type more complex than `Point` has a corresponding DSL.

A GeoJSON object's `bbox` value can be assigned in any of the DSLs.

#### MultiPoint

The `MultiPoint` builder uses `add()` to add positions or `Point` geometries.

=== "Kotlin"

    ```kotlin
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/KotlinDocsTest.kt:dslMultiPointKt"
    ```

=== "Java"

    ```java
    --8<-- "geojson/src/jvmTest/java/org/maplibre/spatialk/geojson/JavaDocsTest.java:dslMultiPointJava"
    ```

=== "JSON"

    ```json
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/KotlinDocsTest.kt:dslMultiPointJson"
    ```

#### LineString

A `LineString` contains two or more positions, in order. The builder uses
`add()` to add positions. The order in which positions are added is the order
that the line will follow.

=== "Kotlin"

    ```kotlin
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/KotlinDocsTest.kt:dslLineStringKt"
    ```

=== "Java"

    ```java
    --8<-- "geojson/src/jvmTest/java/org/maplibre/spatialk/geojson/JavaDocsTest.java:dslLineStringJava"
    ```

=== "JSON"

    ```json
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/KotlinDocsTest.kt:dslLineStringJson"
    ```

#### MultiLineString

The `MultiLineString` builder uses `addLineString()` to define line strings
inline, or `add()` to add existing `LineString` objects.

=== "Kotlin"

    ```kotlin
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/KotlinDocsTest.kt:dslMultiLineStringKt"
    ```

=== "Java"

    ```java
    --8<-- "geojson/src/jvmTest/java/org/maplibre/spatialk/geojson/JavaDocsTest.java:dslMultiLineStringJava"
    ```

=== "JSON"

    ```json
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/KotlinDocsTest.kt:dslMultiLineStringJson"
    ```

#### Polygon

The `Polygon` builder uses `addRing()` (Kotlin DSL) or `add()` with `LineString`
objects (Java/Kotlin) to define linear rings. The first ring is the exterior
ring with four or more positions. The last position must be the same as the
first position. All subsequent rings represent interior rings (i.e., holes) in
the polygon.

=== "Kotlin"

    ```kotlin
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/KotlinDocsTest.kt:dslPolygonKt"
    ```

=== "Java"

    ```java
    --8<-- "geojson/src/jvmTest/java/org/maplibre/spatialk/geojson/JavaDocsTest.java:dslPolygonJava"
    ```

=== "JSON"

    ```json
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/KotlinDocsTest.kt:dslPolygonJson"
    ```

#### MultiPolygon

The `MultiPolygon` builder uses `addPolygon()` to define polygons inline, or
`add()` to add existing `Polygon` objects.

=== "Kotlin"

    ```kotlin
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/KotlinDocsTest.kt:dslMultiPolygonKt"
    ```

=== "Java"

    ```java
    --8<-- "geojson/src/jvmTest/java/org/maplibre/spatialk/geojson/JavaDocsTest.java:dslMultiPolygonJava"
    ```

=== "JSON"

    ```json
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/KotlinDocsTest.kt:dslMultiPolygonJson"
    ```

#### GeometryCollection

The `GeometryCollection` builder provides `addPoint()`, `addLineString()`,
`addPolygon()`, `addMultiPoint()`, `addMultiLineString()`, `addMultiPolygon()`,
and `addGeometryCollection()` to define geometries inline (Kotlin only), or
`add()` to add existing geometry objects.

=== "Kotlin"

    ```kotlin
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/KotlinDocsTest.kt:dslGeometryCollectionKt"
    ```

=== "Java"

    ```java
    --8<-- "geojson/src/jvmTest/java/org/maplibre/spatialk/geojson/JavaDocsTest.java:dslGeometryCollectionJava"
    ```

=== "JSON"

    ```json
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/KotlinDocsTest.kt:dslGeometryCollectionJson"
    ```

### Feature

The `Feature` builder constructs a `Feature` object with a geometry, bounding
box, id, and properties. Properties can be any serializable object, such as a
`JsonObject` built with `buildJsonObject` from kotlinx.serialization.

=== "Kotlin"

    ```kotlin
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/KotlinDocsTest.kt:dslFeatureKt"
    ```

=== "Java"

    ```java
    --8<-- "geojson/src/jvmTest/java/org/maplibre/spatialk/geojson/JavaDocsTest.java:dslFeatureJava"
    ```

=== "JSON"

    ```json
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/KotlinDocsTest.kt:dslFeatureJson"
    ```

### FeatureCollection

The `FeatureCollection` builder uses `addFeature()` to define features inline
(Kotlin only), or `add()` to add existing `Feature` objects.

=== "Kotlin"

    ```kotlin
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/KotlinDocsTest.kt:dslFeatureCollectionKt"
    ```

=== "Java"

    ```java
    --8<-- "geojson/src/jvmTest/java/org/maplibre/spatialk/geojson/JavaDocsTest.java:dslFeatureCollectionJava"
    ```

=== "JSON"

    ```json
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/KotlinDocsTest.kt:dslFeatureCollectionJson"
    ```
