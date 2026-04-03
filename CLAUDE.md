# CLAUDE.md

## Dev workflow

Tooling is managed by `mise`. Run `mise install` first (installs Java, dprint, hk, downloads ktfmt
jar, and sets up git hooks).

Key tasks:

- `mise run build` — compile and run all checks across all platforms
- `mise run test` — all tests (JVM, JS, WASM, native)
- `mise run test:jvm` / `test:jsnode` / `test:wasmjsnode` / `test:native` — individual platforms
- `mise run check` — lint and format checks (hk)
- `mise run fix` — auto-fix formatting
- `mise run generate` — regenerate ABI dumps (run after any public API change)

Run a single test: `./gradlew :module:jvmTest --tests "*SomeTest*"`

## Pitfalls

**After changing any public API**, run `mise run generate` to update the `.api` files — the build
fails without this.

**Floating-point comparisons in tests**: use helpers from `testutil` instead of `assertEquals` to
handle platform-specific precision differences.

## Modules

- `geojson` — GeoJSON types and DSL
- `turf` — geospatial analysis (port of Turf.js)
- `units` — units of measure
- `gpx` — GPX format support
- `polyline-encoding` — Google Encoded Polyline Algorithm
- `testutil` — shared test helpers
- `benchmark` — performance benchmarks
