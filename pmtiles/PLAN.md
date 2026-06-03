# PMTiles Implementation Plan

This plan breaks the PMTiles v3 reader in `SPEC.md` into implementation checkpoints. Each checkpoint
should leave the repository compiling and should keep the public API aligned with the spec.

Global rules:

- Public API lives in `org.maplibre.spatialk.pmtiles`.
- Run `mise run fix` after public API changes so API dumps are regenerated.
- Each checkpoint should run the narrowest relevant tests locally; milestone checkpoints should run
  broader platform tests.
- Do not add PMTiles writing, built-in HTTP/filesystem sources, JavaScript API, or caller codec
  registration in this implementation series.

## Phase 1: Module Scaffold And API Skeleton

Scope:

- Add the `pmtiles` module and wire it into the workspace build.
- Add source sets needed by the spec: `commonMain`, platform gzip implementation source sets, and
  Apple source set for Foundation conveniences.
- Configure only the targets supported by the spec for this module. Do not add a JavaScript,
  TypeScript, or WASM public API surface.
- Add public DTOs, enums, error types, source interfaces, and `PmTilesArchive` method signatures.
- Keep implementation bodies minimal or throwing until later phases.

Tests and checks:

- API dump generation with `mise run fix`.
- Compile all configured `pmtiles` targets that can compile without real implementation.

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

- Tile ID functions are platform-independent and deterministic.

## Phase 4: Built-In Compression

Scope:

- Implement `None` decoding in common code.
- Implement JVM gzip decoding with `java.util.zip.GZIPInputStream` over `ByteArrayInputStream`.
- Implement Apple/native gzip decoding with zlib C interop: `inflateInit2(16 + MAX_WBITS)`, repeated
  `inflate`, and `inflateEnd` on every exit path.
- Preserve `Unknown`, `Brotli`, and `Zstd` compression codes without decoding them.
- Enforce decode limits before allocation and during decompression.

Tests:

- `None` and gzip decode for metadata/directory/tile purposes.
- JVM gzip tests cover valid gzip, truncated gzip, invalid gzip, and decompressed-size limit
  failures from `GZIPInputStream`.
- Apple/native gzip tests cover valid gzip, truncated gzip, invalid gzip, zlib stream errors, and
  decompressed-size limit failures from the zlib path.
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
- Strict failures vs lenient warnings for every warning code.

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

- Swift compilation for async open, metadata, tile reads, `ArchiveTile.data`, warning count/index,
  and custom `ByteRangeDataSource`.
- Exact, short, and long `NSData` results from `ByteRangeDataSource`.
- Copy behavior for `NSData` conversions.
- Swift error handling verifies thrown `PmTilesException` maps to the expected error domain/code and
  missing tiles map to `nil`.

Completion criteria:

- Swift callers do not need to implement `ByteRangeSource` or consume Kotlin collections for
  warnings.

## Phase 13: Fixture Conformance

Scope:

- Add upstream PMTiles v3 fixtures from:
  - `protomaps/PMTiles/spec/v3/`
    - <https://github.com/protomaps/PMTiles/tree/main/spec/v3>
  - `protomaps/PMTiles/js/test/data/`
    - <https://github.com/protomaps/PMTiles/tree/main/js/test/data>
  - `protomaps/go-pmtiles/pmtiles/fixtures/`
    - <https://github.com/protomaps/go-pmtiles/tree/main/pmtiles/fixtures>
- Add generated fixtures produced during tests from small in-repo tile maps using the current
  `go-pmtiles` CLI or library as the compatibility oracle. Generated fixtures must be deterministic
  and must document the exact command or helper used to create them.
- Add conformance tests for vector, raster, metadata, root-only, leaf-directory, gzip, unknown
  values, and malformed archives.
- Verify compatibility with archives produced by current Protomaps/go-pmtiles tooling.

Tests:

- JVM, Apple/native, and common test suites as appropriate.
- JVM Kotlin interop tests for source compilation, coroutine calls, exceptions, `AutoCloseable`,
  `UInt`/`ULong`, and custom `ByteRangeSource` implementation.
- Fixture tests covering all format-conformance bullets in `SPEC.md`.

Completion criteria:

- The reader passes representative real-world Protomaps archives.

## Phase 14: Performance And Robustness Polish

Scope:

- Add PMTiles benchmarks to the existing `benchmark` module, which already uses
  `org.jetbrains.kotlinx.benchmark` and `kotlinx.benchmark`.
- Add focused benchmark methods for open, root lookup, leaf lookup, repeated tile lookup, and tile
  byte reads using deterministic fixture-backed `ByteRangeSource` implementations.
- Add `pmtiles` as a dependency of the `benchmark` module and register only benchmark targets that
  are supported by the PMTiles module.
- Tune default limits and cache defaults.
- Add targeted tests for compressed bombs, huge metadata, huge directories, huge tiles, and
  cancellation races.
- Review all public errors for deterministic codes and useful messages.

Tests:

- Security/robustness regression tests.
- Benchmark smoke run through Gradle using the existing `benchmark` module with reduced warmups and
  iterations, for example `benchmarkWarmups=1` and `benchmarkIterations=1`.
- `mise run test`

Completion criteria:

- Defaults are defensible for untrusted archives and normal Protomaps-generated archives.

## Phase 15: Documentation And Release Readiness

Scope:

- Add user-facing docs and examples for Kotlin and Swift.
- Document source ownership, close behavior, compression support, warning behavior, and metadata
  behavior.
- Document unsupported scope in user-facing docs and release notes: no PMTiles writing, no built-in
  HTTP/filesystem sources, no JS API, and no custom compression registry.
- Produce `pmtiles/SPEC-CHECKLIST.md`, a markdown checklist mapping each normative claim in
  `SPEC.md` to implementation files and tests. Each row must include the spec section, claim,
  implementation reference, test reference, and status.

Tests and checks:

- `mise run fix`
- `mise run build`
- `mise run test`

Completion criteria:

- `SPEC-CHECKLIST.md` has no unchecked required claims, and public API, docs, tests, and spec agree.
