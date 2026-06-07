# PMTiles Writer Research

This document records reference information for adding PMTiles v3 writing support to the `pmtiles`
module. It is intentionally descriptive, not a plan. See `SPEC.md` for the concrete writer-support
specification.

## Primary References

- PMTiles v3 specification: https://github.com/protomaps/PMTiles/blob/main/spec/v3/spec.md
- PMTiles v3 changelog: https://github.com/protomaps/PMTiles/blob/main/spec/v3/CHANGELOG.md
- Protomaps PMTiles docs: https://docs.protomaps.com/pmtiles/
- Protomaps CLI docs: https://docs.protomaps.com/pmtiles/cli
- Reference Go implementation package docs:
  https://pkg.go.dev/github.com/protomaps/go-pmtiles/pmtiles
- Reference Go implementation repository: https://github.com/protomaps/go-pmtiles

Relevant local reader files:

- Public models and TileID conversion:
  `pmtiles/src/commonMain/kotlin/org/maplibre/spatialk/pmtiles/Models.kt`
- Public options and builder-style option bags:
  `pmtiles/src/commonMain/kotlin/org/maplibre/spatialk/pmtiles/Options.kt`
- Archive open/read API:
  `pmtiles/src/commonMain/kotlin/org/maplibre/spatialk/pmtiles/PmTilesArchive.kt`
- Header parser and validation:
  `pmtiles/src/commonMain/kotlin/org/maplibre/spatialk/pmtiles/internal/HeaderParser.kt`
- Directory decoder and lookup model:
  `pmtiles/src/commonMain/kotlin/org/maplibre/spatialk/pmtiles/internal/Directory.kt`
- Binary reader:
  `pmtiles/src/commonMain/kotlin/org/maplibre/spatialk/pmtiles/internal/BinaryReader.kt`
- Compression/decompression support:
  `pmtiles/src/commonMain/kotlin/org/maplibre/spatialk/pmtiles/internal/Decompressors.kt`
- Test-only header and directory encoders:
  `pmtiles/src/commonTest/kotlin/org/maplibre/spatialk/pmtiles/internal/HeaderFixtures.kt`
- Fixture conformance tests:
  `pmtiles/src/commonTest/kotlin/org/maplibre/spatialk/pmtiles/FixtureConformanceTest.kt`

## Format Overview

PMTiles v3 is a single-file archive for tiled data. The recommended MIME type is
`application/vnd.pmtiles`.

The archive has five logical sections:

1. Fixed-size 127-byte header.
2. Root directory.
3. JSON metadata.
4. Optional leaf directories.
5. Tile payload data.

Sections are usually written in that order. The spec allows non-header sections to be relocated, but
the header must start at byte 0 and the root directory must be fully contained in the first 16 KiB
of the file. Because the header is 127 bytes, the normal maximum compressed root directory length is
`16384 - 127 = 16257` bytes.

The existing reader validates this constraint in `HeaderParser.kt` with:

- `HEADER_BYTES = 127`
- `FIRST_READ_BYTES = 16 * 1024`
- `validateRootLocation`

## Header Fields

The header is exactly 127 bytes:

- Magic number: UTF-8 `PMTiles`, 7 bytes.
- Version: `0x03`.
- Root directory offset and length, little-endian uint64.
- Metadata offset and length, little-endian uint64.
- Leaf directories offset and length, little-endian uint64.
- Tile data offset and length, little-endian uint64.
- Number of addressed tiles, tile entries, and tile contents, little-endian uint64.
- Clustered flag: `0x00` or `0x01`.
- Internal compression code.
- Tile compression code.
- Tile type code.
- Min zoom, max zoom.
- Min and max positions.
- Center zoom and center position.

Positions are stored as two little-endian signed int32 values:

- Bytes 0..3: longitude multiplied by 10,000,000.
- Bytes 4..7: latitude multiplied by 10,000,000.

The existing parser builds `ArchiveHeader`, `ArchiveSection`, `HeaderCounts`, `LonLatBounds`, and
`TileCenter` from these fields. Their constructors are internal, which is appropriate for parsed
reader models but means writer-specific public input models should not depend on callers
constructing those parsed models directly.

## Compression Codes

Spec-defined compression codes:

- `0x00`: unknown
- `0x01`: none
- `0x02`: gzip
- `0x03`: brotli
- `0x04`: zstd

The existing reader exposes these as inline `CompressionCode` values and constants under
`CompressionCodes`. This follows the project invariant that spec values that may grow should be
inline value classes, not enums.

Current built-in decompressor availability:

- JVM: none and gzip.
- Native: none and gzip through zlib.
- Web: none and gzip only when the runtime has `DecompressionStream`.
- WASI: none only.

The reader has a public `Decompressor` interface and one decompressor registry keyed by
`CompressionCode`. The same registry is used for internal sections and for
`readDecompressedTile(...)`. Writer support should mirror that with a public `Compressor` interface
and one compressor registry keyed by `CompressionCode`.

The writer API still needs an explicit tile payload mode so callers cannot accidentally
double-compress already-stored tile bytes:

- Stored tile input: write tile payload bytes as provided.
- Uncompressed tile input: compress tile payload bytes with the archive tile compression code and
  registered compressor.

## Tile Type Codes

Spec-defined tile type codes:

- `0x00`: unknown / other
- `0x01`: MVT vector tile
- `0x02`: PNG
- `0x03`: JPEG
- `0x04`: WebP
- `0x05`: AVIF
- `0x06`: MapLibre Vector Tile

The local reader exposes these as inline `TileTypeCode` values and constants under `TileTypeCodes`,
matching project API invariants for extensible spec values.

## Metadata

The metadata section must be a valid UTF-8 JSON object. For MVT tile type, the metadata object must
contain `vector_layers` as described by TileJSON 3.0.

Spec-defined optional metadata keys include:

- `name`
- `description`
- `attribution`
- `type`
- `version`
- `encoding`
- `vector_layers`

PMTiles v3.6 added `encoding = "terrarium"` for terrain datasets.

The existing reader:

- Returns raw metadata JSON through `rawMetadataJson()`.
- Parses selected typed fields through `metadata()`.
- In strict mode, rejects malformed metadata and MVT metadata missing `vector_layers`.
- In lenient mode, records warnings for recoverable metadata issues.

Writer support must preserve the requirement that stored metadata is a JSON object and must account
for the MVT `vector_layers` rule.

## Directories

A directory is a non-empty list of entries. Each entry has:

- `TileID`
- `Offset`
- `Length`
- `RunLength`

For tile entries:

- `RunLength > 0`.
- `Offset` is relative to the first byte of the tile data section.
- `Length` is the stored tile payload length.

For leaf directory entries:

- `RunLength == 0`.
- `Offset` is relative to the first byte of the leaf directories section.
- `Length` is the compressed length of that leaf directory.

Entry length must be greater than zero. Directories must be non-empty. Leaf directories should be
ordered ascending by starting TileID. The spec discourages nested leaf directories.

The local reader allows nested leaf directories only in lenient mode during lookup and records
`ArchiveWarningCode.NestedLeafDirectory`.

## Directory Encoding

Encoded directory layout:

1. Number of entries as a little-endian varint.
2. Delta-encoded TileIDs, each as varint.
3. RunLengths, each as varint.
4. Lengths, each as varint.
5. Offsets, each as varint.

TileID deltas are relative to the previous TileID, starting from zero. TileIDs must be strictly
increasing.

Offsets are encoded as:

- `0` when the current entry starts exactly after the previous entry.
- Otherwise `offset + 1`.

The first directory entry cannot use the contiguous-offset shorthand because there is no previous
entry.

After this logical encoding, each directory is compressed according to the header's internal
compression. Leaf directories are compressed individually, not as one combined leaf section.

The existing test fixtures in `HeaderFixtures.kt` contain an uncompressed `encodeDirectory` helper
for building reader-test archives. That helper is useful as evidence of needed test scaffolding, but
it is not production code. Writer support should add a proper production directory encoder and
independent serializer tests first. After that, fixture builders can call production serialization
when serialization is not the behavior under test.

## TileID Ordering

TileID is a cumulative position over Hilbert curves starting at zoom 0. Official examples include:

- `0/0/0 -> 0`
- `1/0/0 -> 1`
- `1/0/1 -> 2`
- `1/1/1 -> 3`
- `1/1/0 -> 4`
- `2/0/0 -> 5`
- `12/3423/1763 -> 19078479`

The local `TileIds` implementation already matches these examples and supports zooms `0..31`.

Writers that emit clustered archives should write tile entries in increasing TileID order. The
clustered flag means:

- Tile data is ordered by TileID.
- Offsets are either contiguous with the previous offset plus length, or refer to an earlier offset
  when deduplication is used.
- The first tile entry in the directory has offset 0.

## Counts

Header counts:

- Addressed tiles: total number of addressed tiles before run-length encoding.
- Tile entries: total number of tile entries where `RunLength > 0`.
- Tile contents: total number of blobs in the tile data section.

The spec says `0` means unknown. A writer can compute exact values for normal archive creation, so
unknown zero counts should generally be unnecessary.

When using run-length encoding, addressed tiles may exceed tile entries. When using deduplication,
tile contents may be less than tile entries.

## Reference Implementation Notes

The Go reference implementation exposes these writer-relevant functions:

- `SerializeHeader(header HeaderV3) []byte`
- `SerializeEntries(entries []EntryV3, compression Compression) []byte`
- `SerializeMetadata(metadata map[string]interface{}, compression Compression) ([]byte, error)`
- `BuildDirectories(entries []EntryV3, targetRootLen int, compression Compression) ([]byte, []byte, int)`

The Go `SerializeEntries` implementation:

- Writes entry count.
- Writes delta TileIDs.
- Writes run lengths.
- Writes lengths.
- Writes offsets using contiguous shorthand.
- Compresses the directory according to the requested internal compression.

The Go `BuildDirectories` implementation:

- Tries to serialize all entries into the root when the entry count is below a threshold and the
  compressed root fits the target length.
- Otherwise builds leaf directories and root entries that point to them.
- Starts from a leaf size derived from entry count, clamps it to at least 4096, and increases it
  iteratively until the compressed root fits.
- Leaves a TODO for a mixed root containing both direct tile entries and leaf pointers.

The Go conversion path:

- Builds a sorted TileID set.
- Writes tile payloads in increasing TileID order to a temporary file.
- Optionally gzip-compresses MVT tile payloads.
- Optionally deduplicates tile payloads by hash.
- Merges adjacent addressed TileIDs into run-length entries when they resolve to the same stored
  payload offset.
- Records directory entries as staged payloads are written.
- Builds root and leaf directories.
- Serializes metadata.
- Sets zoom/center defaults.
- Sets canonical section offsets.
- Writes the final output sequentially: header, root directory, metadata, leaf directories, then
  tile data copied from the temporary file.

The reference implementation therefore does not require random access to the final PMTiles output.
It does require staged tile payloads before final output assembly, because the header and
directories need final lengths, offsets, and counts before byte 0 of the final archive is written.
The Go implementation satisfies that requirement with a seekable temporary file for tile payload
staging, then emits the final archive as an append-only stream.

The Go minimal example demonstrates that a valid archive can be written with:

- Header.
- One uncompressed root directory entry.
- `{}` metadata.
- One uncompressed PNG tile.

## Common Implementation Behavior

Common PMTiles writers generally emit optimized, canonical archives rather than every variation the
spec can represent.

- `go-pmtiles convert`, `cluster`, `merge`, and `extract` write canonical section offsets and exact
  header counts.
- `go-pmtiles convert`, `cluster`, and `merge` emit clustered archives. `cluster` exists
  specifically to rewrite an unclustered archive into clustered order.
- `go-pmtiles` uses root-to-leaf directory partitioning through `BuildDirectories`; it does not
  create nested leaf directories.
- `go-pmtiles convert` and `cluster` deduplicate by default unless the caller passes the
  no-deduplication flag. Their resolver also emits run-length entries when consecutive TileIDs share
  the same stored payload offset.
- The upstream Python `pmtiles` writer also deduplicates by payload hash, merges adjacent identical
  payloads into run-length entries, and can emit `clustered = false` if tiles are written out of
  TileID order.
- `rio-pmtiles` writes clustered raster archives with exact counts and root/leaf partitioning, but
  does not rely on deduplication or run-length entries for its normal raster output.

Implications for this module:

- Exact counts, canonical section order, root-to-leaf directories, and clustered output are normal
  writer behavior and should be implemented in writer v1.
- Run-length entries are used by common implementations and are simple enough to include for
  consecutive TileIDs with identical stored payload bytes.
- Hash-based deduplication is used by common implementations and should be implemented in writer v1.
  Hash logical input tile bytes with FNV-1a 128-bit: stored bytes for stored tile input, and
  uncompressed bytes before compression for uncompressed tile input. Reuse the first stored
  representation for later duplicates. This preserves the go-pmtiles policy choice of hashing input
  bytes with a stable 128-bit FNV-1a hash. The upstream Python writer also hashes input bytes but
  uses Python's runtime `hash(data)`, which is not stable enough for this module. Neither reference
  writer retains prior full payload bytes solely to verify hash collisions. Keep deduplication
  enabled by default with an option to disable.
- Unclustered output is useful mainly when a writer streams tile data in input order without
  staging/sorting. This module's writer already stages/builds before final output, so clustered
  output is the better v1 behavior.

## Existing Local API Patterns

The local module uses:

- `object PmTiles` as the factory namespace.
- Option bags with private primary constructors, public default constructors, mutable nested
  `Builder`, `toBuilder()`, and Kotlin DSL companion `build`.
- `@ShouldRefineInSwift` for APIs that need Swift wrappers/refinement.
- `@HiddenFromObjC` for Kotlin-only helper APIs.
- Inline value classes for extensible spec codes.
- `PmTilesException` plus stable `PmTilesErrorCode`.
- Internal constructors for parsed result models.

Writer APIs should fit these patterns to keep Kotlin ergonomic while preserving usable Swift/ObjC
exports.

## Output Interface Notes

The final PMTiles output can be append-only. It does not need random access if the writer builds or
stages all information needed for the header, root directory, metadata, leaf directories, and tile
data before writing the final archive.

The existing reader uses `ByteRangeSource` because reading PMTiles requires random byte ranges. A
writer output abstraction has different requirements:

- It must support ordered byte appends.
- It should be suspending so callers can back it with filesystem, HTTP, S3, cloud-storage multipart
  upload, or other non-blocking/asynchronous destinations.
- It should surface cancellation.
- It should not require a new I/O dependency.

`kotlinx-io` has useful synchronous `Sink` and `RawSink` types, and platform adapters such as JVM
`OutputStream.asSink()` and Apple `NSOutputStream.asSink()`. However, those APIs are synchronous.
The upstream async API issue, https://github.com/Kotlin/kotlinx-io/issues/163, remains open and
states that `kotlinx-io` currently provides only synchronous APIs and has no particular async plan.

Because writer outputs may be filesystems or network/cloud storage, a synchronous `Sink`/`RawSink`
should not be the core output interface. A small project-local suspending byte sink is a better fit.
No `kotlinx-io` adapter is necessary for initial writer support.

## Existing Local Test Coverage

Important current tests:

- `TileIdsTest`: official examples, round trips, high-zoom coordinates, rejection cases.
- `DirectoryDecodingTest`: explicit offsets, contiguous shorthand, run lengths, leaf entries,
  malformed directories, limits, varint failures.
- `OpenArchiveTest`: every header field, invalid magic/version/header, section layout, root
  location, compression support, lenient empty root.
- `TileLookupTest`: direct root lookup, run coverage, leaf lookup/caching, nested leaf strict vs
  lenient behavior.
- `MetadataTest`: metadata parsing, UTF-8 validation, strict/lenient metadata behavior.
- `FixtureConformanceTest`: upstream fixtures and generated go-pmtiles fixture.
- `TileBytesTest`: stored and decompressed tile reads, tile coalescing.
- Swift docs tests: public API usability from Swift and useful documentation snippets, not full
  behavior conformance.

Writer tests should reuse the reader to validate emitted archives wherever possible. For low-level
header and directory serialization, reader round-trips are necessary but not sufficient; include
independent expected-byte assertions or external reference vectors to avoid testing the writer only
against another local component with the same assumptions.

The checked-in fixture inventory and provenance live in
`pmtiles/src/commonTest/resources/README.md`. Existing upstream fixtures are reader conformance
inputs and should not be overwritten by writer tests. Writer tests can use those fixtures as source
material, generate writer output in memory or in a temporary directory, and then reopen the result
with the reader. Writer v1 should not add new checked-in writer-generated fixtures.

## Implementation Pitfalls

- Root directory size must be checked after compression, not before compression.
- The root directory must end within the first 16 KiB, not merely start there.
- Directory entry lengths must be nonzero.
- Directories must be non-empty.
- TileIDs must be strictly increasing within every directory.
- The first directory entry cannot encode offset as `0`.
- Offsets in directory entries are section-relative, not absolute.
- Header section offsets are absolute archive offsets.
- Leaf directory offsets are relative to the leaf directories section.
- Tile offsets are relative to the tile data section.
- Leaf directories are compressed individually.
- The leaf directories header length is the sum of all compressed leaf directory byte lengths.
- A writer that marks an archive clustered must obey clustered ordering semantics.
- Deduplication can make later entries point to earlier tile payload offsets.
- Run-length encoding changes addressed tile count but not tile content count.
- Metadata must be a JSON object, not an arbitrary JSON value.
- MVT metadata must include `vector_layers`.
- The project supports many multiplatform targets, so gzip compression availability is not uniform.
- Java/ObjC/Swift export ergonomics matter; public constructors with many defaults should be avoided
  for option bags.
- Large archives cannot be efficiently built as a single in-memory `ByteString`.
- Final archive output does not need random access, but large archives need some way to stage tile
  payload bytes or otherwise make payload bytes repeatably available before final output begins.
- `ByteString` sizes and many existing limits ultimately require `Int`, while PMTiles fields are
  uint64. Writer APIs need clear limits and overflow checks.
- The existing `ArchiveHeader`, `ArchiveSection`, `HeaderCounts`, `LonLatBounds`, and `TileCenter`
  models have internal constructors, so they are reader result types rather than writer input
  builders.

## Useful Test Cases For A Writer

Round-trip through the existing reader:

- Minimal one-tile uncompressed PNG archive.
- Empty metadata object.
- Non-empty metadata object with optional keys.
- MVT archive with `vector_layers`.
- MVT archive missing `vector_layers` should fail writer validation or produce a reader strict-mode
  failure, depending on writer validation behavior.
- Multiple tiles in increasing TileID order.
- Contiguous offset shorthand in directory encoding.
- Archive with leaf directories because root exceeds 16257 compressed bytes.
- Leaf directory section length equals sum of compressed leaves.
- Header counts match addressed tiles, tile entries, and tile contents.
- Bounds and center round-trip with 1e-7 scaling.
- Tile type and compression code round-trip.
- Clustered flag semantics are verifiable.

Negative and validation cases:

- Duplicate TileID.
- Out-of-order TileID input if writer requires sorted input.
- Invalid zoom/coordinate.
- Invalid bounds or center.
- Metadata that is not a JSON object.
- Unknown compression without a registered compressor.
- Internal compression that cannot be compressed on the platform.
- Root directory too large after partitioning attempts.
- Tile payload length exceeding supported `Int` or configured writer limits.
- Archive total length or section offset overflow.

Interoperability cases:

- Compare minimal archive fields with the Go minimal example.
- Open writer-produced archives with current `PmTiles.open`.
- Verify writer-produced files with `go-pmtiles verify` in an opt-in external conformance task when
  the CLI is available.
- Ensure Swift can create writer options and provide sink/tile inputs through
  `pmtiles/src/swiftMain/SpatialKPmtiles.swift`. Swift tests should prove representative call shapes
  and docs snippets; full writer behavior coverage belongs in common/platform Kotlin tests.

Existing fixtures useful as writer source material:

- `go-pmtiles-unclustered.pmtiles`: small PNG archive from `protomaps/go-pmtiles`.
- `pmtiles-js-test-fixture-1.pmtiles`: one-tile gzip MVT archive from `protomaps/PMTiles`.
- `protomaps-vector-odbl-firenze.pmtiles`: realistic MVT metadata with `vector_layers`.
- `stamen-toner-raster-cc-by-odbl-z3.pmtiles`: PNG raster archive with leaf directories.

## PMTiles v3 Features Writer V1 Does Not Emit

Apart from compression methods without registered compressors and tile payload parsing/construction,
writer v1 intentionally does not emit these valid or tolerated PMTiles variations:

- Non-canonical section ordering. The spec allows relocating all sections except the header, but
  writer v1 emits canonical section order.
- Unclustered archives. The spec supports `clustered = 0`; writer v1 sorts input tiles by TileID and
  emits `clustered = 1`.
- Unknown header counts. The spec allows count fields to be `0` for unknown; writer v1 computes
  exact counts.
- Nested leaf directories. The spec discourages them and asks writer authors to open an issue if
  they need them; writer v1 emits only root-to-leaf directory references.

The current reader should continue to support or reject these cases according to its existing
strict/lenient behavior. These are output-scope decisions for writer v1, not limitations of the
PMTiles reader.
