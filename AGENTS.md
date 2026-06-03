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

## Cursor Cloud specific instructions

This is a **Kotlin Multiplatform library monorepo** — there is no runnable application. End-to-end
validation is `./gradlew build` (via `mise run build`), which compiles all targets, runs Detekt, ABI
checks, and tests on JVM, JS Node, WASM Node, and Linux native.

**Mise trust**: On a fresh VM, run `mise trust` in the repo root before `mise install`. Without
this, mise refuses to load `/workspace/mise.toml`.

**First build is slow**: The initial `./gradlew build` downloads Kotlin/Native prebuilts, Android
NDK pieces, and npm dependencies for JS/WASM tests. Expect roughly 6–10 minutes; subsequent builds
are much faster thanks to the Gradle daemon and caches.

**Docs require Python venv**: Building or serving docs (`./gradlew :mkdocsBuild`, `mise run docs`)
needs the system package `python3-venv` (Debian/Ubuntu: `apt install python3-venv`). The Gradle
python plugin creates `.gradle/python` automatically once venv is available.

**Lint/format**: Use `mise run check` (verify) or `mise run fix` (auto-fix + regenerate `.api` files
after public API changes). Standard workflow commands are listed above.

**Optional long-running process**: `mise run docs` starts an MkDocs live-reload server for
documentation editing — not required for normal library development.
