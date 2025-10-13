# Turf

[Turf.js](https://turfjs.org) is a spatial analysis library for JavaScript
applications. The `turf` module is a Kotlin port with support for Kotlin
Multiplatform and Java projects.

This module uses the classes from the [`geojson`](../geojson/index.md) module as
GeoJSON inputs to turf functions.

Documentation for ported functions can be found in the
[API docs](../api/turf/index.html), while details on each function are available
on the [Turf.js](https://turfjs.org) site.

## Installation

=== "Multiplatform"

    ```kotlin
    commonMain {
        dependencies {
            implementation("org.maplibre.spatialk:turf:{{ gradle.project_version }}")
        }
    }
    ```

=== "JVM"

    ```kotlin
    dependencies {
        implementation("org.maplibre.spatialk:turf-jvm:{{ gradle.project_version }}")
    }
    ```

## Example

Turf functions are available as top-level functions in Kotlin and as static
methods in Java.

=== "Kotlin"

    ```kotlin
    --8<-- "turf/src/commonTest/kotlin/org/maplibre/spatialk/turf/KotlinDocsTest.kt:example"
    ```

=== "Java"

    ```java
    --8<-- "turf/src/jvmTest/java/org/maplibre/spatialk/turf/JavaDocsTest.java:example"
    ```

## Turf Functions

See [the list of turf functions that have been ported](./ported-functions.md).
