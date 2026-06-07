# PMTiles Writer Support Specification

This document specifies what must be added or changed to support PMTiles v3 writing in the `pmtiles`
module. Background facts, citations, and pitfalls are in `RESEARCH.md`.

## Goals

- Add PMTiles v3 archive writing support to the `pmtiles` module.
- Keep the API ergonomic for Kotlin and usable from Swift/ObjC.
- Design a clean combined reader/writer API, allowing breaking reader API changes when they
  materially improve the public surface.
- Reuse existing reader validation, TileID logic, compression code models, and option-bag patterns.
- Emit archives that the existing `PmTiles.open` reader can parse in strict mode.
- Support canonical archive layout: `header`, `root directory`, `metadata`, `leaf directories`,
  `tile data`.

## Non-Goals

- Do not implement tile payload format encoders such as MVT, PNG, JPEG, WebP, AVIF, or MLT.
- Do not implement MBTiles conversion in this module.
- Do not require filesystem or HTTP sinks in common code.
- Do not implement brotli or zstd compression unless a compressor is supplied by callers.
- Do not emit non-canonical section ordering. Writer v1 always emits `header`, `root directory`,
  `metadata`, `leaf directories`, `tile data`.
- Do not emit unclustered archives. Writer v1 always sorts tile inputs by TileID and sets the
  clustered flag.
- Do not emit unknown header counts. Writer v1 always computes exact addressed tile, tile entry, and
  tile content counts.
- Do not add nested leaf directory creation. PMTiles v3 directory traversal is canonical as
  root-directory entries pointing either directly to tile data or to one leaf directory. Nested
  leaves are a malformed/non-canonical case the current reader accepts only in lenient mode for
  robustness; a writer should never emit them.
- Do not make `kotlinx-io` a required writer dependency or expose its synchronous `Sink`/`RawSink`
  types as the core output interface.

## Public API Additions

### Writer Entry Point

Add writing entry points under the existing `PmTiles` namespace, matching the current factory
pattern used by `PmTiles.open`.

Required capabilities:

- Write a complete archive to a caller-provided sink.
- Provide a convenience in-memory writer returning `ByteString` for small archives and tests.
- Accept writer options through an option bag with the same style as `ArchiveOpenOptions`.

The API should avoid exposing parsed reader result models as writer input. `ArchiveHeader`,
`ArchiveSection`, `HeaderCounts`, `LonLatBounds`, and `TileCenter` have internal constructors and
represent parsed archive state, not writer configuration.

### Byte Sink

Add a common suspending byte sink abstraction analogous to `ByteRangeSource`, but append-only rather
than range-addressed.

Required behavior:

- Append exact bytes in write order through a suspending API.
- Flush pending output through a suspending API.
- Close output through a suspending API.
- Surface failures as `PmTilesException` and preserve cancellation.
- Avoid a dependency on synchronous `kotlinx-io` `Sink`/`RawSink` types.

The API should support large archives without requiring a complete archive `ByteString` in memory.
An in-memory helper can be provided separately.

A representative shape is:

```kotlin
public interface ByteSink {
    @Throws(PmTilesException::class, CancellationException::class)
    public suspend fun write(bytes: ByteString)

    @Throws(PmTilesException::class, CancellationException::class)
    public suspend fun flush()

    @Throws(PmTilesException::class, CancellationException::class)
    public suspend fun close()
}
```

The final PMTiles output writer should only require append semantics. It should not require seeking
or random access to the destination.

### Tile Input Model

Add public writer input models for tile payloads.

Required fields:

- `TileCoord`.
- Tile payload bytes as `ByteString`.
- Whether the payload bytes are already stored/compressed bytes or uncompressed bytes to be
  compressed by the writer.

Tile coordinate validation should reuse `TileCoord` and `TileIds` behavior described in
`RESEARCH.md`.

### Metadata Input

Support raw metadata JSON string input.

Required behavior:

- Store metadata as UTF-8 JSON object bytes.
- Parse the raw JSON string to verify that it is a JSON object.
- Preserve `vector_layers` requirements for MVT archives.

Do not add a typed metadata builder in v1. PMTiles metadata allows additional arbitrary keys, so raw
JSON is the required public path.

### Header Configuration

Add writer configuration for:

- Tile type code.
- Tile compression code.
- Internal compression code.
- Bounds.
- Center.
- Min zoom and max zoom derived from tile inputs.
- Clustered output. Writer v1 always emits clustered archives.

The API should use inline `CompressionCode` and `TileTypeCode`, not enums, matching existing
extensible-code patterns.

### Writer Options

Add `ArchiveWriteOptions` or equivalent with:

- Private primary constructor.
- Public default constructor.
- Nested mutable `Builder`.
- `toBuilder()`.
- Kotlin DSL companion `build { ... }`.
- `@ShouldRefineInSwift` and `@HiddenFromObjC` usage consistent with `ArchiveOpenOptions`.

Options should include:

- Internal compression.
- Tile compression.
- Root directory byte limit, defaulting to `16384 - 127` for canonical layout.
- Limits for metadata bytes, directory bytes, tile bytes, and total archive size.
- Compressor registrations.
- Deduplication enabled flag, defaulting to enabled.

Writer validation is strict. The writer should not have a lenient mode.

See `RESEARCH.md` for current option-bag patterns.

### Compressor API

Add a public `Compressor` interface parallel to `Decompressor`.

This should mirror the reader path:

- the reader has one decompressor registry keyed by `CompressionCode`;
- that registry is used for both internal sections and tile payloads;
- the writer should have one compressor registry keyed by `CompressionCode`;
- that registry should be used for internal sections and for tile payloads when the caller chooses
  an uncompressed tile-input mode.

Required behavior:

- Accept input `ByteString`.
- Return compressed `ByteString`.
- Receive limits.
- Support `CompressionCodes.None` by default.
- Support caller registration for gzip, brotli, zstd, or future compression codes.

Platform default compressors may be added where available, but writer support must not assume gzip
is available on every target. `RESEARCH.md` documents current decompressor platform differences.

Tile compression must be explicit at the API boundary:

- Stored tile input: tile payload bytes are already in the stored form described by the archive tile
  compression code, so the writer does not transform them.
- Uncompressed tile input: tile payload bytes are uncompressed, so the writer compresses them using
  the archive tile compression code and registered compressor.

Do not infer the mode from `tileCompression`; that would make double-compression easy.

### Final Output Assembly

The final archive writer must be append-only over `ByteSink`.

Before writing byte 0 to the final output, the implementation must know:

- Stored tile payload lengths.
- Tile data section-relative offsets.
- Directory entries.
- Serialized and compressed root directory bytes.
- Serialized and compressed leaf directory bytes.
- Serialized and compressed metadata bytes.
- Header counts.
- Final section offsets and lengths.

This can be achieved by building from repeatable in-memory tile payload inputs, or by staging tile
payloads before final output assembly. `RESEARCH.md` documents that the Go reference implementation
stages tile payloads in a temporary file and then writes the final PMTiles file sequentially.

The core writer API should not require random access to the final output destination. This keeps S3,
HTTP, cloud multipart uploads, and async filesystem sinks viable.

## Internal Additions And Refactors

### Binary Writer

Add internal binary writing utilities for:

- UInt8.
- Little-endian uint64.
- Little-endian int32.
- Varint uint64.
- Position encoding.

This should pair with the existing `BinaryReader` rather than expanding read-only logic into ad hoc
byte-array writes.

### Header Serialization

Add production internal header serialization. `HeaderFixtures.kt` has test scaffolding that creates
header bytes today, but that code should not be promoted as production code.

Required behavior:

- Emit exactly 127 bytes.
- Emit fixed magic and version 3.
- Encode all section offsets and lengths as little-endian uint64.
- Encode positions using longitude/latitude scaled by 10,000,000 into little-endian int32.
- Validate zooms, bounds, center, section layout, and root location before or after serialization.

The reader's `parseHeader` and `PmTiles.open` should be used in tests to validate emitted headers.
Header serializer tests must also include independent expected-byte assertions for representative
fields so they are not only testing production writer output with production reader input.

### Directory Encoding

Add production internal directory encoding. `HeaderFixtures.kt` has a test-only `encodeDirectory`
helper today, but production encoding should be implemented as its own internal component rather
than lifted from test scaffolding.

Required behavior:

- Reject empty directory entry lists.
- Require strictly increasing TileIDs.
- Require nonzero lengths.
- Encode entry count.
- Encode TileID deltas.
- Encode run lengths.
- Encode lengths.
- Encode offset shorthand exactly as described in `RESEARCH.md`.
- Compress each directory using internal compression.

The existing decoder in `Directory.kt` should remain the conformance oracle for round-trip tests.
Directory encoder tests must also assert independent expected bytes for representative entries,
including offset shorthand, so encoder coverage is not purely a round-trip through the local
decoder.

### Directory Partitioning

Add root/leaf directory construction.

Required behavior:

- First attempt a direct root directory when the compressed root fits the target root length.
- If it does not fit, split tile entries into leaf directories and make root entries point to those
  leaves.
- Ensure the final compressed root length is at most the target length.
- Ensure leaf directories are ordered by starting TileID.
- Ensure leaf directory offsets are relative to the leaf directories section.
- Ensure tile offsets are relative to the tile data section.
- Do not create nested leaf directories.

`RESEARCH.md` summarizes the reference Go `BuildDirectories` behavior. The implementation does not
have to clone that algorithm exactly, but must satisfy the same PMTiles constraints.

### Tile Entry Assembly

Add internal logic to convert tile inputs into `DirectoryEntry` values.

Required behavior:

- Sort input tiles by TileID.
- Reject duplicate TileIDs.
- Produce increasing TileIDs.
- Compute section-relative tile offsets.
- Compute stored payload lengths.
- Compute addressed tile count.
- Compute tile entry count.
- Compute tile content count.
- Support contiguous offsets.
- Deduplicate byte-identical logical tile payloads when deduplication is enabled.
- For stored tile input, hash the stored input bytes.
- For uncompressed tile input, hash the uncompressed input bytes before compression, matching the
  go-pmtiles policy documented in `RESEARCH.md`.
- Use the first stored representation for later duplicate input payloads.
- Use FNV-1a 128-bit as the deduplication key, matching go-pmtiles `fnv.New128a()`. Do not retain
  prior full payloads solely to verify hash collisions.
- Coalesce consecutive TileIDs that resolve to the same stored payload offset into one tile entry
  whose `runLength` covers the consecutive range.
- For non-consecutive duplicate payloads, emit separate tile entries that point back to the first
  stored payload offset.
- When deduplication is disabled, store one payload per input tile and emit `runLength = 1`.

### Metadata Serialization

Add metadata serialization for writer input.

Required behavior:

- Validate that metadata is a JSON object.
- Encode as UTF-8 bytes.
- Compress with internal compression.
- Enforce MVT `vector_layers` requirement.

Use `kotlinx.serialization.json` already present in `commonMain`.

### Error Codes

Consider adding writer-specific error codes only where existing codes do not fit cleanly.

Likely reusable existing codes:

- `InvalidHeader`
- `InvalidDirectory`
- `InvalidMetadata`
- `InvalidTileCoordinate`
- `UnsupportedCompression`
- `LimitExceeded`

Possible additions:

- `SinkUnavailable`, analogous to `SourceUnavailable`.
- `InvalidTileInput`, if tile input ordering/duplicates need clearer reporting than
  `InvalidDirectory`.

## API Design Requirements

- Prefer a cohesive final API over preserving the existing reader API exactly.
- Treat reader API breakage as acceptable when it removes awkwardness, duplicated concepts, or
  reader-only assumptions that would make writer support worse.
- Keep writer APIs in the `org.maplibre.spatialk.pmtiles` package.
- Use builder DSLs for option bags that may grow.
- Use inline value classes for extensible spec codes.
- Avoid many-default public constructors for growing writer configuration.
- Hide Kotlin-only helpers from foreign APIs with `@HiddenFromObjC`.
- Use `@ShouldRefineInSwift` where Kotlin API shape needs Swift wrappers.
- Avoid making Kotlin call sites awkward solely to optimize Swift.
- Keep low-level section models internal unless there is a clear public use case.

## Validation Requirements

Writer output must satisfy:

- Header is exactly 127 bytes.
- Magic bytes are `PMTiles`.
- Version is `3`.
- Root directory is non-empty.
- Root directory is fully contained in the first 16 KiB.
- Header plus compressed root directory is at most 16 KiB in canonical layout.
- Sections do not overlap.
- Section offsets are absolute.
- Directory offsets are section-relative.
- Metadata is a JSON object.
- MVT metadata contains `vector_layers`.
- Directory entry lengths are nonzero.
- Directory TileIDs are strictly increasing.
- No nested leaf directories are emitted.
- Header counts are exact for normal writer output.
- Tile type and compression codes match stored payloads.

## Test Requirements

### Fixture Inventory

Use the existing fixture inventory documented in `pmtiles/src/commonTest/resources/README.md` as the
source of truth for checked-in PMTiles files. Any new checked-in fixture must be listed there with
its origin and generation command.

Existing fixtures to keep using:

- `pmtiles-js-test-fixture-1.pmtiles`: copied from
  `protomaps/PMTiles/js/test/data/test_fixture_1.pmtiles`; one-tile MVT fixture with gzip
  internal/tile compression.
- `pmtiles-js-test-fixture-2.pmtiles`: copied from
  `protomaps/PMTiles/js/test/data/test_fixture_2.pmtiles`; second upstream JS fixture for reader
  conformance.
- `pmtiles-js-test-fixture-mlt.pmtiles`: copied from
  `protomaps/PMTiles/js/test/data/test_fixture_mlt.pmtiles`; MLT fixture currently opened only in
  lenient mode because it has an empty root directory.
- `protomaps-vector-odbl-firenze.pmtiles`: copied from
  `protomaps/PMTiles/spec/v3/protomaps(vector)ODbL_firenze.pmtiles`; vector MVT fixture with real
  metadata and `vector_layers`.
- `stamen-toner-raster-cc-by-odbl-z3.pmtiles`: copied from
  `protomaps/PMTiles/spec/v3/stamen_toner(raster)CC-BY+ODbL_z3.pmtiles`; PNG raster fixture that
  exercises leaf directory lookup.
- `usgs-mt-whitney-8-15-webp-512.pmtiles`: copied from
  `protomaps/PMTiles/spec/v3/usgs-mt-whitney-8-15-webp-512.pmtiles`; WebP raster fixture.
- `go-pmtiles-unclustered.pmtiles`: copied from
  `protomaps/go-pmtiles/pmtiles/fixtures/unclustered.pmtiles`; unclustered reference fixture.
- `input-go-pmtiles-unclustered.pmtiles` and `generated-go-pmtiles-unclustered-clustered.pmtiles`:
  local pair where the generated fixture is produced from the input using `go-pmtiles cluster`.
- `pmtiles-js-empty.pmtiles`, `pmtiles-js-invalid.pmtiles`, and `pmtiles-js-invalid-v4.pmtiles`:
  copied from upstream JS tests and retained for reader negative conformance, not writer output.

Existing fixtures are reader fixtures. Writer tests should not overwrite them.

### Writer Fixture Policy

Do not add new checked-in writer-generated PMTiles fixtures for v1. Writer tests should generate
writer output in memory during the test, then open that output with the existing reader.

Use existing upstream fixtures as source material and conformance references:

- Use `go-pmtiles-unclustered.pmtiles` as the source for a small PNG archive. Read its tile payloads
  and header/metadata with the current reader, write a new archive in memory, then reopen and
  compare tile presence, payload bytes, tile type, compression, counts, bounds, and center.
- Use `pmtiles-js-test-fixture-1.pmtiles` as the source for a one-tile gzip MVT payload and MVT
  metadata behavior. The writer should store the already-compressed tile payload bytes and label
  tile compression as gzip; it does not need to gzip-compress the tile itself.
- Use `protomaps-vector-odbl-firenze.pmtiles` as the source for realistic MVT metadata with
  `vector_layers`.
- Use `stamen-toner-raster-cc-by-odbl-z3.pmtiles` as source data for leaf-directory writer tests.
  The writer test can force leaf partitioning by using a deliberately small root directory limit,
  rather than checking in a new large generated fixture.

### Unit Tests

Add tests for:

- Binary writer little-endian integer encoding.
- Varint encoding boundaries.
- Position encoding and rounding/truncation behavior.
- Header serialization.
- Directory encoding direct round trip through existing `decodeDirectory`.
- Offset shorthand encoding.
- Directory partitioning with no leaves.
- Directory partitioning with leaves.
- Metadata validation and serialization.
- Compressor registration and unsupported compression failures.

### Round-Trip Reader Tests

For writer-produced archives, first generate archives in memory during tests when possible. Open the
result with `PmTiles.open` in strict mode and verify:

- Header fields.
- Counts.
- Bounds and center.
- Tile type.
- Compression codes.
- Raw metadata.
- Parsed metadata.
- Tile lookup.
- Stored tile payload bytes.
- Decompressed tile payload bytes when compression is supported.

### Required Archive Cases

Cover these cases using generated-in-test archives and existing checked-in upstream fixtures as
source material:

- Minimal one-tile uncompressed PNG archive: generate in test from the one-pixel PNG payload used by
  the Go minimal example.
- Multiple uncompressed tiles in increasing TileID order: generate in test.
- Archive with metadata optional keys: generate in test.
- MVT archive with `vector_layers`: use metadata from `protomaps-vector-odbl-firenze.pmtiles`.
- Archive requiring leaf directories: use both generated-in-test bytes with a small configured root
  limit and tile payloads sourced from `stamen-toner-raster-cc-by-odbl-z3.pmtiles`.
- Existing gzip tile payload preservation: use `pmtiles-js-test-fixture-1.pmtiles`.
- Run-length entry generation: generate consecutive TileIDs with identical stored payload bytes and
  verify `addressedTiles > tileEntries`.
- Hash-based deduplication: generate non-consecutive tiles with identical stored payload bytes and
  verify `tileContents < tileEntries`.
- Deduplication disabled: generate duplicate payloads with deduplication disabled and verify
  `tileContents == tileEntries`.

### Negative Tests

Include:

- Duplicate TileID.
- Out-of-order TileID input when sorted input is required.
- Invalid tile coordinate.
- Invalid bounds.
- Invalid center.
- Metadata that is not a JSON object.
- MVT metadata missing `vector_layers`.
- Unsupported internal compression.
- Unsupported tile compression when uncompressed tile input asks the writer to compress tile bytes.
- Declared tile compression that does not match stored tile bytes; this should fail when the
  produced archive is read/decompressed, because stored tile input is written as provided.
- Root directory too large after partitioning.
- Length or offset overflow.

### Interop Tests

- Keep Kotlin docs tests updated with writer examples.
- Update Swift shims in `pmtiles/src/swiftMain/SpatialKPmtiles.swift`.
- Add Swift docs tests in `pmtiles/src/swiftTest/SwiftDocsTest.swift`.
- Swift docs tests should provide useful package-documentation snippets and prove API usability from
  Swift. They do not need full writer behavior coverage.
- Swift snippets should cover the important call shapes: writer option construction, Swift sinks,
  stored tile input, uncompressed tile input with a Swift-provided compressor, representative error
  propagation, and reopening a writer-produced archive through the Swift reader API.
- In an opt-in JVM/external conformance task, generate the writer test archives in a temporary
  directory and verify them with the Go `pmtiles verify` CLI when the CLI is available.

## Documentation Updates

Update:

- `pmtiles/MODULE.md`
- `docs/src/content/docs/pmtiles/index.mdx`
- Kotlin docs tests used by doc snippets.
- Swift docs tests and package docs.

Docs should state:

- The module can read and write PMTiles v3 archives.
- The writer accepts stored tile payload bytes and uncompressed tile payload bytes through explicit
  input modes.
- Compression availability depends on platform and registered compressors.
- Filesystem/HTTP sinks are caller-provided unless platform-specific helpers are added.

## Change Management Notes

- Compatibility preservation is not required if a breaking redesign produces a cleaner reader/writer
  API.
- Public API changes require API check updates and documentation updates.
- Adding production serialization internals should not expose them publicly unless necessary.
- After production serializer tests exist, test fixture helpers may call production serialization
  where serialization is not the behavior under test. Serializer tests need independent
  expected-byte coverage.
- If public error codes are added, API check files must be updated.
- If platform compressor defaults are added, target-specific source sets and tests must be updated.
- Existing reader strictness should not be relaxed to accommodate writer output; writer output must
  satisfy the reader.
- Swift shims and Swift docs tests must be updated with public writer API changes.
