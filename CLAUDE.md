# CLAUDE.md

## Dev workflow

Tooling is managed by `mise`. Run `mise install` first (installs Java, dprint, hk, downloads ktfmt
jar, and sets up git hooks).

Key tasks:

- `mise run build` — compile and run all checks across all platforms
- `mise run test` — JVM, JS, and WASM tests
- `mise run test-native` — native tests for the current platform
- `mise run check` — lint and format checks (hk)
- `mise run fix` — auto-fix formatting
- `mise run generate` — regenerate ABI dumps (run after any public API change)

Run a single test: `./gradlew :module:jvmTest --tests "*SomeTest*"`

## Pitfalls

**After changing any public API**, run `mise run generate` to update the `.api` files — the build
fails without this.

**detekt only covers `src/commonMain`** — it won't report issues in test sources.

**Floating-point comparisons in tests**: use helpers from `testutil` instead of `assertEquals` to
handle platform-specific precision differences.

**KDoc is required on all public declarations** in `commonMain`. Missing KDoc is a detekt error.

## Modules

- `geojson` — GeoJSON types and DSL
- `turf` — geospatial analysis (port of Turf.js)
- `units` — units of measure
- `gpx` — GPX format support
- `polyline-encoding` — Google Encoded Polyline Algorithm
- `testutil` — shared test helpers
- `benchmark` — performance benchmarks

## Conventions

- All public declarations use explicit `public` modifier
- Package: `org.maplibre.spatialk.{module}`
- Prefer `data object` over plain `object` for singleton entry points (see `GeoJson`, `Gpx`)
- KDoc: prefer imports over fully qualified names in doc comments
- Convention plugins in `buildSrc/src/main/kotlin/` configure shared build behavior

## Commits

Never commit unless explicitly asked. Include in commit message:

```
Co-Authored-By: Claude <noreply@anthropic.com>
```
