<div style="text-align: center; width: 100%; height: 200px; background-color: transparent">
    <div style="width: 100%; height: 100%; background-color: #4CAE4F; mask-image: url('./images/logo-icon.svg');
                mask-size: contain; mask-repeat: no-repeat; mask-position: center"></div>
</div>

# Spatial K

Spatial K is a set of libraries for working with geospatial data in Kotlin,
including an implementation of GeoJson and a port of Turfjs written in pure
Kotlin. It supports Kotlin Multiplatform projects and also features a Kotlin DSL
for building GeoJson objects.

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
