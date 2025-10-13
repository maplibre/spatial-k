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

In Kotlin, `Length`, `Area`, and `Rotation` are type-safe value classes wrapping
a `Double`. They can be converted to and from raw `Double` values using the
provided unit accessors.

In Java, use the `convert` helper from
`org.maplibre.spatialk.units.extensions.Utils` to convert between units.

=== "Kotlin"

    ```kotlin
    --8<-- "units/src/commonTest/kotlin/org/maplibre/spatialk/units/KotlinDocsTest.kt:conversion"
    ```

=== "Java"

    ```java
    --8<-- "units/src/jvmTest/java/org/maplibre/spatialk/units/JavaDocsTest.java:conversion"
    ```

## Arithmetic

In Kotlin, `Area` and `Length` support arithmetic operations through operator
overloading, converting between scalars, lengths, and areas as needed.

=== "Kotlin"

    ```kotlin
    --8<-- "units/src/commonTest/kotlin/org/maplibre/spatialk/units/KotlinDocsTest.kt:arithmetic"
    ```

## Bearings and Rotations

`Bearing` represents an absolute geographic heading (e.g., North, East), while
`Rotation` represents a relative angular displacement. In Kotlin, bearings can
be rotated using operators, and the difference between two bearings is a
rotation.

=== "Kotlin"

    ```kotlin
    --8<-- "units/src/commonTest/kotlin/org/maplibre/spatialk/units/KotlinDocsTest.kt:bearings"
    ```

## Custom units

Predefined SI (International System of Units) and Imperial units are available,
but you can define custom units as needed.

=== "Kotlin"

    ```kotlin
    --8<-- "units/src/commonTest/kotlin/org/maplibre/spatialk/units/KotlinDocsTest.kt:customUnits"
    ```

=== "Java"

    ```java
    --8<-- "units/src/jvmTest/java/org/maplibre/spatialk/units/JavaDocsTest.java:customUnits"
    ```
