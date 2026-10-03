# Repository guidance

This project is a set of Kotlin Multiplatform libraries for working with geospatial data. Each
module is a distinct library.

## Priorities

The published libraries are the product. Prioritize correct behavior on supported platforms and a
clear public API. Make API and binary compatibility changes explicit in the PR description.

Build tooling, benchmarks, and documentation tooling serve maintainers. Keep their complexity
proportionate to the ways the project uses them.

## Project map

- `geojson` — GeoJSON types and DSL
- `geohash` — Geohash cells and OpenStreetMap shortlinks
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
- Geohash: [Geohash format](https://en.wikipedia.org/wiki/Geohash) and OpenStreetMap
  [shortlink documentation](https://wiki.openstreetmap.org/wiki/Shortlink) and
  [Rails implementation](https://github.com/openstreetmap/openstreetmap-website/blob/master/lib/short_link.rb).
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

### Testing

- Keep regression tests that would catch the changed behavior, sized like neighboring tests. Scratch
  checks used while working need not be committed.
- Choose platform checks by what the change can break. Numeric precision, serialization, and
  foreign-language APIs can differ between runtimes.
- For floating-point comparisons in tests, use helpers from `testutil` instead of `assertEquals` to
  handle platform-specific precision differences.
- Foreign export tests don't need full coverage; they're primarily about proving the api is usable
  in that language.

### API design

- Design for ObjC export (used in Swift) without damaging the Kotlin API ergonomics.
- Avoid Kotlin idioms that create unusable foreign APIs, but do not make Kotlin awkward just to
  perfect a foreign-language call site.
- Hide Kotlin-only helpers from foreign APIs with `@HiddenFromObjC`/similar annotations.
- Option bags that may grow over time use a builder DSL rather than public constructor with
  defaults.
- Enum-like values which may grow over time without breaking spec changes should be defined as
  inline `value class`, not `enum class`.

## Documentation

Describe the library as it is, for a reader who does not know its history. Update existing text when
an API change affects what readers need to do. Migration instructions belong in the PR description.

- Site pages in `docs/src/content/docs/` explain common tasks and concepts.
- KDoc defines API behavior, parameter meaning, and limits callers rely on.
- `CONTRIBUTING.md` explains how to work on the project.

Use plain, literal language. Update the test snippets used by documentation examples so the examples
continue to compile.

## Pull requests

Follow [PULL_REQUEST_TEMPLATE.md](.github/PULL_REQUEST_TEMPLATE.md). Use Conventional Commits for PR
titles, for example `fix(units): correct infinity checks` or `docs: explain custom units`.

Write for a reviewer who has not seen your working session. Start with the problem as a user or
maintainer experiences it, then explain what behaves differently. A small before/after example can
make a bug fix clearer. Include implementation details only when they explain a decision or
tradeoff; the diff carries the rest.

Keep the description as short as it can be while letting a reviewer understand and trust the change.
Explain any changes users must make, and any part of the problem left unresolved.

CI runs the standard checks and test suites. Describe what changed tests catch and anything measured
or checked outside CI. Name affected behavior that you could not verify.

Use draft status for unfinished work, unresolved decisions, or AI-assisted changes awaiting human
review. Follow [AI_POLICY.md](AI_POLICY.md) for review and disclosure.
