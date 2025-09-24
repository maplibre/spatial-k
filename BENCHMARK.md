# Benchmarks

Benchmarks are set up for GeoJSON serialization and deserialization.

## Running Benchmarks

```shell
./gradlew :geojson:benchmark
```

This will run benchmarks on the JVM, NodeJS, and Kotlin/Native.

These benchmarks measure the time taken to serialize and deserialize a `FeatureCollection` containing 15,000 randomly
generated features.
See [GeoJsonBenchmark.kt](geojson/src/commonBench/kotlin/org/maplibre/spatialk/geojson/GeoJsonBenchmark.kt) for
details.

## Results

All measurements are in ms/op (milliseconds per operation). Lower score is better.

| Target              | Serialization (fast) | Serialization (kotlinx) | Deserialization     |
| ------------------- | -------------------- | ----------------------- | ------------------- |
| JVM                 | `71.450 ± 2.566`     | `250.776 ± 7.869`       | `84.444 ± 1.968`    |
| JS                  | `178.458 ± 5.962`    | `986.504 ± 24.773`      | `383.339 ± 15.627`  |
| Native (`linuxX64`) | `326.828 ± 32.121`   | `1073.703 ± 168.403`    | `652.600 ± 117.655` |

_Run on Ubuntu 20.04 (WSL2). 32GB RAM, 3.60 GHz 8-core Intel Core i7-9700k_

### Fast vs. Kotlinx

There are two serialization implementations available in Spatial-K. The "kotlinx" implementation
uses `kotlinx.serialization` to encode the object to a string and is fully compatible with other `kotlinx.serialization`
models, but is slower. The "fast" implementation encodes an object directly
to a string using string interpolation and isn't as versatile, but is much faster.

Deserialization is done only using `kotlinx.serialization`.
