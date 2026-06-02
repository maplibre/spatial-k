## AI policy

This project is a set of Kotlin Multiplatform libraries for working with geospatial data. Each
module is a distinct library.

## Project map

- `geojson` — GeoJSON types and DSL
- `turf` — geospatial analysis (port of Turf.js)
- `units` — units of measure
- `gpx` — GPX format support
- `polyline-encoding` — Google Encoded Polyline Algorithm
- `testutil` — shared test helpers
- `benchmark` — performance benchmarks
- `docs` — API documentation

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
mise exec -- ./gradlew :module:jvmTest --tests "*SomeTest*"`
```

Formatters and linters run automatically on pre-commit; you usually don't need to run them manually.

The environment is managed by mise, so if you need to run a command that's not already a mise task,
use `mise exec -- <command>`.

## Project invariants

**After changing any public API**, run `mise run fix` to regenerate the `.api` files — the build
fails without this.

**Floating-point comparisons in tests**: use helpers from `testutil` instead of `assertEquals` to
handle platform-specific precision differences.
