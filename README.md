# Spatial K

Spatial K is a set of libraries for working with geospatial data in Kotlin including an implementation of GeoJson and 
a port of Turfjs written in pure Kotlin. It supports Kotlin Multiplatform and Java projects while also featuring a 
Kotlin DSL for building GeoJson objects.

See the [project site](https://dellisd.github.io/spatial-k) for more info.

## Installation

#### Java and Kotlin/JVM

```kotlin
dependencies {
    implementation("io.github.dellisd.spatialk:geojson:0.3.0")
    implementation("io.github.dellisd.spatialk:turf:0.3.0")
}
```

#### Kotlin Multiplatform
```kotlin
commonMain {
    dependencies {
        implementation("io.github.dellisd.spatialk:geojson:0.3.0")
        implementation("io.github.dellisd.spatialk:turf:0.3.0")
    }
}
```
