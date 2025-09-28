# Turf

[Turfjs](https://turfjs.org) is a spatial analysis library for JavaScript
applications and the `turf` module contains a Kotlin port of it with support for
Kotlin Multiplatform projects.

This module makes use of the classes defined in the [`geojson`](./geojson.md)
module as the GeoJson inputs to many of the turf functions.

The documentation for the ported functions can be found in the
[API docs](./api/turf/index.html), while more details on each function can be
found on the [Turfjs](https://turfjs.org) site.

## Installation

![Maven Central](https://img.shields.io/maven-central/v/org.maplibre.spatialk/turf)
![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/org.maplibre.spatialk/turf?server=https%3A%2F%2Foss.sonatype.org)

=== "Kotlin"

    ```kotlin
    dependencies {
        implementation("org.maplibre.spatialk:turf:<version>")
    }
    ```

=== "Groovy"

    ```groovy
    dependencies {
        implementation "org.maplibre.spatialk:turf:<version>"
    }
    ````

## Example

Turf functions are available as top-level functions in Kotlin, or as static
member functions in Java.

=== "Kotlin"

    ```kotlin
    --8<-- "turf/src/commonTest/kotlin/org/maplibre/spatialk/turf/DocSnippets.kt:example"
    ```

## Turf Functions

A list of all turf functions and their current status in the port can be found
on [this page](./ported-functions.md).
