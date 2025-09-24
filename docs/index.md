<div style="text-align: center;">
    <img width="200" src="images/colour.svg" alt="Spatial K Logo" title="Part of a complete breakfast!">
</div>

# Spatial K

Spatial K is a set of libraries for working with geospatial data in Kotlin, including an implementation of GeoJson and
a port of Turfjs written in pure Kotlin. It supports Kotlin Multiplatform projects and also features a
Kotlin DSL for building GeoJson objects.

## Installation

#### Java and Kotlin/JVM

```groovy
dependencies {
    implementation "org.maplibre.spatialk:geojson:<version>"
    implementation "org.maplibre.spatialk:turf:<version>"
}
```

#### Kotlin Multiplatform

```groovy
commonMain {
    dependencies {
        implementation "org.maplibre.spatialk:geojson:<version>"
        implementation "org.maplibre.spatialk:turf:<version>"
    }
}
```

### Snapshots

Snapshot builds are available on Sonatype.

```groovy
repositories {
    maven { url "https://oss.sonatype.org/content/repositories/snapshots/" }
}
```

## Supported targets

**Legend:**

- ✅ Published and tested in CI
- ⚠️ Published but not tested in CI
- ❌ Not published

| Target            | Platform                      | Support |
| ----------------- | ----------------------------- | ------- |
| **Kotlin/JVM**    | N/A                           | ✅      |
| **Kotlin/JS**     | Browser                       | ⚠️      |
| **Kotlin/JS**     | Node.js                       | ✅      |
| **Kotlin/WASM**   | Browser, D8                   | ⚠️      |
| **Kotlin/WASM**   | Node.js                       | ✅      |
| **Kotlin/WASM**   | Node.js (WASI)                | ⚠️      |
| **Kotlin/Native** | macOS (ARM64, x64)            | ✅      |
| **Kotlin/Native** | Linux (x64, ARM64)            | ✅      |
| **Kotlin/Native** | Windows x64                   | ✅      |
| **Kotlin/Native** | iOS (all variants)            | ⚠️      |
| **Kotlin/Native** | watchOS (all variants)        | ⚠️      |
| **Kotlin/Native** | tvOS (all variants)           | ⚠️      |
| **Kotlin/Native** | Android Native (all variants) | ⚠️      |
