# Spatial K

[![Maven Central Version](https://img.shields.io/maven-central/v/org.maplibre.spatialk/geojson?label=Maven)](https://central.sonatype.com/namespace/org.maplibre.spatialk)
[![Snapshot Version](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fcentral.sonatype.com%2Frepository%2Fmaven-snapshots%2Forg%2Fmaplibre%2Fspatialk%2Fgeojson%2Fmaven-metadata.xml&label=Snapshot)](https://central.sonatype.com/repository/maven-snapshots/org/maplibre/spatialk/geojson/maven-metadata.xml)
[![License](https://img.shields.io/github/license/maplibre/spatial-k?label=License)](https://github.com/maplibre/spatial-k/blob/main/LICENSE)
[![Kotlin Version](https://img.shields.io/badge/dynamic/toml?url=https%3A%2F%2Fraw.githubusercontent.com%2Fmaplibre%2Fspatial-k%2Frefs%2Fheads%2Fmain%2Fgradle%2Flibs.versions.toml&query=versions.kotlin&prefix=v&logo=kotlin&label=Kotlin)](./gradle/libs.versions.toml)
[![Documentation](https://img.shields.io/badge/Documentation-blue?logo=MaterialForMkDocs&logoColor=white)](https://maplibre.org/spatial-k/)
[![API Reference](https://img.shields.io/badge/API_Reference-blue?logo=Kotlin&logoColor=white)](https://maplibre.org/spatial-k/api/)
[![Slack](https://img.shields.io/badge/Slack-4A154B?logo=slack&logoColor=white)](https://osmus.slack.com/archives/maplibre)

## Introduction

Spatial K is a set of libraries for working with geospatial data in Kotlin.

It includes:

- GeoJSON implementation and DSL for building GeoJSON objects
- Port of Turf.js geospatial analysis functions in pure Kotlin
- Library for working with units of measure

Spatial K supports Kotlin Multiplatform and Java projects.

## Getting Started

Add Spatial K to your project:

```kotlin
dependencies {
    implementation("org.maplibre.spatialk:geojson:VERSION")
    implementation("org.maplibre.spatialk:turf:VERSION")
    implementation("org.maplibre.spatialk:units:VERSION")
}
```

### GeoJSON

```kotlin
val sf = buildFeature {
    geometry = Point(longitude = -122.4, latitude = 37.8)
    properties = buildJsonObject {
        put("name", "San Francisco")
    }
}

val json: String = sf.toJson()
```

### Turf

```kotlin
val from = Position(longitude = -122.4, latitude = 37.8)
val to = Position(longitude = -74.0, latitude = 40.7)
val distance = distance(from, to)
val bearing = from.bearingTo(to)
```

### Units

```kotlin
val distance = 5.kilometers
val inMeters = distance.inMeters     // 5000.0
val formatted = distance.toString()  // "5000 m"
```

See the [project site](https://maplibre.org/spatial-k/) for more info.

## Getting Involved

Join the [#maplibre Slack channel](https://osmus.slack.com/archives/maplibre) at
OSMUS (get an invite at https://slack.openstreetmap.us/).

Read the [CONTRIBUTING.md](CONTRIBUTING.md) guide to get familiar with how we do
things around here.
