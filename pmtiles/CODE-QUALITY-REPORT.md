# PMTiles Code Quality Report

Phase 16 audited `pmtiles/src/**`, PMTiles fixture metadata, PMTiles docs and examples, PMTiles API
dumps, `pmtiles/SPEC-CHECKLIST.md`, and PMTiles benchmark code in `benchmark/src/**`.

## Findings

### CQ-001

- Area: concurrency and warning surface
- Affected file and line:
  `pmtiles/src/commonMain/kotlin/org/maplibre/spatialk/pmtiles/PmTilesArchive.kt:578`
- Issue: Warning append previously used `archiveWarnings.store(archiveWarnings.load() + warning)`.
  Concurrent lenient operations could load the same snapshot and overwrite each other's appended
  warnings.
- Risk: `warningCount`, `warningAt(index)`, and `warnings()` could under-report recoverable
  anomalies under concurrent metadata parsing or nested-leaf lookup.
- Resolution options:
  - Do nothing: preserves the existing simple code, but leaves a lost-update race in the exported
    warning surface.
  - Protect warnings with `stateMutex`: avoids lost updates, but makes non-suspending warning
    accessors harder to keep simple and risks coupling warning reads to coroutine locking.
  - Use an atomic compare-and-set append loop: keeps warning access non-suspending and preserves
    append-only behavior under concurrent writers.
- Tradeoffs: The CAS loop allocates one list per failed compare under contention. Warning appends
  are rare and small, so this cost is lower than adding another lock boundary.
- Recommended option: Use an atomic compare-and-set append loop.
- Confidence: 5
- Status: Completed
- Implementation reference:
  `pmtiles/src/commonMain/kotlin/org/maplibre/spatialk/pmtiles/PmTilesArchive.kt:578`

### CQ-002

- Area: lifecycle and metadata cache
- Affected file and line:
  `pmtiles/src/commonMain/kotlin/org/maplibre/spatialk/pmtiles/PmTilesArchive.kt:469`
- Issue: `parseAndCacheMetadata` checked the close state before parsing, then parsed and cached the
  result without checking close state again. A concurrent `close()` that lost `stateMutex.tryLock()`
  could set `closed=true` while metadata parsing was in progress, after which the metadata call
  could still cache and return a value.
- Risk: An archive operation could complete successfully after `close()` had won the close-state
  transition. That weakens the close semantics specified for archive-owned caches and in-flight
  work.
- Resolution options:
  - Do nothing: keeps the code short, but leaves a lifecycle race.
  - Move metadata parsing outside `stateMutex`: reduces lock hold time, but concurrent callers can
    duplicate parsing and duplicate lenient warnings without additional coordination.
  - Keep the existing locking shape and add a second close-state check before caching and returning
    parsed metadata.
- Tradeoffs: The second close check does not reduce metadata parse lock hold time. It is a narrow
  lifecycle fix that preserves current cache and warning ordering behavior.
- Recommended option: Add a second close-state check before caching parsed metadata.
- Confidence: 4
- Status: Completed
- Implementation reference:
  `pmtiles/src/commonMain/kotlin/org/maplibre/spatialk/pmtiles/PmTilesArchive.kt:476`

### CQ-003

- Area: build verification
- Affected file and line: Repository-wide
- Issue: `mise run build` depends on JS and WASM browser test tasks from the shared
  `multiplatform-module` convention. Those tasks look for Google Chrome by default on macOS, so a
  local Chromium install must be exposed with `CHROME_BIN` unless Google Chrome is installed at the
  default path. This affects every module that uses `published-library` or `multiplatform-module`,
  not just PMTiles. CI runs `./gradlew build` on `ubuntu-latest`, where browser tooling is provided
  by the runner image. The previously observed PMTiles aggregate JVM test compile failure is tracked
  and resolved as CQ-006.
- Risk: Local release verification can appear blocked even when the code is correct if Chromium is
  installed but `CHROME_BIN` is not set.
- Resolution options:
  - Do nothing: leaves local `mise run build` dependent on the default Google Chrome app path.
  - Install Google Chrome locally: satisfies the default browser launcher path on macOS.
  - Install Chromium locally and set `CHROME_BIN`: satisfies the browser launcher without requiring
    Google Chrome.
  - Add repository-level browser provisioning or skip rules: improves repeatability, but changes
    project-wide build policy and should be handled outside this PMTiles audit.
- Tradeoffs: Browser installation and `CHROME_BIN` are local environment actions, not PMTiles code
  changes. Build-policy changes affect every module and should be made with maintainer agreement.
  The split test commands already cover JVM, JS Node, WASM JS Node, and native PMTiles paths.
- Recommended option: Install Chromium locally and set `CHROME_BIN` to
  `/Applications/Chromium.app/Contents/MacOS/Chromium` on this machine.
- Confidence: 4
- Status: Completed
- Implementation reference: Full workspace `mise run build` passed with
  `CHROME_BIN=/Applications/Chromium.app/Contents/MacOS/Chromium`.

### CQ-004

- Area: Swift documentation examples
- Affected file and line: `pmtiles/README.md:49`
- Issue: Swift usage examples describe the intended Kotlin/Native export shape, but this
  implementation series intentionally does not add SwiftPM, XCTest, `swiftc`, or generated framework
  tests.
- Risk: A future Kotlin/Native export naming change could make the Swift snippet stale while Kotlin
  and Apple-source-set tests still pass.
- Resolution options:
  - Do nothing: consistent with `SPEC.md` and `PLAN.md`, which exclude Swift toolchain tests.
  - Add a generated-framework or Swift compilation check: increases confidence in the snippet, but
    adds a new foreign toolchain dependency explicitly excluded from this implementation series.
  - Reword the docs to avoid Swift call syntax: lowers staleness risk, but makes the user-facing
    Apple section less useful.
- Tradeoffs: The current docs are useful and match the specified API shape. Compile-proofing them
  belongs in a future interop-hardening phase because it changes the project toolchain surface.
- Recommended option: Leave unchanged for this phase; add Swift compilation only in a future phase
  that explicitly expands foreign export testing.
- Confidence: 3
- Status: Needs Triage

### CQ-005

- Area: benchmark lifecycle
- Affected file and line:
  `benchmark/src/jvmAndMacosMain/kotlin/org/maplibre/spatialk/pmtiles/PmTilesBenchmark.kt:21`
- Issue: Benchmark setup opens long-lived `PmTilesArchive` instances and never closes them. The
  current archives use in-memory sources and bounded caches, so the benchmark process exit releases
  everything, but the lifecycle is less explicit than normal reader usage.
- Risk: Future benchmark changes could add source implementations or larger caches where explicit
  teardown matters.
- Resolution options:
  - Do nothing: current benchmark behavior is bounded and simple.
  - Add benchmark teardown that closes opened archives: improves lifecycle hygiene, but requires
    confirming the benchmark plugin's multiplatform teardown annotation support for both JVM and
    macOS native targets.
  - Open archives inside each benchmark method: avoids long-lived resources, but changes what the
    lookup/read benchmarks measure.
- Tradeoffs: Adding teardown is likely the right long-term shape, but using the wrong annotation or
  target-specific support would add noise to a benchmark module that already works.
- Recommended option: Add explicit teardown after confirming `kotlinx.benchmark` teardown support
  for the registered JVM and macOS targets.
- Confidence: 3
- Status: Needs Triage

### CQ-006

- Area: build verification
- Affected file and line: `pmtiles/build.gradle.kts:44`
- Issue: `:pmtiles:compileTestKotlinJvm` could compile in isolation while failing in larger
  aggregate build graphs with unresolved `kotlin.test.Test`. The base convention plugin contributes
  `kotlin("test")` to `commonTest`, and the dependency appeared in `jvmTestCompileClasspath`, but
  the PMTiles JVM test compile still dropped the test annotation package when the task ran after the
  module's JS and native test graph.
- Risk: PMTiles implementation checks could pass through `mise run test` while aggregate build
  verification remained brittle or order-sensitive.
- Resolution options:
  - Do nothing: leaves the split test path green, but preserves a reproducible aggregate build
    failure.
  - Move all test dependency wiring from the convention plugin into each module: may avoid the
    graph-sensitive behavior, but changes every module and is broader than the PMTiles scope.
  - Add an explicit PMTiles `jvmTest` dependency on `kotlin("test")`: keeps the repository
    convention intact while making the failing target's classpath unambiguous.
- Tradeoffs: The explicit dependency duplicates the convention-provided common test dependency for
  this module, but it is target-scoped and fixes the failing PMTiles aggregate path without changing
  sibling modules.
- Recommended option: Add an explicit PMTiles `jvmTest` dependency on `kotlin("test")`.
- Confidence: 4
- Status: Completed
- Implementation reference: `pmtiles/build.gradle.kts:44`
