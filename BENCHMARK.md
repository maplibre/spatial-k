# Benchmarks

Benchmarks are set up for GeoJSON serialization and deserialization.

## Running Benchmarks

```shell
./gradlew :geojson:benchmark
```

This will run benchmarks on the JVM, NodeJS, and Kotlin/Native.

These benchmarks measure the time taken to serialize and deserialize a
`FeatureCollection` containing 15,000 randomly generated features. See
[GeoJsonBenchmark.kt](geojson/src/commonBench/kotlin/org/maplibre/spatialk/geojson/GeoJsonBenchmark.kt)
for details.

## Results

All measurements are in ms/op (milliseconds per operation). Lower score is
better.

| Target              | Serialization     | Deserialization   |
| ------------------- | ----------------- | ----------------- |
| JVM                 | `32.853 ± 0.346`  | `79.946 ± 0.490`  |
| JS                  | `239.719 ± 1.217` | `240.597 ± 0.653` |
| Native (`linuxX64`) | `169.141 ± 0.608` | `279.671 ± 0.414` |

_Run on GitHub Actions Ubuntu 24.04_
