# Spatial K

Spatial K is a set of libraries for working with geospatial data in Kotlin,
including an implementation of GeoJson and a port of Turf.js written in pure
Kotlin. It supports Kotlin Multiplatform and Java projects, while also featuring
a Kotlin DSL for building GeoJson objects.

See the [project site](https://maplibre.github.io/spatial-k) for more info.

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

## Contribution

### Getting Involved

Join the #maplibre slack channel at OSMUS: get an invite at
https://slack.openstreetmap.us/

Read the [CONTRIBUTING.md](CONTRIBUTING.md) guide in order to get familiar with
how we do things around here.
