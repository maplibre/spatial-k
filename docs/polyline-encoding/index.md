# Polyline Encoding

The `polyline-encoding` module encodes and decodes polylines using
[Google's Encoded Polyline Algorithm Format](https://developers.google.com/maps/documentation/utilities/polylinealgorithm).

```kotlin
--8<-- "polyline-encoding/src/commonTest/kotlin/org/maplibre/spatialk/polyline/KotlinDocsTest.kt:example"
```

Details can be found in the [API reference](../api/polyline-encoding/index.html).

## Installation

=== "Multiplatform"

    ```kotlin
    commonMain {
        dependencies {
            implementation("org.maplibre.spatialk:polyline-encoding:{{ gradle.project_version }}")
        }
    }
    ```

=== "JVM"

    ```kotlin
    dependencies {
        implementation("org.maplibre.spatialk:polyline-encoding-jvm:{{ gradle.project_version }}")
    }
    ```

## Encoding

`PolylineEncoding.encode` converts a list of `Position` values into a compact encoded string.

```kotlin
--8<-- "polyline-encoding/src/commonTest/kotlin/org/maplibre/spatialk/polyline/KotlinDocsTest.kt:encode"
```

## Decoding

`PolylineEncoding.decode` converts an encoded polyline string back into a list of `Position` values.
It throws `IllegalArgumentException` if the input is malformed.

```kotlin
--8<-- "polyline-encoding/src/commonTest/kotlin/org/maplibre/spatialk/polyline/KotlinDocsTest.kt:decode"
```

`PolylineEncoding.decodeOrNull` behaves the same way, but returns `null` on malformed input instead
of throwing.

```kotlin
--8<-- "polyline-encoding/src/commonTest/kotlin/org/maplibre/spatialk/polyline/KotlinDocsTest.kt:decodeOrNull"
```

## Precision

All three methods accept an optional `precision` parameter that controls the number of decimal
digits used for coordinate values. The default is `5`, which matches the standard Google precision
of 1e-5. Some services, such as OSRM, use a precision of `6` for 1e-6.

```kotlin
--8<-- "polyline-encoding/src/commonTest/kotlin/org/maplibre/spatialk/polyline/KotlinDocsTest.kt:precision"
```
