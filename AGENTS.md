## AI policy

This project is a set of Kotlin Multiplatform libraries for working with geospatial data. Each
module is a distinct library.

## Project map

- `geojson` — GeoJSON types and DSL
- `turf` — geospatial analysis (port of Turf.js)
- `units` — units of measure
- `gpx` — GPX format support
- `pmtiles` — PMTiles v3 archive reader
- `polyline-encoding` — Google Encoded Polyline Algorithm
- `testutil` — shared test helpers
- `benchmark` — performance benchmarks
- `docs` — API documentation

## Reference resources

- GeoJSON: [RFC 7946](https://www.rfc-editor.org/rfc/rfc7946).
- Turf: [Turf.js docs](https://turfjs.org/docs/) and
  [source repository](https://github.com/Turfjs/turf).
- Units: [BIPM SI Brochure](https://www.bipm.org/en/publications/si-brochure) and
  [NIST SI units guide](https://www.nist.gov/pml/weights-and-measures/metric-si/si-units).
- GPX: [GPX 1.1 schema documentation](https://www.topografix.com/gpx/1/1/) and
  [XSD](https://www.topografix.com/GPX/1/1/gpx.xsd).
- PMTiles v3: [spec](https://github.com/protomaps/PMTiles/blob/main/spec/v3/spec.md),
  [changelog](https://github.com/protomaps/PMTiles/blob/main/spec/v3/CHANGELOG.md),
  [Protomaps docs](https://docs.protomaps.com/pmtiles/), and
  [go-pmtiles](https://github.com/protomaps/go-pmtiles).
- Polyline encoding:
  [Google Encoded Polyline Algorithm Format](https://developers.google.com/maps/documentation/utilities/polylinealgorithm).
- Kotlin interop:
  [Objective-C and Swift interop](https://kotlinlang.org/docs/native-objc-interop.html#importing-swift-objective-c-libraries-to-kotlin),
  [Swift export](https://kotlinlang.org/docs/native-swift-export.html),
  [C interop](https://kotlinlang.org/docs/native-c-interop.html),
  [JS interop](https://kotlinlang.org/docs/js-interop.html),
  [JS to Kotlin interop](https://kotlinlang.org/docs/js-to-kotlin-interop.html),
  [Java interop](https://kotlinlang.org/docs/java-interop.html), and
  [Java to Kotlin interop](https://kotlinlang.org/docs/java-to-kotlin-interop.html).

## Workflow

```bash
# Install/refresh all tools
mise install

# List available tasks across the workspace
mise tasks --all

# Compile all platforms, also run Detekt
mise run build

# Run all tests (JVM, JS, WASM, native)
mise run test

# Test specific platforms
mise run test:jvm
mise run test:jsnode
mise run test:wasmjsnode
mise run test:native

# Run formatters and linters on _all_ files (will stage affected files)
mise run fix

# Run formatters and linters on targeted files (will stage affected files)
hk fix [FILES...]

# Run a specific JVM test
mise exec -- ./gradlew :module:jvmTest --tests "*SomeTest*"
```

Formatters and linters run automatically on pre-commit; you usually don't need to run them manually.

The environment is managed by mise, so if you need to run a command that's not already a mise task,
use `mise exec -- <command>`.

## Project invariants

**After changing any public API**, run `mise run fix` to regenerate the `.api` files — the build
fails without this.

**Floating-point comparisons in tests**: use helpers from `testutil` instead of `assertEquals` to
handle platform-specific precision differences.
