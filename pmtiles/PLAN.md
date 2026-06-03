# PMTiles Implementation Plan

This plan breaks the PMTiles v3 reader in `SPEC.md` into implementation checkpoints. Every
checkpoint leaves the repository compiling and keeps the public API aligned with the spec.

Operating rules:

- Public API lives in `org.maplibre.spatialk.pmtiles`.
- Run `mise run fix` after public API changes so API dumps are regenerated.
- Each checkpoint lists its required tests and checks. Run every command or test category named in a
  checkpoint before starting the next checkpoint.
- Commit at least once per phase. Each commit is atomic: it contains one coherent change, preserves
  compilation for completed implementation paths, and includes the tests or docs required to make
  that change understandable.
- Split a phase into multiple commits when the phase contains independent implementation,
  test/fixture, benchmark, documentation, or API-dump changes.
- Do not add PMTiles writing, built-in HTTP/filesystem sources, foreign JavaScript export surfaces,
  or caller codec registration in this implementation series. Foreign JavaScript export surfaces are
  `@JsExport`, generated TypeScript declarations, and JavaScript facade APIs. Kotlin/JS consumers
  use the same Kotlin API as other Kotlin Multiplatform consumers.

## Phase 1: Module Scaffold And API Skeleton

Scope:

- Add the `pmtiles` module and wire it into the workspace build.
- Add source sets needed by the spec: `commonMain`, platform gzip implementation source sets,
  JavaScript/WASM gzip placeholder source sets, and Apple source set for Foundation conveniences.
- Configure the standard repository Kotlin Multiplatform target set used by `multiplatform-module`:
  `jvm`, `js` browser and Node, `wasmJs` browser/Node/D8, `wasmWasi` Node, `macosArm64`, `macosX64`,
  `iosArm64`, `iosX64`, `iosSimulatorArm64`, `linuxX64`, `linuxArm64`, `mingwX64`,
  `watchosSimulatorArm64`, `watchosX64`, `watchosArm32`, `watchosArm64`, `watchosDeviceArm64`,
  `tvosSimulatorArm64`, `tvosX64`, `tvosArm64`, `androidNativeArm32`, `androidNativeArm64`,
  `androidNativeX86`, and `androidNativeX64`.
- Add public DTOs, enums, error types, source interfaces, and `PmTilesArchive` method signatures.
- Functions not implemented in Phase 1 throw `NotImplementedError`.

Tests and checks:

- API dump generation with `mise run fix`.
- Compile the standard repository `pmtiles` target set: `mise exec -- ./gradlew :pmtiles:assemble`

Completion criteria:

- Public package, names, and annotations match `SPEC.md`.

## Phase 2: Binary Primitives, Errors, And Limits

Scope:

- Implement little-endian readers for PMTiles fixed-width fields.
- Implement bounded unsigned varint decoding.
- Implement `ArchiveLimits`, `ArchiveLimits.Default`, `DecodeLimits`, range arithmetic helpers, and
  overflow checks.
- Implement `PmTilesException`, `PmTilesErrorCode`, and internal error construction helpers.
- Implement `ArchiveOpenOptions.Default` and `ArchiveOpenOptions.Lenient` defaults.

Tests:

- Fixed-width unsigned parsing, varint valid cases, unterminated varints, overflow varints, and
  range arithmetic overflow.
- Limit failures for byte allocation and out-of-bounds ranges.
- Default option objects exist, use `Strict`/`CompressedBytes` by default, and `Lenient` switches
  only validation mode.

Completion criteria:

- Binary helpers are internal except for public models/errors required by the spec.

## Phase 3: Tile ID Math

Scope:

- Implement `TileIds.fromZxy`, `TileIds.toZxy`, and `TileIds.zoomStart`.
- Enforce the public `z` range of `0..31`.
- Add coordinate validation for `TileCoord`-based APIs.

Tests:

- Official PMTiles TileID examples.
- Z/X/Y round trips across zooms, including randomized high-zoom cases.
- Rejection for out-of-range zoom, x/y, and TileID values.

Completion criteria:

- Tile ID functions return identical results on JVM, macOS, and iOS targets.

## Phase 4: Built-In Compression

Scope:

- Implement `None` decoding in common code.
- Implement JVM gzip decoding with `java.util.zip.GZIPInputStream` over `ByteArrayInputStream`.
- Implement Kotlin/Native gzip decoding with zlib C interop: `inflateInit2(16 + MAX_WBITS)`,
  repeated `inflate`, and `inflateEnd` on every exit path.
- Add JavaScript and WASM gzip actual implementations that compile and throw `NotImplementedError`
  when gzip decoding is invoked.
- Preserve `Unknown`, `Brotli`, and `Zstd` compression codes without decoding them.
- Enforce decode limits before allocation and during decompression.

Tests:

- `None` and gzip decode for metadata/directory/tile purposes.
- JVM gzip tests cover valid gzip, truncated gzip, invalid gzip, and decompressed-size limit
  failures from `GZIPInputStream`.
- Kotlin/Native gzip tests cover valid gzip, truncated gzip, invalid gzip, zlib stream errors, and
  decompressed-size limit failures from the zlib path.
- JavaScript and WASM gzip tests assert that invoking gzip decoding throws `NotImplementedError`.
- Unsupported unknown/brotli/zstd failures when decompression is required.
- Decompression limit failures.

Completion criteria:

- No public codec registration API is introduced.

## Phase 5: Header Parse, Open Read, And Root Validation

Scope:

- Implement the first-16-KiB open read.
- Parse every 127-byte PMTiles v3 header field into `ArchiveHeader`, including section ranges,
  counts, clustered flag, compression codes, tile type code, zooms, bounds, and center.
- Validate magic, version, section arithmetic, zoom fields, coordinate fields, counts, compression
  codes, tile type code, and root-directory location.
- Preserve unknown raw compression and tile type codes while failing when unsupported internal
  compression is required.
- Construct `PmTilesArchive` with parsed immutable state and close behavior.

Tests:

- Valid minimal headers.
- Invalid magic/version/header length.
- Header parsing for every field and raw-code value.
- Section offset/length overflow, out-of-bounds sections, legal non-canonical section order, and
  root outside first 16 KiB.
- Clustered flag, unknown counts, bounds, center, compression codes, and tile type code cases.
- Source `size()`, exact read, zero-length read, short-read failure, out-of-bounds read failure,
  preserving source-thrown `PmTilesException`, and wrapping other source exceptions.

Completion criteria:

- `PmTilesArchive.open(ByteRangeSource, ...)` can open a valid archive far enough to parse and
  validate header/root placement.

## Phase 6: Directory Decoding

Scope:

- Implement internal `DirectoryEntry` decoding from complete decompressed directory bytes.
- Decode entry count, TileID deltas, run lengths, lengths, and offset encodings.
- Validate sorted entries, nonzero lengths, offset bases, contiguous offset shorthand, and entry
  limits.
- Decode and validate the root directory during open.

Tests:

- Root directories with explicit offsets, contiguous offsets, run-length entries, and leaf entries.
- Empty directories, unsorted TileIDs, zero lengths, malformed offsets, overflowed deltas, and
  malformed varints.

Completion criteria:

- Open fails deterministically for malformed root directories in strict mode.

## Phase 7: Tile Lookup And Range APIs

Scope:

- Implement predecessor search by TileID.
- Implement leaf directory loading, decompression, validation, and traversal.
- Implement `getTileRange`, `containsTile`, and TileID/ZXY overload behavior.
- Enforce `maxDirectoryDepth` and track visited leaf ranges within a lookup.

Tests:

- Missing tile lookup.
- Direct root tile hits, run coverage, leaf hits, nested leaf traversal, leaf cache hit/miss, and
  recursive/over-depth leaf failures.
- Absolute tile and leaf range computation.

Completion criteria:

- Range-serving callers can use the library without tile payload decompression.

## Phase 8: Tile Byte APIs

Scope:

- Implement `getTileCompressed`.
- Implement `getTile` for `CompressedBytes` and `DecompressedBytes` modes.
- Apply tile compressed/decompressed limits.
- Set `wasDecompressed` and tile compression metadata correctly.

Tests:

- Compressed byte reads match exact source bytes.
- Decompressed tile reads for `None` and gzip.
- Unsupported tile compression fails only when tile decompression is requested.
- Tile range out-of-bounds and oversized tile failures.

Completion criteria:

- Tile payloads remain opaque; no MVT/image/terrain decoding is added.

## Phase 9: Metadata

Scope:

- Implement lazy `rawMetadataJson()`.
- Parse typed `ArchiveMetadata` fields with `kotlinx.serialization.json`.
- Preserve arbitrary custom keys through raw JSON.
- Enforce UTF-8, JSON object, PMTiles-defined key types, and MVT `vector_layers` behavior.

Tests:

- Valid PMTiles-defined keys and custom keys.
- Invalid UTF-8, non-object JSON, wrong typed fields, missing MVT `vector_layers`.
- Strict and lenient metadata behavior.

Completion criteria:

- Typed metadata and raw metadata are cached after first read.

## Phase 10: Lenient Mode And Warning Surface

Scope:

- Implement warning accumulation for recoverable anomalies.
- Implement `warningCount`, `warningAt(index)`, and hidden `warnings()` snapshot behavior.
- Complete strict-vs-lenient behavior for metadata, unknown counts, non-canonical section order,
  unknown tile type, unknown compression code, and nested leaf directories.

Tests:

- Warning append order, snapshot behavior, out-of-range `warningAt`, and lazy warning additions.
- Strict behavior vs lenient warnings for every warning code. Strict mode rejects metadata recovery
  cases and nested leaf directories, and preserves legal header raw-code/count/order anomalies
  without warnings.

Completion criteria:

- Lenient mode never ignores unsafe ranges, malformed directories, decompression failures, or
  overflow.

## Phase 11: Caching, Concurrency, And Close Semantics

Scope:

- Implement leaf-directory LRU cache.
- Implement metadata cache.
- Implement in-flight read de-duplication.
- Implement thread/coroutine-safe close state and idempotent `close()`.

Tests:

- Concurrent tile lookups share in-flight reads where expected.
- Archive close during in-flight reads.
- Reads after close fail with `Closed`.
- Source mutation/truncation test doubles.
- Concurrent `ByteRangeSource` reads, source mutation as `SourceChanged`, and cancellation races.

Completion criteria:

- Concurrent reads are safe across supported targets.

## Phase 12: Apple Interop

Scope:

- Add Apple `ArchiveTile.data: NSData` extension.
- Add Apple `ByteRangeDataSource`.
- Add Apple `PmTilesArchive.open(ByteRangeDataSource, ...)`.
- Hide common `open(ByteRangeSource, ...)` and `warnings()` from Objective-C/Swift with
  `@HiddenFromObjC`.
- Apply `@ObjCName`/export names required by the spec.
- Annotate every exported throwing API with `@Throws(PmTilesException::class)`.

Tests:

- Compile the standard repository `pmtiles` target set after adding the Apple declarations.
- Exact, short, and long `NSData` results from `ByteRangeDataSource`.
- Copy behavior for `NSData` conversions.
- API dump generation with `mise run fix`.
- API dumps include the public Apple declarations required by the spec.
- Public source declarations contain the `@HiddenFromObjC`, `@ObjCName`, and
  `@Throws(PmTilesException::class)` annotations required by the spec.
- Do not add SwiftPM, XCTest, `swiftc`, or generated-framework tests in this implementation series.

Completion criteria:

- Apple source sets compile, `NSData` behavior tests pass, API dumps include the public Apple
  declarations, and public source declarations include the Apple export annotations required by the
  spec.

## Phase 13: Fixture Conformance

Scope:

- Add upstream PMTiles v3 fixtures from:
  - `protomaps/PMTiles/spec/v3/`
    - <https://github.com/protomaps/PMTiles/tree/main/spec/v3>
  - `protomaps/PMTiles/js/test/data/`
    - <https://github.com/protomaps/PMTiles/tree/main/js/test/data>
  - `protomaps/go-pmtiles/pmtiles/fixtures/`
    - <https://github.com/protomaps/go-pmtiles/tree/main/pmtiles/fixtures>
- Add `pmtiles/fixtures/MANIFEST.md`. For each copied fixture, record upstream repository, commit
  SHA, path, filename, license, and reason for inclusion.
- Add generated fixtures from inputs stored under `pmtiles/src/commonTest/resources/fixtures/input/`
  using a pinned `go-pmtiles` version recorded in `pmtiles/fixtures/MANIFEST.md`. Generated fixtures
  are checked into the repository. The manifest records the exact command or helper used to create
  each generated fixture.
- Add conformance tests for vector, raster, metadata, root-only, leaf-directory, gzip, unknown
  values, and malformed archives.
- Verify compatibility with archives produced by the pinned `go-pmtiles` version recorded in
  `pmtiles/fixtures/MANIFEST.md`.

Tests:

- Run JVM tests, macOS ARM64 native tests, and iOS simulator ARM64 tests.
- JVM Kotlin interop tests for source compilation, coroutine calls, exceptions, `AutoCloseable`,
  `UInt`/`ULong`, and custom `ByteRangeSource` implementation.
- Fixture tests covering all format-conformance bullets in `SPEC.md`.

Completion criteria:

- The reader passes every fixture listed in `pmtiles/fixtures/MANIFEST.md`.

## Phase 14: Performance Benchmarks And Robustness Tests

Scope:

- Add PMTiles benchmarks to the existing `benchmark` module, which already uses
  `org.jetbrains.kotlinx.benchmark` and `kotlinx.benchmark`.
- Add benchmark methods named `openArchive`, `rootTileRangeLookup`, `leafTileRangeLookup`,
  `repeatedTileRangeLookup`, `compressedTileRead`, and `decompressedTileRead` using fixtures listed
  in `pmtiles/fixtures/MANIFEST.md` and in-memory `ByteRangeSource` implementations.
- Add `pmtiles` as a dependency of the `benchmark` module and register exactly these PMTiles
  benchmark targets: `jvm` and `macosArm64`.
- Set default limits and cache defaults to the values recorded in `SPEC-CHECKLIST.md`.
- Add tests named `compressedBombFails`, `hugeMetadataFails`, `hugeDirectoryFails`, `hugeTileFails`,
  and `cancellationRaceDoesNotReturnPartialData`.
- For every `PmTilesErrorCode`, add or update one test that asserts that code is emitted by a
  concrete failure path.

Tests:

- Security/robustness regression tests.
- Benchmark command:
  `mise exec -- ./gradlew :benchmark:benchmark -PbenchmarkWarmups=1 -PbenchmarkIterations=1`
- `mise run test`

Completion criteria:

- `SPEC-CHECKLIST.md` lists the chosen defaults for every `ArchiveLimits` and cache setting, and
  every Phase 14 robustness test passes.

## Phase 15: Documentation And Release Readiness

Scope:

- Add user-facing docs and examples for Kotlin and Swift.
- Document source ownership, close behavior, compression support, warning behavior, and metadata
  behavior.
- Document unsupported scope in user-facing docs and release notes: no PMTiles writing, no built-in
  HTTP/filesystem sources, no foreign JavaScript export surface, no JavaScript/WASM gzip
  decompression, and no custom compression registry.
- Produce `pmtiles/SPEC-CHECKLIST.md`, a markdown checklist mapping each normative claim in
  `SPEC.md` to implementation files and tests. Each row must include the spec section, claim,
  implementation reference, test reference, and status.

Tests and checks:

- `mise run fix`
- `mise run build`
- `mise run test`

Completion criteria:

- Every `SPEC-CHECKLIST.md` row has status `Implemented`, `Tested`, or `Not Applicable`. No row has
  status `Missing`, `Untested`, or `Unknown`.

## Phase 16: Code Quality Audit

Scope:

- Inspect `pmtiles/src/**`, PMTiles fixture files, PMTiles docs and examples, PMTiles API dump
  changes, `pmtiles/SPEC-CHECKLIST.md`, and PMTiles benchmark code in `benchmark/src/**`.
- Audit for correctness risk, code smells, brittle tests, unclear ownership boundaries, avoidable
  complexity, duplicated logic, weak error messages, concurrency hazards, allocation hazards,
  platform-specific drift, and spec/API mismatches.
- Produce `pmtiles/CODE-QUALITY-REPORT.md`.
- For each finding, the report records:
  - finding ID
  - area
  - affected file and line, or `Repository-wide`
  - issue
  - risk
  - resolution options, including `Do nothing`
  - tradeoffs for each resolution option
  - recommended option
  - confidence from `1` through `5`
  - status: `Completed` or `Needs Triage`
  - implementation reference for `Completed` findings
- Apply every finding with confidence `4` or `5` in the same phase.
- Leave every finding with confidence `1`, `2`, or `3` unapplied for manual triage.

Tests and checks:

- After applying confidence `4` and `5` findings, run `mise run fix`, `mise run build`, and
  `mise run test`.
- Run the Phase 14 benchmark command after applying confidence `4` and `5` findings.

Completion criteria:

- `pmtiles/CODE-QUALITY-REPORT.md` exists.
- Every confidence `4` or `5` finding has status `Completed` and includes an implementation
  reference.
- Every confidence `1`, `2`, or `3` finding has status `Needs Triage` and has no implementation
  reference.
- No code change from this phase lacks a corresponding completed report finding.
