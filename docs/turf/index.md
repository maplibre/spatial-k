# Turf

[Turf.js](https://turfjs.org) is a spatial analysis library for JavaScript
applications and the `turf` module contains a Kotlin port of it with support for
Kotlin Multiplatform projects.

This module makes use of the classes defined in the
[`geojson`](../geojson/index.md) module as the GeoJson inputs to many of the
turf functions.

The documentation for the ported functions can be found in the
[API docs](../api/turf/index.html), while more details on each function can be
found on the [Turfjs](https://turfjs.org) site.

## Installation

#### Kotlin Multiplatform

```kotlin
commonMain {
    dependencies {
        implementation("org.maplibre.spatialk:turf:{{ gradle.project_version }}")
    }
}
```

#### Java and Kotlin/JVM

```kotlin
dependencies {
    implementation("org.maplibre.spatialk:turf-jvm:{{ gradle.project_version }}")
}
```

## Example

Turf functions are available as top-level functions in Kotlin, or as static
member functions in Java.

=== "Kotlin"

    ```kotlin
    --8<-- "turf/src/commonTest/kotlin/org/maplibre/spatialk/turf/DocSnippets.kt:example"
    ```

## Turf Functions

See [the list of turf functions that have been ported](./ported-functions.md).
