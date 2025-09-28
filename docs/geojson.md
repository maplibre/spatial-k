# GeoJson

The `geojson` module contains an implementation of the
[GeoJson standard](https://tools.ietf.org/html/rfc7946).

See below for constructing GeoJson objects using the DSL.

## Installation

![Maven Central](https://img.shields.io/maven-central/v/org.maplibre.spatialk/geojson)
![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/org.maplibre.spatialk/geojson?server=https%3A%2F%2Foss.sonatype.org)

=== "Kotlin"

    ```kotlin
    dependencies {
        implementation("org.maplibre.spatialk:geojson:<version>")
    }
    ```

=== "Groovy"

    ```groovy
    dependencies {
        implementation "org.maplibre.spatialk:geojson:<version>"
    }
    ```

## GeoJson Objects

The `GeoJsonObject` interface represents all GeoJson objects. All GeoJson
objects can have a `bbox` property specified on them which is a `BoundingBox`
that represents the bounds of that object's geometry.

### Geometry

Geometry objects are a sealed hierarchy of classes that inherit from the
`Geometry` class. This allows for exhaustive type checks in Kotlin using a
`when` block.

=== "Kotlin"

    ```kotlin
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/DocSnippets.kt:geometryExhaustiveTypeChecks"
    ```

All seven types of GeoJSON geometries are implemented and summarized below. Full
documentation can be found in the [API pages](./api/geojson/index.html).

#### Position

Positions are implemented as a `DoubleArray`-backed class. Each component
(`longitude`, `latitude`, `altitude`) can be accessed by its propery. The class
also supports destructuring.

Positions are implemented as an interface where the longitude, latitude, and
optionally an altitude are accessible as properties. The basic implementation of
the `Position` interface is the `LngLat` class.

=== "Kotlin"

    ```kotlin
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/DocSnippets.kt:positionKt"
    ```

=== "JSON"

    ```json
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/DocSnippets.kt:positionJson"
    ```

#### Point

A Point is a single Position.

=== "Kotlin"

    ```kotlin
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/DocSnippets.kt:pointKt"
    ```

=== "JSON"

    ```json
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/DocSnippets.kt:pointJson"
    ```

#### MultiPoint

A `MultiPoint` is an array of Positions.

=== "Kotlin"

    ```kotlin
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/DocSnippets.kt:multiPointKt"
    ```

=== "JSON"

    ```json
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/DocSnippets.kt:multiPointJson"
    ```

#### LineString

A `LineString` is a sequence of two or more Positions.

=== "Kotlin"

    ```kotlin
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/DocSnippets.kt:lineStringKt"
    ```

=== "JSON"

    ```json
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/DocSnippets.kt:lineStringJson"
    ```

#### MultiLineString

A `MultiLineString` is an array of LineStrings.

=== "Kotlin"

    ```kotlin
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/DocSnippets.kt:multiLineStringKt"
    ```

=== "JSON"

    ```json
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/DocSnippets.kt:multiLineStringJson"
    ```

#### Polygon

A `Polygon` is an array of rings. Each ring is a sequence of points with the
last point matching the first point to indicate a closed area. The first ring
defines the outer shape of the polygon, while all the following rings define
"holes" inside the polygon.

=== "Kotlin"

    ```kotlin
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/DocSnippets.kt:polygonKt"
    ```

=== "JSON"

    ```json
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/DocSnippets.kt:polygonJson"
    ```

#### MultiPolygon

A `MultiPolygon` is an array of Polygons.

=== "Kotlin"

    ```kotlin
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/DocSnippets.kt:multiPolygonKt"
    ```

=== "JSON"

    ```json
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/DocSnippets.kt:multiPolygonJson"
    ```

#### GeometryCollection

A `GeometryCollection` is a collection of multiple geometries. It implements the
`Collection` interface and can be used in any place that a collection can be
used.

=== "Kotlin"

    ```kotlin
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/DocSnippets.kt:geometryCollectionKt"
    ```

=== "JSON"

    ```json
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/DocSnippets.kt:geometryCollectionJson"
    ```

### Feature

A `Feature` can contain a `Geometry` object, as well as a set of data
properties, and optionally a commonly used identifier (`id`).

A feature's properties are stored as a map of `JsonElement` objects from
`kotlinx.serialization`. A set of helper methods to get and set properties with
the appropriate types directly.

=== "Kotlin"

    ```kotlin
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/DocSnippets.kt:featureKt"
    ```

=== "JSON"

    ```json
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/DocSnippets.kt:featureJson"
    ```

### FeatureCollection

A `FeatureCollection` is a collection of multiple features. It implements the
`Collection` interface and can be used in any place that a collection can be
used.

=== "Kotlin"

    ```kotlin
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/DocSnippets.kt:featureCollectionKt"
    ```

=== "JSON"

    ```json
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/DocSnippets.kt:featureCollectionJson"
    ```

### BoundingBox

The `BoundingBox` class is used to represent the bounding boxes that can be set
for any `GeoJsonObject`. Like the `Position` class, bounding boxes are backed by
a `DoubleArray` with each component accessible by its propery (`southwest` and
`northeast`). Bounding boxes also support destructuring.

=== "Kotlin"

    ```kotlin
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/DocSnippets.kt:boundingBoxKt"
    ```

=== "JSON"

    ```json
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/DocSnippets.kt:boundingBoxJson"
    ```

## Serialization

### To JSON

Any `GeoJsonObject` can be serialized to a JSON string using the `json()`
function. This function converts the object to JSON using string concatenation
and is therefore very fast.

=== "Kotlin"

    ```kotlin
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/DocSnippets.kt:serializationToJson"
    ```

### From JSON

The `fromJson` and `fromJsonOrNull` companion (or static) functions are
available on each `GeoJsonObject` class to decode each type of object from a
JSON string.

=== "Kotlin"

    ```kotlin
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/DocSnippets.kt:serializationFromJson1"

    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/DocSnippets.kt:serializationFromJson2"
    ```

Like with encoding, Spatial-K objects can also be decoded using
`kotlinx.serialization` using the `GeoJson` serializer.

=== "Kotlin"

    ```kotlin
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/DocSnippets.kt:kotlinxSerialization"
    ```

## GeoJson DSL

It's recommended to construct GeoJson objects in-code using the GeoJson DSL.

### Positions

Convenience functions to construct latitude/longitude Position instances are
included. These functions will check for valid latitude and longitude values and
will throw an `IllegalArgumentException` otherwise.

=== "Kotlin"

    ```kotlin
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/DocSnippets.kt:dslLngLatKt"

    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/DocSnippets.kt:dslLngLatException"
    ```

=== "JSON"

    ```json
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/DocSnippets.kt:dslLngLatJson"
    ```

### Geometry

Each geometry type has a corresponding DSL.

A GeoJson object's `bbox` value can be assigned in any of the DSLs.

#### Point

=== "Kotlin"

    ```kotlin
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/DocSnippets.kt:dslPointKt"
    ```

=== "JSON"

    ```json
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/DocSnippets.kt:dslPointJson"
    ```

#### MultiPoint

The `MultiPoint` DSL creates a `MultiPoint` from many `Point`s, or by using the
unary plus operator to add `Position` instances as positions in the geometry.
`Point` geometries can also be added to the multipoint using the unary plus
operator.

=== "Kotlin"

    ```kotlin
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/DocSnippets.kt:dslMultiPointKt"
    ```

=== "JSON"

    ```json
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/DocSnippets.kt:dslMultiPointJson"
    ```

#### LineString

A `LineString` contains main points. Like with `MultiPoint`, a `LineString` can
also be built using the unary plus operator to add positions as part of the
line. The order in which positions are added to the `LineString` is the order
that the `LineString` will follow.

=== "Kotlin"

    ```kotlin
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/DocSnippets.kt:dslLineStringKt"
    ```

=== "JSON"

    ```json
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/DocSnippets.kt:dslLineStringJson"
    ```

#### MultiLineString

The `MultiLineString` DSL uses the unary plus operator to add multiple line
strings. The `LineString` DSL can be used to create `LineString` objects to add.

=== "Kotlin"

    ```kotlin
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/DocSnippets.kt:dslMultiLineStringKt"
    ```

=== "JSON"

    ```json
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/DocSnippets.kt:dslMultiLineStringJson"
    ```

#### Polygon

The `Polygon` DSL is used by specifying linear rings that make up the polygon's
shape and holes. The first `ring` is the exterior ring with four or more
positions. The last position must be the same as the first position. All `ring`s
that follow will represent interior rings (i.e., holes) in the polygon.

For convenience, the `complete()` function can be used to "complete" a ring. It
adds the last position in the ring by copying the first position that was added.

=== "Kotlin"

    ```kotlin
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/DocSnippets.kt:dslPolygonKt"
    ```

=== "JSON"

    ```json
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/DocSnippets.kt:dslPolygonJson"
    ```

#### MultiPolygon

Like with previous "Multi" geometries, the unary plus operator is used to add
multiple `Polygon` objects. The `Polygon` DSL can also be used here.

=== "Kotlin"

    ```kotlin
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/DocSnippets.kt:dslMultiPolygonKt"
    ```

=== "JSON"

    ```json
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/DocSnippets.kt:dslMultiPolygonJson"
    ```

#### Geometry Collection

The unary plus operator can be used to add any geometry instance to a
`GeometryCollection`.

=== "Kotlin"

    ```kotlin
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/DocSnippets.kt:dslGeometryCollectionKt"
    ```

=== "JSON"

    ```json
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/DocSnippets.kt:dslGeometryCollectionJson"
    ```

### Feature

The `Feature` DSL can construct a `Feature` object with a geometry, a bounding
box, and an id. Properties can be specified in the `PropertiesBuilder` block by
calling `put(key, value)` to add properties.

=== "Kotlin"

    ```kotlin
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/DocSnippets.kt:dslFeatureKt"
    ```

=== "JSON"

    ```json
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/DocSnippets.kt:dslFeatureJson"
    ```

### Feature Collection

A `FeatureCollection` is constructed by adding multiple `Feature` objects using
the unary plus operator.

=== "Kotlin"

    ```kotlin
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/DocSnippets.kt:dslFeatureCollectionKt"
    ```

=== "JSON"

    ```json
    --8<-- "geojson/src/commonTest/kotlin/org/maplibre/spatialk/geojson/DocSnippets.kt:dslFeatureCollectionJson"
    ```
