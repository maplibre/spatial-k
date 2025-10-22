# GPX

The `gpx` module contains data types for the GPS Exchange Format (GPX) version
1.1.

=== "Kotlin"

    ```kotlin
    --8<-- "gpx/src/commonTest/kotlin/org/maplibre/spatialk/gpx/KotlinDocsTest.kt:example"
    ```

Details can be found in the [API reference](../api/gpx/index.html).

## Installation

=== "Multiplatform"

    ```kotlin
    commonMain {
        dependencies {
            implementation("org.maplibre.spatialk:gpx:{{ gradle.project_version }}")
        }
    }
    ```

## GeoJson conversion

This module provides extension functions to convert GPX data into GeoJson. The
original Gpx type is attached as feature properties.

=== "Kotlin"

    ```kotlin
    --8<-- "gpx/src/commonTest/kotlin/org/maplibre/spatialk/gpx/KotlinDocsTest.kt:toGeoJson"
    ```
