# Units

The `units` module contains utilities for working with units of measure, like
[Length](../api/units/org.maplibre.spatialk.units/-length/index.html),
[Area](../api/units/org.maplibre.spatialk.units/-area/index.html),
[Rotation](../api/units/org.maplibre.spatialk.units/-rotation/index.html), and
[Bearing](../api/units/org.maplibre.spatialk.units/-bearing/index.html).

Details can be found in the [API reference](../api/units/index.html).

## Installation

=== "Multiplatform"

    ```kotlin
    commonMain {
        dependencies {
            implementation("org.maplibre.spatialk:units:{{ gradle.project_version }}")
        }
    }
    ```

=== "JVM"

    ```kotlin
    dependencies {
        implementation("org.maplibre.spatialk:units-jvm:{{ gradle.project_version }}")
    }
    ```

## Simple unit conversion

`Length`, `Area`, and `Rotation` measurements are type safe inline wrappers
around `Double` values, and can be converted to/from numbers with unit
conversion.

Java has no support for inline classes, so we provide a `convert` method to
convert a `Double` between units.

=== "Kotlin"

    ```kotlin
    --8<-- "units/src/commonTest/kotlin/org/maplibre/spatialk/units/KotlinDocsTest.kt:conversion"
    ```

=== "Java"

    ```java
    --8<-- "units/src/jvmTest/java/org/maplibre/spatialk/units/JavaDocsTest.java:conversion"
    ```

## Arithmetic

`Area` and `Length` support common arithmetic operations, and will convert
between scalars, lengths, and areas as needed.

=== "Kotlin"

    ```kotlin
    --8<-- "units/src/commonTest/kotlin/org/maplibre/spatialk/units/KotlinDocsTest.kt:arithmetic"
    ```

## Bearings and Rotations

`Bearing` represents an absolute geographic heading (e.g., North, East), while
`Rotation` represents a relative angular displacement. Bearings can be rotated,
and the difference between two bearings is a rotation.

=== "Kotlin"

    ```kotlin
    --8<-- "units/src/commonTest/kotlin/org/maplibre/spatialk/units/KotlinDocsTest.kt:bearings"
    ```

## Custom units

We provide common international units already defined, but if you need to work
with other units, you can define your own.

=== "Kotlin"

    ```kotlin
    --8<-- "units/src/commonTest/kotlin/org/maplibre/spatialk/units/KotlinDocsTest.kt:customUnits"
    ```

=== "Java"

    ```java
    --8<-- "units/src/jvmTest/java/org/maplibre/spatialk/units/JavaDocsTest.java:customUnits"
    ```
