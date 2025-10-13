<div style="text-align: center; width: 100%; height: 200px; background-color: transparent">
    <div style="width: 100%; height: 100%; background-color: #4CAE4F; mask-image: url('./images/logo-icon.svg');
                mask-size: contain; mask-repeat: no-repeat; mask-position: center"></div>
</div>

# Spatial K

Spatial K is a set of libraries for working with geospatial data in Kotlin,
including an implementation of GeoJSON and a port of Turf.js written in pure
Kotlin. It supports Kotlin Multiplatform and Java projects and features a Kotlin
DSL for building GeoJSON objects.

See the [API Reference](api/index.html) for detailed documentation.

## Modules

- [`geojson`](./geojson/index.md) - GeoJSON implementation and DSL
- [`turf`](./turf/index.md) - Turf.js port
- [`units`](./units/index.md) - Units of measure

### Snapshots

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

**Legend:**

- ✅ Available and tested in CI
- ☑️ Available but not tested in CI
- ❌ Not available

<table>
  <thead>
    <tr>
      <th>Target</th>
      <th>Platform</th>
      <th>Support</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td rowspan="2"><b>Kotlin/JVM</b></td>
      <td>JVM</td>
      <td>✅</td>
    </tr>
    <tr>
      <td>Android</td>
      <td>☑️</td>
    </tr>
    <tr>
      <td rowspan="2"><b>Kotlin/JS</b></td>
      <td>Browser</td>
      <td>☑️</td>
    </tr>
    <tr>
      <td>Node.js</td>
      <td>✅</td>
    </tr>
    <tr>
      <td rowspan="3"><b>Kotlin/WASM</b></td>
      <td>Browser, D8</td>
      <td>☑️</td>
    </tr>
    <tr>
      <td>Node.js</td>
      <td>✅</td>
    </tr>
    <tr>
      <td>Node.js (WASI)</td>
      <td>☑️</td>
    </tr>
    <tr>
      <td rowspan="7"><b>Kotlin/Native</b></td>
      <td>macOS (ARM64, x64)</td>
      <td>✅</td>
    </tr>
    <tr>
      <td>Linux (x64, ARM64)</td>
      <td>✅</td>
    </tr>
    <tr>
      <td>Windows (x64)</td>
      <td>✅</td>
    </tr>
    <tr>
      <td>iOS (all variants)</td>
      <td>☑️</td>
    </tr>
    <tr>
      <td>watchOS (all variants)</td>
      <td>☑️</td>
    </tr>
    <tr>
      <td>tvOS (all variants)</td>
      <td>☑️</td>
    </tr>
    <tr>
      <td>Android Native (all variants)</td>
      <td>☑️</td>
    </tr>
  </tbody>
</table>
