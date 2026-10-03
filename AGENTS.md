# Repository guidance

Spatial K is a set of Kotlin Multiplatform libraries for geospatial data. Each module is a distinct
library.

## Priorities

Prioritize correct behavior on supported platforms and a clear public API. Explain source and binary
compatibility changes in the PR. Keep maintainer tooling proportionate to how the project uses it.
Weigh review findings by their likelihood in real use and their cost to affected users.

## Project map

- `geojson` — types and DSL; [RFC 7946](https://www.rfc-editor.org/rfc/rfc7946).
- `geohash` — cells and OSM shortlinks;
  [shortlink implementation](https://github.com/openstreetmap/openstreetmap-website/blob/master/lib/short_link.rb).
- `turf` — geospatial analysis; [Turf.js source](https://github.com/Turfjs/turf).
- `units` — units of measure; [SI Brochure](https://www.bipm.org/en/publications/si-brochure).
- `gpx` — GPX support; [GPX 1.1 schema](https://www.topografix.com/GPX/1/1/gpx.xsd).
- `pmtiles` — archive reader;
  [PMTiles v3 spec](https://github.com/protomaps/PMTiles/blob/main/spec/v3/spec.md).
- `polyline-encoding` —
  [Google Encoded Polyline Algorithm](https://developers.google.com/maps/documentation/utilities/polylinealgorithm).
- `testutil` — shared test helpers; `benchmark` — benchmarks; `docs` — documentation site and API
  reference.

## Development

Use mise tasks: `mise tasks --all` lists them, and [CONTRIBUTING.md](CONTRIBUTING.md) covers setup
and platform requirements. For other commands, use `mise exec -- <command>`.

- Build and check: `mise run build`.
- Formatting and lint: `mise run check`; automatic fixes: `mise run fix`. Pre-commit hooks format
  staged files and run checks. `hk fix [FILES...]` targets specific files and stages changes.
- Tests: `mise run test`, or `test:jvm`, `test:jsnode`, `test:wasmjsnode`, `test:native`, and
  `test:swift` for individual platforms.
- One JVM test: `mise exec -- ./gradlew :module:jvmTest --tests "*SomeTest*"`.

Keep regression tests focused on the changed behavior, sized like neighboring tests. Scratch checks
need not be committed. Choose platforms by what the change can break: numeric precision,
serialization, and foreign APIs can differ between runtimes. Use `testutil` helpers for
floating-point comparisons. Foreign export tests primarily prove the API is usable in that language.

## API design

- Keep Kotlin ergonomic while making exported APIs usable from ObjC and Swift; see
  [Kotlin interop](https://kotlinlang.org/docs/native-objc-interop.html).
- Hide Kotlin-only helpers with `@HiddenFromObjC` or equivalent annotations.
- Use builder DSLs for option bags that may grow, rather than public constructors with defaults.
- Use inline `value class` for enum-like values that may grow without a breaking spec change.

## Documentation

Describe the current library in plain, literal language. Fix existing text when an API change
affects what readers need to do. Site pages in `docs/src/content/docs/` cover common tasks and
concepts; KDoc defines behavior, parameters, and caller limits; `CONTRIBUTING.md` covers contributor
workflows. Update the compiled test snippets used by examples. Put migration instructions in the PR
description.

## Pull requests

Follow [PULL_REQUEST_TEMPLATE.md](.github/PULL_REQUEST_TEMPLATE.md) and use Conventional Commit
titles, such as `fix(units): correct infinity checks`. Write the shortest description that lets a
reviewer who has not seen your working session understand and trust the change. Start with the
problem and resulting behavior; use a before/after example when helpful. Explain migration steps,
unresolved limitations, and decisions the diff cannot explain.

CI runs the standard checks and tests. Describe what changed tests catch and anything measured or
checked outside CI. Name affected behavior that went unverified. Use draft status for unfinished
work, unresolved decisions, or AI-assisted changes awaiting human review.
