# PMTiles Writer Implementation Plan

This plan is written against the current `pmtiles` implementation, not a greenfield module. The
reader already has PMTiles v3 models, TileID conversion, parsing, decompression, limits, source
reads, and directory decoding. The writer work should reuse those pieces and only refactor the
missing write-side pieces that need production-quality implementations.

The plan intentionally starts with the required refactors, then builds writer behavior in dependency
order. Do not start archive assembly before the binary/header/directory write primitives are in
production code.

## 1. Refactors First

### 1.1 Add Write-Side Binary Primitives

`internal/BinaryReader.kt` already handles reading. Do not rewrite it. Add only the missing
write-side primitives needed by header and directory serialization.

Required changes:

- Add an internal `BinaryWriter` or small write helpers for:
  - `UInt8`
  - little-endian `ULong`
  - little-endian signed `Int`
  - unsigned varint
  - PMTiles scaled longitude/latitude positions
- Do not copy private test-helper byte-writing code into production.

Exit criteria:

- Integer, varint, and position encoding have focused tests before header or directory serialization
  is implemented.
- Production serializers have a single binary-writing primitive layer to call.

### 1.2 Add Production Header And Directory Serializers

`HeaderFixtures.kt` currently contains two writer-like helpers:

- `buildHeader(...)`
- `encodeDirectory(...)`

Those helpers are test scaffolding, not production code. Use them only as a signal that the project
already needed header and directory bytes in tests. Implement proper production serialization in
`commonMain` before implementing archive assembly.

Required changes:

- Add internal header serialization, for example `internal/HeaderWriter.kt`.
- Add internal directory serialization, either in `internal/Directory.kt` next to `decodeDirectory`
  or in a focused `internal/DirectoryEncoding.kt`.
- Reuse the production binary writer from step 1.1.
- For header/directory serialization tests, avoid tautologies: assert against independent expected
  bytes, parsed header fields, existing upstream fixtures, or external reference vectors rather than
  comparing a production encoder with a test helper that delegates to that same encoder.
- Keep fixture-specific helpers such as `buildArchive(...)`, `buildSingleTileArchive(...)`, and
  `TestByteRangeSource` in tests.

Do not recreate independent byte-writing logic inside the final archive writer.

Exit criteria:

- Production header and directory serializers exist in `commonMain`.
- Existing header parser and directory decoder tests still pass using their current fixture
  strategy.
- Directory decoder tests can still use handcrafted byte fixtures for decoder-specific coverage.
- Production directory encoder tests round-trip through `decodeDirectory` only as one assertion;
  they also check independent expected bytes for representative entries.
- Production archive assembly has a single header writer and a single directory encoder to call.

### 1.3 Keep Existing Reader Models Unless Writer Forces A Real Conflict

These already exist and should not be recreated:

- `ByteRange` and `ByteRangeSource`
- `ArchiveHeader`, `ArchiveSection`, `HeaderCounts`, `LonLatBounds`, `TileCenter`
- `CompressionCode` / `CompressionCodes`
- `TileTypeCode` / `TileTypeCodes`
- `TileCoord` and `TileIds`
- internal `DirectoryEntry` and `decodeDirectory`
- `ArchiveOpenOptions` and `ArchiveLimits`
- public `Decompressor`

No reader API refactor is required before starting writer implementation. Breaking reader changes
are allowed if a concrete writer API conflict appears, but compatibility is not itself a goal and
reader cleanup is not a prerequisite.

Writer input should use new writer-specific public shapes rather than asking callers to construct
parsed reader result types.

### 1.4 Add Write Options Instead Of Overloading Read Options

`ArchiveOpenOptions` and `ArchiveLimits` are read-side concepts. Add writer-specific options rather
than stretching read options into write behavior.

Required changes:

- Add `ArchiveWriteOptions` using the same option-bag pattern as `ArchiveOpenOptions`.
- Add writer limits where needed, for example `ArchiveWriteLimits`.
- Keep the root directory target limit on the write side. Default it to `16384 - 127`, matching the
  canonical layout constraint from `SPEC.md` and `RESEARCH.md`.
- Add a deduplication flag, defaulting to enabled.
- Add compressor registrations.

Exit criteria:

- Reader option semantics remain unchanged.
- Writer options do not expose read-only settings such as tile read coalescing.

### 1.5 Add A Writer Codec Surface Matching The Reader Surface

`Decompressor` already exists and should remain. Add a parallel writer-side `Compressor` surface
keyed by `CompressionCode`, matching the reader's one-registry model.

The reader uses one decompressor registry for root directories, leaf directories, metadata, and
decompressed tile reads. The writer should use one compressor registry for metadata, directories,
and tile payloads when the caller explicitly provides uncompressed tile input.

Required changes:

- Add public `Compressor`.
- Add writer compression limits if the compressor contract needs limits separate from
  `DecompressionLimits`.
- Add a compressor resolver parallel to the existing decompressor resolver.
- Register only `CompressionCodes.None` by default.
- Make tile compression mode explicit in the tile input API so stored tile bytes are not
  accidentally compressed again.

Do not add a `kotlinx-io` dependency or adapters. The output interface is a project-local suspend
sink.

Exit criteria:

- Unsupported compression fails before any bytes are written to the final sink.
- Existing decompressor behavior is untouched.

## 2. Public Writer API

Add the public API only after the refactors above are in place.

Required public additions:

- `ByteSink`: suspending, append-only output interface.
- Writer tile input models containing `TileCoord`, payload `ByteString`, and explicit stored vs
  uncompressed payload mode.
- Writer header/config input for tile type, compression codes, bounds, center, and raw metadata
  JSON.
- `ArchiveWriteOptions`.
- `Compressor`.
- `PmTiles` writer entry point, matching the existing namespace/factory style used by
  `PmTiles.open`.
- In-memory convenience writer returning `ByteString`, implemented through the same assembly path
  used by `ByteSink`.

API constraints:

- Final output requires append-only writes, not seek or random access.
- Writer input supports stored tile bytes and uncompressed tile bytes as explicit modes. Tile
  payload parsing and construction are out of scope for writer v1.
- Writer v1 always emits clustered archives.
- Writer v1 always computes exact header counts.
- Writer v1 validates strictly; no lenient writer mode.

Exit criteria:

- Kotlin call sites are straightforward.
- Swift/ObjC export is considered using the existing annotations and option-bag patterns.
- Public APIs do not require constructing parsed reader result models.

## 3. Header Serialization

Implement this immediately after write-side binary primitives.

Required behavior:

- Emit exactly 127 bytes.
- Write PMTiles magic and version 3.
- Write section offsets and lengths as little-endian `uint64`.
- Write counts as exact little-endian `uint64`.
- Write clustered flag, compression codes, tile type code, min zoom, max zoom, center zoom.
- Write bounds and center positions using PMTiles scaled int32 coordinates.
- Validate section offsets, section lengths, zooms, bounds, center, and root placement.

Tests:

- Exact 127-byte header length.
- Header parsed by existing `parseHeader`.
- All fields survive round-trip through `parseHeader`.
- Invalid field combinations fail before output.

Exit criteria:

- Header serialization has no dependency on final archive assembly.

## 4. Directory Encoding

Implement this before directory building.

Required behavior:

- Reject empty directory entry lists.
- Require strictly increasing TileIDs.
- Require nonzero lengths.
- Encode entry count.
- Encode TileID deltas.
- Encode run lengths.
- Encode lengths.
- Encode PMTiles offset shorthand:
  - `0` when the entry starts immediately after the previous entry's data.
  - `offset + 1` otherwise.

Tests:

- Explicit offsets.
- Contiguous shorthand.
- Run-length entries.
- Leaf directory entries.
- Strictly increasing TileID enforcement.
- Empty directory rejection.
- Zero length rejection.
- Round-trip through existing `decodeDirectory`.

Exit criteria:

- Directory encoding is independent of root/leaf partitioning.

## 5. Metadata Serialization

Implement metadata before final archive assembly because metadata length participates in section
offsets.

Required behavior:

- Accept raw metadata JSON string.
- Parse it with `kotlinx.serialization.json`.
- Reject non-object JSON.
- For MVT tile type, require `vector_layers`.
- Encode as UTF-8.
- Compress with internal compression.
- Enforce metadata byte limits.

Tests:

- Object JSON accepted.
- Non-object JSON rejected.
- Invalid JSON rejected.
- MVT metadata without `vector_layers` rejected.
- Metadata round-trips through the existing reader APIs.

Exit criteria:

- Metadata bytes and compressed length are known before header construction.

## 6. Compression Writer Pipeline

Implement compression before metadata, directory assembly, and tile entry assembly need final stored
byte lengths.

Required behavior:

- Resolve compressors from writer options.
- Support `CompressionCodes.None` by default.
- Fail unsupported compression before final sink writes begin.
- Preserve cancellation from compressor calls.
- Apply internal compression to metadata and directories.
- For stored tile input, do not transform tile payload bytes.
- For uncompressed tile input, compress tile payload bytes using the archive tile compression code.
- Do not infer stored vs uncompressed mode from the compression code.

Tests:

- None compressor returns input.
- Registered custom compressor is used for internal sections.
- Registered custom compressor is used for uncompressed tile input.
- Unsupported internal compression fails.
- Unsupported tile compression fails for uncompressed tile input.
- Compressor limit failures are reported as `PmTilesException`.

Exit criteria:

- Later phases can request compressed bytes without knowing registry details.

## 7. FNV-1a 128-Bit Hashing

Implement the dedup hash before tile assembly.

Required behavior:

- Implement internal multiplatform FNV-1a 128-bit.
- Match go-pmtiles policy from `RESEARCH.md`.
- Hash stored input tile bytes for stored input.
- Hash uncompressed input tile bytes before compression for uncompressed input.
- Use hashes as dedup keys.
- Do not keep prior full payloads solely to verify hash collisions.

Tests:

- Known deterministic vectors, or vectors generated once from the Go reference implementation.
- Empty input.
- Multi-byte payload.
- Same result across supported test targets.

Exit criteria:

- Tile assembly can deduplicate without depending on platform hash implementations.

## 8. Tile Entry Assembly

Implement this after compression and hashing.

Required behavior:

- Convert `TileCoord` to TileID using existing `TileIds`.
- Sort input tiles by TileID.
- Reject duplicate TileIDs.
- If deduplication is enabled:
  - hash logical input payload bytes according to the input mode;
  - compress only the first occurrence for a hash when the input mode is uncompressed;
  - store only the first occurrence for a hash;
  - point later duplicate tiles to the first stored tile-data offset.
- If deduplication is disabled:
  - store every input payload independently;
  - emit `runLength = 1`.
- Coalesce consecutive TileIDs that point to the same stored offset into one directory entry with
  `runLength > 1`.
- Compute exact counts:
  - addressed tile count;
  - tile entry count;
  - tile content count.
- Compute tile data bytes and section-relative tile offsets.
- Compute min zoom and max zoom from inputs.

Tests:

- Unsorted input becomes sorted output.
- Duplicate TileID rejected.
- Dedup enabled stores one content for repeated payloads.
- Dedup disabled stores repeated payloads separately.
- Consecutive duplicate payloads become a run-length entry.
- Non-consecutive duplicate payloads share an offset but remain separate entries.
- Counts match PMTiles definitions.

Exit criteria:

- The output of this phase is a complete list of tile `DirectoryEntry` values plus staged tile-data
  bytes.

## 9. Directory Building

Implement root/leaf construction after tile entries are assembled.

Required behavior:

- Encode all tile entries as a direct root first.
- If `127 + compressedRootLength <= 16384` and within configured root limit, emit no leaves.
- If direct root does not fit, partition tile entries into leaf directories.
- Root entries for leaves point into the leaf directory section.
- Leaf directory offsets are relative to the leaf directory section.
- Tile offsets are relative to the tile data section.
- Leaf directories are ordered by starting TileID.
- Iteratively adjust partitioning until the compressed root fits the configured root target.
- Fail if configured limits make a valid root impossible.
- Never emit nested leaf directories.

Tests:

- Direct-root archive.
- Forced leaf archive using a small test root limit.
- Leaf directory offsets and lengths resolve through existing reader lookup.
- Root-limit failure.
- No nested leaf output.

Exit criteria:

- This phase returns compressed root bytes, compressed leaf bytes, and final section lengths.

## 10. Final Archive Assembly

Implement final output only after header, metadata, tile assembly, and directory building are tested
independently.

Required behavior:

- Use canonical section ordering:
  - header at `0`
  - root directory at `127`
  - metadata after root
  - leaf directories after metadata
  - tile data after leaf directories
- Compute final section offsets and lengths before writing byte 0.
- Serialize the header with exact counts.
- Write append-only to `ByteSink`:
  - header;
  - root directory;
  - metadata;
  - leaf directories;
  - tile data.
- Flush and close according to the `ByteSink` contract.
- Surface sink failures as `PmTilesException` while preserving cancellation.

Tests:

- In-memory sink receives bytes in canonical order.
- No final-output random access is used.
- Sink write failure stops the writer.
- Cancellation is not wrapped as a normal PMTiles error.
- In-memory convenience writer uses the same path.

Exit criteria:

- A produced archive can be opened by the existing reader in strict mode.

## 11. Round-Trip And Fixture Tests

Use existing checked-in fixtures as source material. Do not add new writer-generated fixture files
for v1; generated archives should be produced in test memory or temporary test output.

Fixtures to reuse:

- `go-pmtiles-unclustered.pmtiles`: small PNG source archive.
- `pmtiles-js-test-fixture-1.pmtiles`: one-tile gzip MVT payload source.
- `protomaps-vector-odbl-firenze.pmtiles`: realistic vector metadata with `vector_layers`.
- `stamen-toner-raster-cc-by-odbl-z3.pmtiles`: larger raster source suitable for leaf directory
  tests.

Required tests:

- Read source tiles from `go-pmtiles-unclustered.pmtiles`, write a new clustered archive, reopen it,
  and compare tile payloads, tile presence, type, compression, counts, bounds, and center.
- Read payload and metadata from `pmtiles-js-test-fixture-1.pmtiles`, write an MVT archive, and
  verify gzip-marked payload behavior on targets where gzip decompression is registered.
- Read metadata from `protomaps-vector-odbl-firenze.pmtiles` and verify `vector_layers` validation
  and preservation.
- Use tiles from `stamen-toner-raster-cc-by-odbl-z3.pmtiles` with a small root limit to force leaf
  directory generation, then verify lookups through the existing reader.
- Generate in-test duplicate payload cases for dedup and run-length behavior.
- Generate in-test negative archives or inputs for duplicate TileID, invalid bounds, invalid center,
  invalid metadata, unsupported internal compression, root partition failure, and offset/length
  overflow.

Exit criteria:

- Writer tests reuse fixture provenance already documented in
  `pmtiles/src/commonTest/resources/README.md`.
- No new long-lived binary fixture is added without updating that README.

## 12. Swift Shim And Interop Updates

Do this after the Kotlin public API names are stable and before final verification.

Required changes:

- Update `pmtiles/src/swiftMain/SpatialKPmtiles.swift`.
- Add Swift-facing wrappers/adapters for writer APIs that are awkward through raw Kotlin export:
  - `ByteSink`;
  - `Compressor`;
  - writer options;
  - stored tile input;
  - uncompressed tile input;
  - writer entry points.
- Keep Swift wrappers thin. They should adapt Swift `Data`, async calls, and option construction
  without reimplementing PMTiles writer logic.
- Preserve Swift error behavior for thrown Kotlin and Swift-side adapter failures.
- Keep Kotlin APIs ergonomic; use `@ShouldRefineInSwift` and `@HiddenFromObjC` where the raw export
  should be hidden behind a Swift wrapper.

Required tests:

- Update `pmtiles/src/swiftTest/SwiftDocsTest.swift`.
- Add Swift docs-test snippets that prove the API is usable from Swift:
  - writing an archive to a Swift sink;
  - writing in memory, or documenting why the in-memory convenience is Kotlin-only;
  - stored tile input;
  - uncompressed tile input with a Swift-provided compressor;
  - writer option construction;
  - sink failure propagation;
  - compressor failure propagation.
- Ensure Swift can reopen writer-produced archives through the existing Swift reader APIs.
- Do not use Swift tests for full writer behavior coverage. Behavior conformance belongs in
  common/JVM/JS/WASM/native writer tests.

Exit criteria:

- Swift shims compile.
- Swift docs tests provide useful snippets for package docs.
- Swift docs tests prove the writer API is usable from Swift.
- Swift tests do not duplicate core writer conformance covered in common tests.

## 13. API Documentation And Examples

Add docs only after the API names are stable.

Required docs:

- `ByteSink` contract, including append-only behavior and cancellation.
- Writer entry point usage.
- Raw metadata JSON requirements.
- Compression registration.
- Deduplication default and disable option.
- Statement that writer v1 emits clustered canonical-layout archives.
- Statement that tile payload encoding is caller responsibility.

Exit criteria:

- Public API docs explain behavior without requiring readers to know PMTiles internals.

## 14. Verification Order

Run tests in this order so failures stay local:

1. Unit tests for binary writer, header writer, directory encoder, metadata validation, FNV hashing,
   compression resolution, tile assembly, and directory building.
2. JVM PMTiles tests, for quick iteration: `mise exec -- ./gradlew :pmtiles:jvmTest`
3. JS, WASM, and native PMTiles tests as supported by the workspace tasks.
4. Swift shim/docs tests for `pmtiles/src/swiftMain/SpatialKPmtiles.swift` and
   `pmtiles/src/swiftTest/SwiftDocsTest.swift`.
5. Full workspace test when the module is stable: `mise run test`
6. Build and static checks: `mise run build`

If a platform lacks a registered compressor such as gzip, gate only the compression-specific
assertion for that platform. Do not skip archive structure tests.

## 15. Final Audit Against SPEC.md

Before calling the implementation complete, audit every requirement in `SPEC.md`.

Required report:

- Public API additions implemented.
- Non-goals respected.
- Writer always emits canonical layout.
- Writer always emits clustered archives.
- Writer computes exact counts.
- Dedup default matches go/python policy from `RESEARCH.md`.
- Run-length entries emitted for consecutive duplicate offsets.
- Root-to-leaf only; no nested leaves.
- No required `kotlinx-io` dependency.
- No tile payload parser or builder added.
- Swift shim and Swift docs tests updated.
- Fixtures and tests match the fixture policy in this plan and `SPEC.md`.

## 16. Final Audit Against The PMTiles V3 Spec

Audit the final output against the upstream PMTiles v3 spec cited in `RESEARCH.md`.

Required checks:

- Header size, magic, version, field offsets, and field encodings.
- Root directory location inside the first 16 KiB.
- Section offsets and lengths.
- Compression code handling.
- Tile type code handling.
- Directory entry encoding.
- Leaf directory semantics.
- Tile data offsets relative to the tile data section.
- Metadata JSON object requirement.
- Bounds and center position encoding.
- Clustered flag semantics.

Record any intentionally unimplemented spec surface. The expected exclusions are only:

- built-in brotli/zstd compressors unless caller supplied;
- tile payload parsing/construction.

## 17. Code Quality, Architecture, And Organization Review

End with a code review pass, not just passing tests.

Review checklist:

- Writer code reuses existing TileID, compression-code, tile-type, and reader validation patterns.
- Binary serialization is centralized.
- Package and file organization is coherent:
  - public API files contain public concepts only;
  - internal serialization, directory building, tile assembly, hashing, and final archive assembly
    are separated by responsibility;
  - platform-specific compression code stays in platform source sets;
  - Swift shim code stays in `pmtiles/src/swiftMain/SpatialKPmtiles.swift`.
- Module architecture is cohesive:
  - reader and writer share low-level models where appropriate;
  - writer-specific input/config models do not leak parsed reader result types;
  - no shared abstraction exists only to hide a single call site;
  - no phase depends on final archive assembly unless it must.
- Fixture-building helpers call production internals only where serialization is not the behavior
  under test.
- Header and directory serializer tests include independent expected-byte coverage and are not
  tautological.
- Public API names are small and coherent.
- Swift/ObjC annotations follow existing option-bag patterns.
- Swift shims are thin adapters and do not reimplement writer logic.
- Error messages name the invalid input clearly.
- Cancellation is preserved.
- No final-output random access assumption exists.
- No unrelated reader refactor was introduced.
- No unchecked integer narrowing can overflow archive offsets or lengths.
- Generated archives are validated by reopening with the existing reader.

The final implementation report should list:

- completed phases;
- tests run;
- remaining intentionally unsupported behavior;
- any code quality risks found and fixed during the review.
