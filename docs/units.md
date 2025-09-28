# Units

The `units` module contains utilities for working with units of measure, like
[Length](api/units/org.maplibre.spatialk.units/-length/index.html) and
[Area](api/units/org.maplibre.spatialk.units/-area/index.html).

Details can be found in the [API reference](./api/units/index.html).

## Installation

![Maven Central](https://img.shields.io/maven-central/v/org.maplibre.spatialk/units)
![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/org.maplibre.spatialk/units?server=https%3A%2F%2Foss.sonatype.org)

=== "Kotlin"

    ```kotlin
    dependencies {
        implementation("org.maplibre.spatialk:units:<version>")
    }
    ```

=== "Groovy"

    ```groovy
    dependencies {
        implementation "org.maplibre.spatialk:units:<version>"
    }
    ````

## Simple unit conversion

`Length` and `Area` measurements are type safe wrappers around `Double` values,
and can be converted to/from numbers with unit conversion.

=== "Kotlin"

    ```kotlin
    --8<-- "units/src/commonTest/kotlin/org/maplibre/spatialk/units/DocSnippets.kt:conversion"
    ```

## Arithmetic

Measurements support common arithmetic operations, and will convert between
scalars, lengths, and areas as needed.

=== "Kotlin"

    ```kotlin
    --8<-- "units/src/commonTest/kotlin/org/maplibre/spatialk/units/DocSnippets.kt:arithmetic"
    ```

## Custom units

We provide common international units already defined, but if you need to work
with other units, you can define your own.

=== "Kotlin"

    ```kotlin
    --8<-- "units/src/commonTest/kotlin/org/maplibre/spatialk/units/DocSnippets.kt:customUnits1"
    --8<-- "units/src/commonTest/kotlin/org/maplibre/spatialk/units/DocSnippets.kt:customUnits2"
    ```
