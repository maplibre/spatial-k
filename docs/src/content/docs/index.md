---
title: "Overview"
---

<div style="text-align: center; width: 100%; height: 200px; background-color: transparent">
    <div style="width: 100%; height: 100%; background-color: #4CAE4F; mask-image: url('/spatial-k/logo-icon.svg');
                mask-size: contain; mask-repeat: no-repeat; mask-position: center"></div>
</div>

Spatial K is a set of libraries for working with geospatial data in Kotlin, including an
implementation of GeoJSON and a port of Turf.js written in pure Kotlin. It supports Kotlin
Multiplatform and Java projects and features a Kotlin DSL for building GeoJSON objects.

See the [API Reference](/spatial-k/api/dokka/) for detailed documentation.

## Snapshots

![Sonatype Snapshots](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fcentral.sonatype.com%2Frepository%2Fmaven-snapshots%2Forg%2Fmaplibre%2Fspatialk%2Fgeojson%2Fmaven-metadata.xml&label=Snapshot)

Snapshot builds are available on Sonatype:

```kotlin
repositories {
    maven {
        url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
    }
}

dependencies {
    implementation("org.maplibre.spatialk:geojson:VERSION-SNAPSHOT")
}
```

Note: Snapshots are unstable and may change without notice.

## Supported targets

All modules have broad Kotlin Multiplatform support, including JVM, JS, WASM, and Native. Native
targets include [tier 1-3](https://kotlinlang.org/docs/native-target-support.html), except platforms
deprecated by JetBrains.
