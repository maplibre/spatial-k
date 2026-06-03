# PMTiles v3 Kotlin Multiplatform Library Specification

This is a complete-state design specification for a Kotlin Multiplatform PMTiles v3 library. It
describes the completed library’s specified public surface and runtime behavior. It intentionally
does **not** duplicate the PMTiles binary specification; the official PMTiles v3 specification and
changelog remain normative.

## 1. Summary

The library implements PMTiles v3 as a **single-file tile archive/container**. It is responsible for
locating tiles, reading PMTiles headers, decoding directories, resolving Hilbert TileIDs, reading
metadata, applying PMTiles-level compression, and exposing typed archive information. It is **not**
responsible for decoding MVT, MLT, PNG, JPEG, WebP, AVIF, terrain pixels, or application-specific
tile payloads.

The central design is:

```text
ByteRangeSource -> PmTilesArchive -> tile ranges / tile bytes / metadata
```

Most of the library lives in `commonMain`: binary decoding, unsigned/varint handling, Hilbert math,
directory decode, metadata parsing, validation, caches, read orchestration, and API models. The core
library does not provide HTTP, filesystem, object-store, Blob/File, or Node source implementations.
Callers provide a `ByteRangeSource`.

The public API is a single Kotlin API designed to export cleanly to Swift/Apple through
Kotlin/Native interop:

| Audience      | Primary API style                                                                                                                                     |
| ------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------- |
| Kotlin        | `suspend` functions, immutable data classes, nullable missing tiles, caller-provided `ByteRangeSource`, built-in PMTiles codecs, and caches.          |
| JVM Kotlin    | Same Kotlin API. No JVM source adapters and no dedicated pure-Java wrapper are part of this specification.                                            |
| Swift / Apple | Kotlin/Native exported API: suspend functions as completion handlers and Swift async calls, simple DTOs, `UInt`/`ULong` values, and custom providers. |

PMTiles v2 and earlier formats are not supported by the core reader. Any old-format converter
belongs in a separate compatibility tool.

---

## 2. Normative references

The library must follow the official PMTiles v3 format definition. This document maps the format
into library behavior and APIs.

| Reference                                                                                                                                            | Role in this library spec                                                                                                                  |
| ---------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------ |
| [PMTiles Version 3 Specification](https://github.com/protomaps/PMTiles/blob/main/spec/v3/spec.md)                                                    | Normative binary format definition: header, sections, directories, metadata, compression, TileIDs, and pseudocode.                         |
| [PMTiles v3 Changelog](https://github.com/protomaps/PMTiles/blob/main/spec/v3/CHANGELOG.md)                                                          | Normative change history for v3.x clarifications and enum additions, MIME type, directory length clarifications, and nested-leaf guidance. |
| [Protomaps PMTiles documentation](https://docs.protomaps.com/pmtiles/)                                                                               | Conceptual background and practical PMTiles usage model.                                                                                   |
| [Protomaps go-pmtiles](https://github.com/protomaps/go-pmtiles)                                                                                      | Compatibility target for archives produced by the primary Protomaps PMTiles tooling.                                                       |
| [TileJSON 3.0 vector_layers](https://github.com/mapbox/tilejson-spec/blob/22f5f91e643e8980ef2656674bef84c2869fbe76/3.0.0/README.md#33-vector_layers) | Required metadata key when the PMTiles header tile type is MVT.                                                                            |
| [Kotlin releases](https://kotlinlang.org/docs/releases.html)                                                                                         | Kotlin release currency. This spec assumes the Kotlin 2.3.x-era feature set as of 2026-06-03.                                              |
| [Kotlin Swift/Objective-C interop](https://kotlinlang.org/docs/native-objc-interop.html)                                                             | Stable Apple interop baseline.                                                                                                             |
| [Kotlin Swift export](https://kotlinlang.org/docs/native-swift-export.html)                                                                          | Experimental direct Swift export constraints and API hygiene.                                                                              |

---

## 3. PMTiles v3 coverage contract

The library is considered PMTiles v3 reader-complete when every official v3 read feature is
represented in the reader, validator, and public model. The following matrix is the library’s
coverage checklist, not a restatement of the spec.

| PMTiles v3 area                    | Library behavior                                                                                                                                                                                                               |
| ---------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| Magic and version                  | Reader validates `PMTiles` magic and version `3`. Unsupported versions fail with a deterministic error.                                                                                                                        |
| Fixed 127-byte header              | Reader parses all fields into an immutable `ArchiveHeader`. Unknown enum values are preserved in raw-code models.                                                                                                              |
| Section offsets and lengths        | Reader validates section arithmetic, overflow, overlap policy, and configured operational limits. Reader supports legal non-canonical relocation except where the official spec restricts layout.                              |
| Root directory within first 16 KiB | Reader opens with an initial first-16-KiB range read and requires the complete compressed root directory to be available there.                                                                                                |
| JSON metadata                      | Reader exposes raw UTF-8 JSON and typed known fields.                                                                                                                                                                          |
| Optional leaf directories          | Reader supports leaf traversal, caching, and configurable nested-leaf depth.                                                                                                                                                   |
| Tile data section                  | Reader returns compressed ranges, compressed tile bytes, or PMTiles-decompressed tile bytes according to read mode.                                                                                                            |
| Hilbert TileID scheme              | Common code implements Z/X/Y to TileID conversion and reverse conversion, including zoom-start offsets and overflow checks.                                                                                                    |
| Directory entries                  | Common code supports TileID deltas, run lengths, lengths, offsets, leaf entries, tile entries, contiguous-offset shorthand, whole-directory decoding, and binary search by predecessor entry.                                  |
| Varint encoding                    | Common code implements bounded unsigned varint reads with malformed, unterminated, and overflow detection.                                                                                                                     |
| Internal compression               | Applies to root directory, metadata, and each leaf directory independently. Required for opening and directory traversal.                                                                                                      |
| Tile compression                   | Applies uniformly to all tile payloads in the archive. Required only when callers request decompressed tile payloads. Range APIs work without tile decompression.                                                              |
| Compression codes                  | Supports `Unknown`, `None`, `gzip`, `brotli`, and `zstd` model values. `None` and gzip decoding are built in. Unknown, brotli, and zstd values are preserved in raw header models and fail whenever decompression is required. |
| Tile type codes                    | Supports `Unknown/Other`, `MVT`, `PNG`, `JPEG`, `WebP`, `AVIF`, and `MLT`, plus unknown raw codes. Tile payloads remain opaque.                                                                                                |
| Clustered flag                     | Reader reports the flag. Normal tile reads do not prove clustered layout.                                                                                                                                                      |
| Counts                             | Header counts are exposed as nullable semantic `ULong` values because PMTiles uses `0` for “unknown”. Raw unsigned values remain accessible for diagnostics.                                                                   |
| Bounds and center                  | Header positions are decoded into lon/lat models, with strict validation of sane coordinate ranges.                                                                                                                            |
| MVT metadata requirement           | Strict metadata parsing requires `vector_layers` when tile type is MVT. Lenient mode warns.                                                                                                                                    |

---

## 4. Goals and non-goals

### Goals

- PMTiles v3 read support covering header, root directory, metadata, leaf directories, tile data,
  TileIDs, compression, and validation.
- Tile-payload agnosticism: core returns bytes and metadata; payload decoders live elsewhere.
- One byte-range abstraction supplied by the caller.
- Fast range-oriented reads: single first-16-KiB open read, leaf-directory cache, tile payload cache
  disabled by default, and in-flight request de-duplication.
- Safe behavior on untrusted archives: explicit limits, bounded decompression, overflow checks,
  deterministic errors.
- Kotlin API that works for Kotlin Multiplatform consumers and exports cleanly to Swift/Apple using
  Kotlin Multiplatform export support.
- Simple DTOs that work for Kotlin callers and Swift/Apple consumers without a separate rich API.

### Non-goals

- No PMTiles v1/v2 support in the core reader.
- No PMTiles writing. Writer behavior belongs in a separate specification.
- No MVT, MLT, image, raster terrain, or vector-tile semantic decoding in core.
- No in-place PMTiles mutation.
- No built-in HTTP, filesystem, object-store, Blob/File, or Node source implementations.
- No JavaScript or TypeScript foreign export API in this specification. Kotlin/JS consumers use the
  same Kotlin Multiplatform API as other Kotlin callers. The implementation does not add
  `@JsExport`, generated TypeScript declarations, or JavaScript facade APIs.
- No map renderer integration in core.
- No package publishing, Gradle metadata, artifact-coordinate, build-system, or repository-layout
  specification. Kotlin package names are public API and are specified below.

---

## 5. Architecture

### 5.1 Core responsibilities

| Subsystem                   | Location             | Responsibility                                                                                                                                                                                                                                           |
| --------------------------- | -------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Binary primitives           | `commonMain`         | Little-endian numeric reads, unsigned 64-bit parsing policy, varints, bounds checks.                                                                                                                                                                     |
| Tile ID math                | `commonMain`         | Hilbert conversion, zoom-start offsets, coordinate validation, TileID-to-Z/X/Y reverse conversion.                                                                                                                                                       |
| Directories                 | `commonMain`         | Directory decoding, validation, predecessor binary search, run-length handling, leaf traversal.                                                                                                                                                          |
| Metadata                    | `commonMain`         | Load internal-compressed UTF-8 JSON; expose raw JSON and typed convenience fields.                                                                                                                                                                       |
| Compression                 | `commonMain`         | Compression code modeling, built-in decoder lookup, decode limits, and read modes.                                                                                                                                                                       |
| Compression implementations | platform source sets | `None` is built in on every supported target. gzip is built in on JVM and Kotlin/Native targets. JavaScript and WASM gzip actuals compile and throw `NotImplementedError`. brotli/zstd are recognized values but are not decoded by this implementation. |
| Cache                       | `commonMain`         | Header/root memoization, lazy metadata, leaf-directory LRU, and in-flight request de-duplication.                                                                                                                                                        |
| Host exports                | target source sets   | Export annotations and names for Swift/Apple; JVM remains Kotlin-first.                                                                                                                                                                                  |

### 5.2 Object lifecycle

`PmTilesArchive` owns parsed archive state and caches. `open(source)` treats the supplied
`ByteRangeSource` as caller-owned and does not close it. Closing an archive releases archive caches,
cancels archive-managed in-flight work, and leaves the source open. Closing is idempotent.

The archive object is immutable except for caches, request de-duplication maps, and close state.
Header, root directory, options, and source are fixed after `open`.

---

## 6. API principles

| Principle             | Consequence                                                                                                                                |
| --------------------- | ------------------------------------------------------------------------------------------------------------------------------------------ |
| Opaque tile payloads  | Tiles are bytes plus tile type/compression metadata. Core never parses MVT, MLT, raster image formats, or terrain pixels.                  |
| Source agnostic       | All reads go through caller-provided byte ranges. Storage-specific behavior belongs to the caller’s `ByteRangeSource`.                     |
| Coroutine core        | Kotlin IO APIs are suspending. Kotlin/JS callers use Kotlin coroutines. Kotlin/Native exports suspension into Swift/Apple’s async idiom.   |
| Range-first design    | `getTileRange` and `getTileCompressed` are first-class; not every caller wants decompressed bytes.                                         |
| Validation by mode    | Strict mode rejects spec violations; lenient mode surfaces warnings for recoverable anomalies.                                             |
| Interop-safe DTOs     | Public APIs use simple DTOs, `UInt`/`ULong`, no deep generics, no Kotlin collections in hot paths, and no overloaded exported names.       |
| Explicit limits       | Metadata bytes, directory bytes, tile bytes, varint length, directory entries, recursion depth, and coalesced read sizes are configurable. |
| Future enum tolerance | Unknown compression/tile-type codes are preserved in raw header models. Operations that require decoding fail explicitly.                  |
| Deterministic errors  | All failures carry a stable error code suitable for Kotlin and Swift consumers.                                                            |

### 6.1 Package policy

All public declarations in this specification are in one Kotlin package:

```kotlin
package org.maplibre.spatialk.pmtiles
```

The initial public API has no public subpackages. Implementation-only packages are allowed under the
same package root but must expose no public declarations. If a future PMTiles writer is added, it
gets a separate specification before adding public subpackages or new artifacts.

### 6.2 Public naming policy

Public names avoid repeating the module name. The `PmTiles` prefix is reserved for types whose
unprefixed names would be too generic in exported host-language APIs and whose identity is the
library entry point or error domain:

- `PmTilesArchive`
- `PmTilesException`
- `PmTilesErrorCode`

Archive-owned DTOs use domain names instead of the module prefix: `ArchiveHeader`,
`ArchiveMetadata`, `ArchiveOpenOptions`, `ArchiveLimits`, `ArchiveTile`, `ArchiveWarning`, and
`ArchiveWarningCode`. Header-specific submodels use `Header*`, such as `HeaderCounts`.

PMTiles format concepts and byte-range abstractions keep their natural names without a `PmTiles`
prefix: `Compression`, `TileType`, `TileCoord`, `TileIds`, `TileRange`, `ByteRange`,
`ByteRangeSource`, and `ByteRangeDataSource`.

---

## 7. Core domain model

### 7.1 Byte ranges and sources

```kotlin
public data class ByteRange(
    public val offset: ULong,
    public val length: Int
)

public interface ByteRangeSource {
    public suspend fun size(): ULong
    public suspend fun read(range: ByteRange): ByteArray
}
```

`offset` is absolute from the start of the archive. `length` is an `Int` because all supported
targets ultimately allocate byte arrays with platform-specific maximum sizes. PMTiles unsigned
64-bit offsets and lengths are parsed into `ULong` models. Any read whose byte allocation exceeds
configured limits fails with `LimitExceeded` or `RangeOutOfBounds`.

The reader calls `size()` during open. The source must return a stable archive size for the lifetime
of the archive object.

### 7.2 Header

```kotlin
public data class ArchiveHeader(
    public val specVersion: Int,
    public val rootDirectory: ArchiveSection,
    public val metadata: ArchiveSection,
    public val leafDirectories: ArchiveSection,
    public val tileData: ArchiveSection,
    public val counts: HeaderCounts,
    public val clustered: Clustered,
    public val internalCompression: Compression,
    public val tileCompression: Compression,
    public val tileType: TileType,
    public val minZoom: Int,
    public val maxZoom: Int,
    public val bounds: LonLatBounds,
    public val center: TileCenter
)

public data class ArchiveSection(
    public val offset: ULong,
    public val length: ULong
)

public data class HeaderCounts(
    public val addressedTiles: ULong?,
    public val tileEntries: ULong?,
    public val tileContents: ULong?,
    public val rawAddressedTiles: ULong,
    public val rawTileEntries: ULong,
    public val rawTileContents: ULong
)
```

Counts use nullable semantic values because PMTiles uses `0` to mean “unknown”. Raw unsigned count
values are exposed as `ULong`.

### 7.3 Raw-code values

```kotlin
public data class Compression(
    public val code: UInt
) {
    public companion object {
        public val Unknown: Compression = Compression(0u)
        public val None: Compression = Compression(1u)
        public val Gzip: Compression = Compression(2u)
        public val Brotli: Compression = Compression(3u)
        public val Zstd: Compression = Compression(4u)
    }
}

public data class TileType(
    public val code: UInt
) {
    public companion object {
        public val Unknown: TileType = TileType(0u)
        public val Mvt: TileType = TileType(1u)
        public val Png: TileType = TileType(2u)
        public val Jpeg: TileType = TileType(3u)
        public val Webp: TileType = TileType(4u)
        public val Avif: TileType = TileType(5u)
        public val Mlt: TileType = TileType(6u)
    }
}
```

Compression and tile type are raw-code DTOs with named constants. Kotlin and Swift use this same API
shape. Unknown values are represented by `Compression(code)` and `TileType(code)`.

### 7.4 Tile coordinates and TileIDs

```kotlin
public data class TileCoord(
    public val z: Int,
    public val x: Int,
    public val y: Int
)

public object TileIds {
    public fun fromZxy(z: Int, x: Int, y: Int): Long
    public fun toZxy(tileId: Long): TileCoord
    public fun zoomStart(z: Int): Long
}
```

The public coordinate API uses Web tile coordinates. It validates:

- `z >= 0`
- `x` and `y` are in `0 until 2^z`
- `z` is within the PMTiles TileID range representable by signed `Long` and practical platform
  integer APIs
- reverse conversion rejects TileIDs outside supported range

The public `TileCoord` API supports `z` values from `0` through `31`. `x` and `y` are `Int` values
in `0 until 2^z`; `z=31` is the highest zoom whose coordinate range fits the non-negative `Int`
domain. `TileIds.fromZxy` rejects `z > 31` with `InvalidTileCoordinate`. `TileIds.toZxy` rejects
TileIDs outside the `z <= 31` range with `InvalidTileCoordinate`.

### 7.5 Directory entries

```kotlin
internal data class DirectoryEntry(
    internal val tileId: Long,
    internal val offset: ULong,
    internal val length: Int,
    internal val runLength: Int
) {
    internal val isLeaf: Boolean get() = runLength == 0
    internal val isTile: Boolean get() = runLength > 0
}
```

Semantics:

- `runLength == 0` means the entry points to a leaf directory.
- `runLength > 0` means the entry points to a tile blob that applies to
  `tileId until tileId + runLength`.
- Tile-entry offsets are relative to the tile data section.
- Leaf-entry offsets are relative to the leaf directory section.
- Lengths are compressed lengths and must be greater than zero.
- Entries in a directory are sorted by starting TileID.
- Search uses the greatest entry whose `tileId <= targetTileId`, then checks run coverage or
  descends into the leaf.

### 7.6 Tile result models

```kotlin
public data class TileRange(
    public val tileId: Long,
    public val coord: TileCoord,
    public val archiveRange: ByteRange,
    public val tileType: TileType,
    public val compression: Compression,
    public val directoryDepth: Int
)

public data class ArchiveTile(
    public val tileId: Long,
    public val coord: TileCoord,
    public val bytes: ByteArray,
    public val tileType: TileType,
    public val compression: Compression,
    public val wasDecompressed: Boolean,
    public val range: TileRange
)
```

---

## 8. Byte range sources

`ByteRangeSource` is the only IO dependency of the PMTiles core. A source implementation must return
exactly the requested bytes or throw a typed source error.

### 8.1 Source contract

| Requirement       | Behavior                                                                                                                             |
| ----------------- | ------------------------------------------------------------------------------------------------------------------------------------ |
| Addressing        | `offset` is absolute from the start of the archive. `length` is a non-negative byte count.                                           |
| Exact reads       | `read(range)` returns exactly `length` bytes or fails. Partial arrays are never returned.                                            |
| Zero-length reads | Allowed and return an empty array. PMTiles reader code uses zero-length reads only in tests and adapter probes.                      |
| Concurrency       | Sources accept concurrent reads. Implementations for non-thread-safe storage serialize internally before exposing `ByteRangeSource`. |
| Stability         | Sources represent a stable archive snapshot or fail with `SourceChanged`.                                                            |
| Size              | `size()` returns the stable total archive size in bytes.                                                                             |
| Errors            | `PmTilesException` from the source is preserved. Other source exceptions are wrapped as `SourceUnavailable`.                         |

The core library does not inspect storage-specific state. Protocols, paths, validators, redirects,
file identity, object-store metadata, retries, authentication, and similar concerns belong to the
source implementation supplied by the caller.

---

## 9. Reader behavior

### 9.1 Open sequence

`PmTilesArchive.open(source, options)` performs:

1. Read `min(source.size(), 16 KiB)` bytes starting at offset `0`.
2. Parse the 127-byte header.
3. Validate magic, version, section fields, compression codes, tile type code, zoom fields,
   coordinate fields, and configured limits.
4. Validate that the compressed root directory is contained in the first 16 KiB.
5. Slice the compressed root directory bytes from the initial buffer.
6. Decompress the root directory using internal compression.
7. Decode and validate root directory entries.
8. Construct an immutable archive object with header, root directory, source, options, caches, and
   warnings.

Failure to find the complete root directory inside the first 16 KiB is
`InvalidRootDirectoryLocation`. The reader never issues extra root reads to rescue non-conforming
archives, because the first-16-KiB root constraint is central to PMTiles v3’s latency model.

### 9.2 Tile lookup

Tile lookup by Z/X/Y:

```kotlin
public suspend fun getTile(z: Int, x: Int, y: Int): ArchiveTile? {
    val tileId = TileIds.fromZxy(z, x, y)
    return getTileById(tileId)?.withCoord(z, x, y)
}
```

Tile lookup by TileID:

```text
findTileEntry(directory, target, depth):
  entry = predecessor(directory.entries, target)
  if entry == null:
    return null

  if entry.runLength > 0:
    if target < entry.tileId + entry.runLength:
      return entry
    else:
      return null

  if entry.runLength == 0:
    leaf = loadLeafDirectory(entry, depth + 1)
    return findTileEntry(leaf, target, depth + 1)
```

The actual implementation must check overflow in `entry.tileId + entry.runLength` and must enforce
`maxDirectoryDepth`.

### 9.3 Leaf directory loading

A leaf entry’s range is:

```text
absoluteOffset = header.leafDirectories.offset + entry.offset
length = entry.length
```

The reader fetches that exact compressed range, decompresses it with internal compression, decodes
it as a complete directory, validates it, stores it in the leaf-directory cache, and searches it.
Leaf directories are compressed individually, not as a combined section.

Nested leaf directories are discouraged by the PMTiles spec. The reader traverses them up to
`maxDirectoryDepth`, records `NestedLeafDirectory`, and fails with `LimitExceeded` when traversal
would exceed that depth.

### 9.4 Directory decoding requirements

| Feature                  | Reader behavior                                                                                                                               |
| ------------------------ | --------------------------------------------------------------------------------------------------------------------------------------------- |
| Whole-directory encoding | Decode from a complete decompressed byte array. No partial-entry streaming API is exposed.                                                    |
| Entry count              | Reject zero-entry directories in strict mode. Enforce `maxDirectoryEntries`.                                                                  |
| TileID deltas            | Reconstruct absolute TileIDs with overflow checks. Validate monotonic ordering.                                                               |
| Run lengths              | `0` means leaf; `>0` means tile run. Enforce operational `Int` range for public model.                                                        |
| Lengths                  | Require `> 0`. Enforce `maxDirectoryBytes` or `maxTileBytes` depending on entry type.                                                         |
| Offsets                  | Decode `0` as contiguous with previous entry only for index `> 0`; otherwise decode non-zero as `value - 1`. Validate underflow and overflow. |
| Relative bases           | Tile entries are relative to tile data section; leaf entries are relative to leaf directory section.                                          |
| Search                   | Use predecessor search because a run or leaf can cover target IDs beyond its starting TileID.                                                 |

### 9.5 Range-level APIs

The reader exposes range APIs independently from payload decoding.

| API                          | Returns                                                                                       | Use case                                                  |
| ---------------------------- | --------------------------------------------------------------------------------------------- | --------------------------------------------------------- |
| `getTileRange(z, x, y)`      | Absolute archive byte range, TileID, coordinate, tile type, compression, and directory depth. | Range serving, diagnostics, CDN-aware serving, custom IO. |
| `getTileCompressed(z, x, y)` | Compressed bytes exactly as stored.                                                           | Callers preserving PMTiles tile compression.              |
| `getTile(z, x, y)`           | Bytes according to configured read mode.                                                      | Application consumption and tests.                        |
| `containsTile(z, x, y)`      | Boolean without fetching tile payload.                                                        | Render planning, sparse coverage checks.                  |

### 9.6 Tile read modes

```kotlin
public enum class TileReadMode {
    CompressedBytes,
    DecompressedBytes
}
```

| Mode                | Behavior                                                                                                                           |
| ------------------- | ---------------------------------------------------------------------------------------------------------------------------------- |
| `CompressedBytes`   | Returns bytes exactly as stored. `wasDecompressed=false`. No tile codec required.                                                  |
| `DecompressedBytes` | Requires built-in support for the tile compression unless compression is `None`. `wasDecompressed=true` when decompression occurs. |

### 9.7 Validation modes

```kotlin
public enum class ValidationMode {
    Strict,
    Lenient
}
```

| Mode      | Behavior                                                                                                                               |
| --------- | -------------------------------------------------------------------------------------------------------------------------------------- |
| `Strict`  | Rejects spec violations during open and traversal. Intended for ingestion, tests, and validators.                                      |
| `Lenient` | Allows recoverable anomalies, records warnings, and fails only when safe operation is impossible. It never ignores unsafe byte ranges. |

---

## 10. Metadata

Metadata is internal-compressed UTF-8 JSON. Core exposes the complete raw JSON string through
`rawMetadataJson()` and parses typed metadata with `kotlinx.serialization.json`. Typed metadata
contains fields for the keys defined by the PMTiles v3 metadata section. Arbitrary custom keys
remain available through `rawMetadataJson()`; the core library does not model them.

```kotlin
public data class ArchiveMetadata(
    public val name: String?,
    public val description: String?,
    public val attribution: String?,
    public val type: TilesetKind?,
    public val version: String?,
    public val encoding: String?,
    public val vectorLayersJson: String?
)
```

### 10.1 Metadata rules

| Requirement          | Reader behavior                                                                                                                                             |
| -------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Valid JSON object    | Strict mode fails if metadata is not a JSON object. Lenient mode preserves raw JSON after successful UTF-8 decoding and records `InvalidMetadataRecovered`. |
| UTF-8                | Invalid UTF-8 fails.                                                                                                                                        |
| Custom keys          | Preserved in `rawMetadataJson()`. The core library does not expose custom-key lookup APIs.                                                                  |
| MVT `vector_layers`  | Strict metadata parsing requires it when tile type is MVT; lenient mode warns.                                                                              |
| Attribution          | Preserved verbatim.                                                                                                                                         |
| Encoding             | The PMTiles-defined `encoding` string is exposed as metadata. Core does not interpret its value.                                                            |
| PMTiles-defined keys | Lift `name`, `description`, `attribution`, `type`, `version`, `encoding`, and `vector_layers` when their JSON types match the PMTiles spec.                 |

The typed metadata model is intentionally shallow. `vector_layers` is exposed as raw JSON in
`vectorLayersJson` because its structure is TileJSON-specific and can contain nested implementation
details. If a PMTiles-defined key has the wrong JSON type, strict mode records `InvalidMetadata` and
fails; lenient mode leaves the typed field `null`, preserves the full raw metadata JSON, and records
`InvalidMetadataRecovered`.

---

## 11. Compression

PMTiles v3 has two compression concepts:

| Compression field    | Applies to                                     | Required for                                                                                        |
| -------------------- | ---------------------------------------------- | --------------------------------------------------------------------------------------------------- |
| Internal compression | Root directory, metadata, each leaf directory. | Opening, metadata loading, and directory traversal.                                                 |
| Tile compression     | All tile blobs.                                | Decompressed tile payload APIs only. Range and compressed-byte APIs do not need tile decompression. |

### 11.1 Built-in codecs

The initial implementation has no public codec registration API. Compression decoding is an
implementation detail selected from the PMTiles compression code and the decode purpose.

### 11.2 Codec policy

| Compression | Reader policy                                                                                                                                                                   |
| ----------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Unknown     | Preserve code in header models. Fail at `open` when used as internal compression. Fail only at tile decode time when used as tile compression.                                  |
| None        | Built-in on every supported target.                                                                                                                                             |
| gzip        | Built-in on JVM and Kotlin/Native targets for internal compression, metadata, and decompressed tile APIs. JavaScript and WASM actuals compile and throw `NotImplementedError`.  |
| brotli      | Enum value is supported, but brotli decoding is not implemented. Fail at `open` when used as internal compression. Fail only at tile decode time when used as tile compression. |
| zstd        | Enum value is supported, but zstd decoding is not implemented. Fail at `open` when used as internal compression. Fail only at tile decode time when used as tile compression.   |

The base library standardizes on gzip for JVM and Kotlin/Native targets. JVM uses
`java.util.zip.GZIPInputStream` over `ByteArrayInputStream`. Kotlin/Native targets use zlib through
Kotlin/Native C interop, specifically `inflateInit2` with `16 + MAX_WBITS` so the decoder accepts
gzip-wrapped deflate, followed by `inflate` until `Z_STREAM_END` and `inflateEnd` in all exit paths.
The implementation does not use Foundation’s Compression framework for PMTiles gzip decoding.
JavaScript and WASM gzip actuals compile and throw `NotImplementedError` when invoked. Brotli and
zstd remain valid PMTiles enum values, but supporting them would require additional target-specific
dependencies and is not needed for current Protomaps-generated archives.

JVM and Kotlin/Native gzip implementations decode into bounded chunks and check
`DecodeLimits.maxDecompressedBytes` before appending output. JVM `GZIPInputStream`
`IOException`/`ZipException`, zlib initialization failure, zlib stream errors, truncated input, and
output beyond the configured limit fail with `DecompressionFailed` or `LimitExceeded` as
appropriate.

### 11.3 Decompression limits

Every decode operation takes limits:

```kotlin
internal data class DecodeLimits(
    internal val maxCompressedBytes: Int,
    internal val maxDecompressedBytes: Int,
    internal val purpose: DecodePurpose
)

internal enum class DecodePurpose {
    RootDirectory,
    LeafDirectory,
    Metadata,
    Tile
}
```

The decoder must fail before allocating beyond limits. Compressed-bomb behavior is a security
boundary, not a performance optimization.

---

## 12. Caching, concurrency, and performance

### 12.1 Cache layers

| Layer            | Default                       | Key                                      | Invalidation                |
| ---------------- | ----------------------------- | ---------------------------------------- | --------------------------- |
| Header/root      | Always cached per archive.    | Archive instance.                        | Archive close.              |
| Metadata         | Lazy cached after first read. | Archive instance + metadata section.     | Archive close.              |
| Leaf directories | Enabled LRU.                  | Offset + length + internal compression.  | Archive close.              |
| In-flight reads  | Enabled de-duplication.       | Range + read mode + compression purpose. | Completion or cancellation. |

### 12.2 Performance

- Opening a normal archive costs one source read of at most 16 KiB.
- Neighboring tile requests often share leaf directories; cache leaf directories aggressively
  relative to tile payloads.
- `CompressedBytes` is the default `tileReadMode` so range-first callers do not need tile
  decompression support.

### 12.3 Concurrency

`PmTilesArchive` is safe for concurrent read operations. Internal mutable state is restricted to
caches, close state, and in-flight maps protected by a multiplatform lock or coroutine mutex. The
caller-provided `ByteRangeSource` must accept concurrent reads.

Cancellation propagates to the suspending `ByteRangeSource.read` call through normal coroutine
cancellation. The source implementation determines how storage-level cancellation works. The archive
discards bytes delivered after archive close or coroutine cancellation.

---

## 13. Kotlin API

The Kotlin API is the single public API. It is suspending, nullable, and export-aware.

### 13.1 Opening archives

```kotlin
public class PmTilesArchive private constructor(...) : AutoCloseable {
    public val header: ArchiveHeader
    public val tileType: TileType
    public val internalCompression: Compression
    public val tileCompression: Compression

    public suspend fun rawMetadataJson(): String
    public suspend fun metadata(): ArchiveMetadata

    public suspend fun getTile(z: Int, x: Int, y: Int): ArchiveTile?
    public suspend fun getTile(coord: TileCoord): ArchiveTile?
    public suspend fun getTileById(tileId: Long): ArchiveTile?

    public suspend fun getTileRange(z: Int, x: Int, y: Int): TileRange?
    public suspend fun getTileCompressed(z: Int, x: Int, y: Int): ArchiveTile?
    public suspend fun containsTile(z: Int, x: Int, y: Int): Boolean
    public val warningCount: Int
    @ObjCName(swiftName = "warning(at:)")
    public fun warningAt(index: Int): ArchiveWarning?
    @HiddenFromObjC
    public fun warnings(): List<ArchiveWarning>

    override public fun close()

    public companion object {
        @HiddenFromObjC
        public suspend fun open(
            source: ByteRangeSource,
            options: ArchiveOpenOptions = ArchiveOpenOptions.Default
        ): PmTilesArchive
    }
}
```

`warningCount` returns the number of archive-level warnings accumulated so far. `warningAt(index)`
returns the warning at `index`, or `null` when `index` is negative or greater than or equal to
`warningCount` at the time of the call. Warnings are append-only for the archive lifetime.
`warnings()` returns a snapshot list for Kotlin callers and is hidden from Objective-C and Swift.
Lenient metadata parsing and lazy leaf traversal can add warnings after `open`.

### 13.2 Open options

```kotlin
public data class ArchiveOpenOptions(
    public val validationMode: ValidationMode = ValidationMode.Strict,
    public val tileReadMode: TileReadMode = TileReadMode.CompressedBytes,
    public val limits: ArchiveLimits = ArchiveLimits.Default
) {
    public companion object {
        public val Default: ArchiveOpenOptions
        public val Lenient: ArchiveOpenOptions
    }
}
```

### 13.3 Kotlin usage

```kotlin
val archive = PmTilesArchive.open(source)

val metadata = archive.metadata()
val rawMetadataJson = archive.rawMetadataJson()
val range = archive.getTileRange(12, 654, 1583)
val tile = archive.getTile(12, 654, 1583)

if (tile != null && tile.tileType == TileType.Mvt) {
    // Pass tile.bytes to an MVT decoder library.
}
```

### 13.4 API constraints for Swift export

The Kotlin API uses data classes, nullable returns, `UInt`/`ULong`, and `suspend`. The common public
API is used by Kotlin callers and Swift/Apple consumers; Apple targets additionally expose the
`ArchiveTile.data` and `ByteRangeDataSource` conveniences defined in section 15. Internal parser,
cache, and codec helper types are not exported. The public API follows these rules:

- no public mutable state
- no overloaded hot-path methods in exported declarations
- no deep generic result wrappers
- no Kotlin `Result` in public API
- no Kotlin collection types in hot exported paths
- hide Kotlin collection accessors and common-only source entry points from Objective-C with
  `@HiddenFromObjC`
- stable names with `@JvmName` or `@ObjCName` on every exported declaration whose generated name
  differs from the Kotlin source name

---

## 14. JVM API

JVM support is Kotlin-first. The initial implementation does not provide JVM source adapters or a
dedicated pure-Java wrapper.

Java callers use the generated Kotlin/JVM APIs. The implementation does not make core design
decisions solely for pure-Java consumption.

---

## 15. Swift and Apple API

Apple consumers use the Kotlin/Native-exported Kotlin API. Kotlin suspend functions are exported to
Objective-C headers as completion-handler APIs, and Swift 5.5+ can call them as `async` functions
with the Kotlin/Native toolchain version targeted by this library. The initial implementation does
not provide a duplicate Apple wrapper. The initial implementation does not add a Swift toolchain,
SwiftPM package, XCTest target, `swiftc` compilation test, or generated-framework test.

### 15.1 Swift usage shape

```swift
let source: ByteRangeDataSource = ...
let archive = try await PmTilesArchive.open(source: source)
let header = archive.header
let metadata = try await archive.metadata()
let rawMetadataJson = try await archive.rawMetadataJson()
let warningCount = archive.warningCount
let firstWarning = archive.warning(at: 0)

if let tile = try await archive.getTile(z: 12, x: 654, y: 1583) {
    let bytes = tile.bytes
    let data: NSData = tile.data
    let type = tile.tileType
}
```

`ArchiveTile` is the common Kotlin data class defined in section 7.6. It is not an `expect`/`actual`
class. The common result payload property is `bytes`. Apple targets also expose `data` as a copying
extension property for Swift and Objective-C consumers.

The Apple source set defines:

```kotlin
import platform.Foundation.NSData

public val ArchiveTile.data: NSData
    get() = bytes.toNSData()
```

`data` returns a new immutable `NSData` containing the current `bytes` contents each time it is
accessed. It is an `appleMain` extension property, not a constructor property and not an
`expect`/`actual` specialization of `ArchiveTile`. `toNSData()` is an internal Apple-source-set
helper, not exported public API.

### 15.2 Apple export requirements

| Area        | Requirement                                                                                                                                                |
| ----------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Classes     | Export final classes and simple DTOs. Do not export inheritance-heavy models.                                                                              |
| Errors      | Annotate every exported Kotlin API that throws `PmTilesException` with `@Throws(PmTilesException::class)`.                                                 |
| Async       | Use exported Kotlin `suspend` functions. Completion handlers are the Objective-C header shape; Swift async calls are the Swift usage shape.                |
| Bytes       | Export `ArchiveTile.bytes` as the common Kotlin `ByteArray` payload. Apple targets also export `ArchiveTile.data: NSData` as a copying extension property. |
| Collections | Hide `warnings(): List<ArchiveWarning>` from Objective-C and Swift. Export `warningCount` and `warning(at:)` instead.                                      |
| Enums       | Export raw-code DTOs for compression and tile type. Export Kotlin `enum class` for validation mode, tile read mode, and options without unknown raw codes. |
| Sources     | Hide the common `open(ByteRangeSource, ...)` from Objective-C and Swift. Export `open(ByteRangeDataSource, ...)` for Apple callers.                        |
| Names       | Use explicit exported names for every exported declaration whose generated Objective-C or Swift name differs from the Kotlin source name.                  |

### 15.3 Apple byte range data source

The Apple source set defines a Foundation-friendly byte range source:

```kotlin
import platform.Foundation.NSData

public interface ByteRangeDataSource {
    public suspend fun size(): ULong
    public suspend fun read(offset: ULong, length: Int): NSData
}

public suspend fun PmTilesArchive.Companion.open(
    source: ByteRangeDataSource,
    options: ArchiveOpenOptions = ArchiveOpenOptions.Default
): PmTilesArchive
```

The Swift usage shape is:

```swift
public protocol ByteRangeDataSource {
    func size() async throws -> UInt64
    func read(offset: UInt64, length: Int32) async throws -> NSData
}
```

The Kotlin/Native Objective-C header exposes this provider as completion-handler methods; Swift uses
`async` methods. The Apple opener adapts `ByteRangeDataSource` to the common `ByteRangeSource` and
copies returned `NSData` into the Kotlin `ByteArray` required by `ByteRangeSource.read`.
`ByteRangeDataSource` has the same stability, concurrency, and exact-read contract as
`ByteRangeSource`: `read(offset:length:)` must return exactly `length` bytes. The adapter treats a
short or long `NSData` result as `SourceUnavailable`.

---

## 16. Errors, warnings, and limits

### 16.1 Error model

```kotlin
public class PmTilesException(
    public val code: PmTilesErrorCode,
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

public enum class PmTilesErrorCode {
    InvalidMagic,
    UnsupportedVersion,
    InvalidHeader,
    InvalidSectionLayout,
    InvalidRootDirectoryLocation,
    InvalidDirectory,
    InvalidVarint,
    InvalidTileCoordinate,
    UnsupportedCompression,
    DecompressionFailed,
    InvalidMetadata,
    RangeOutOfBounds,
    SourceChanged,
    SourceUnavailable,
    LimitExceeded,
    Closed,
    Cancelled,
    InternalError
}
```

Host-language mappings:

| Platform     | Mapping                                                                     |
| ------------ | --------------------------------------------------------------------------- |
| Kotlin/JVM   | Throw `PmTilesException`; missing tile is `null`.                           |
| Apple export | Kotlin/Native exports `PmTilesException` as `NSError`; missing tile is nil. |

### 16.2 Warning model

```kotlin
public data class ArchiveWarning(
    public val code: ArchiveWarningCode,
    public val message: String,
    public val context: String? = null
)

public enum class ArchiveWarningCode {
    UnknownTileType,
    UnknownCompressionCode,
    UnknownCount,
    NonCanonicalSectionOrder,
    MissingVectorLayers,
    InvalidMetadataRecovered,
    NestedLeafDirectory
}
```

Warnings are not a substitute for errors. Unsafe ranges, overflow, malformed directories, and
decompression failures must fail even in lenient mode.

### 16.3 Default limits

| Limit                           | Purpose                                     | Default guidance                                                                                  |
| ------------------------------- | ------------------------------------------- | ------------------------------------------------------------------------------------------------- |
| `maxInitialReadBytes`           | First read during open.                     | 16 KiB fixed by PMTiles root constraint.                                                          |
| `maxMetadataBytes`              | Prevent huge metadata allocation.           | Conservative, configurable.                                                                       |
| `maxDirectoryCompressedBytes`   | Bound compressed directory reads.           | Derived from section/header values and configured cap.                                            |
| `maxDirectoryDecompressedBytes` | Prevent directory decompression bombs.      | Configurable.                                                                                     |
| `maxDirectoryEntries`           | Prevent CPU/memory abuse.                   | Equal to `maxDirectoryDecompressedBytes / 17`, the minimum encoded bytes for one valid entry set. |
| `maxTileCompressedBytes`        | Bound tile allocation for compressed reads. | Configurable.                                                                                     |
| `maxTileDecompressedBytes`      | Bound tile decompression.                   | Configurable; unused when `tileReadMode=CompressedBytes`.                                         |
| `maxDirectoryDepth`             | Prevent pathological nested leaf traversal. | Small default, configurable.                                                                      |
| `maxVarintBytes`                | Reject unterminated/overflowing varints.    | Must be fixed and small enough for 64-bit values.                                                 |

---

## 17. Security and robustness requirements

PMTiles archives are treated as untrusted input. The library must defend against malformed or
malicious input.

| Threat                               | Required mitigation                                                                    |
| ------------------------------------ | -------------------------------------------------------------------------------------- |
| Header integer overflow              | Parse unsigned fields carefully; validate before converting to signed platform values. |
| Section overlap or impossible layout | Strict validation rejects invalid layouts; lenient mode never reads unsafe ranges.     |
| Root outside first 16 KiB            | Reject at open.                                                                        |
| Malformed varints                    | Reject too-long, unterminated, or overflowing varints.                                 |
| Directory bombs                      | Enforce compressed and decompressed directory limits plus entry count limits.          |
| Decompression bombs                  | Codec-level limits for metadata, directories, and tiles.                               |
| Recursive leaf loops                 | Enforce `maxDirectoryDepth` and track visited leaf ranges within one lookup.           |
| Huge tile reads                      | Enforce compressed and decompressed tile limits.                                       |
| Source mutation                      | Treat `SourceChanged` from the caller-provided source as a hard failure.               |
| Cancellation races                   | Ensure closed archives do not complete future reads with partial data.                 |
| Duplicate or unsorted entries        | Strict validation rejects malformed directories.                                       |

No checksum is defined by PMTiles v3. The library does not imply integrity beyond successful
structural validation and source consistency checks.

---

## 18. Conformance requirements

This section defines required validation coverage for the completed library.

### 18.1 Format conformance

The implementation must include tests or equivalent verification for:

- official PMTiles v3 fixture archives across vector, raster, and metadata cases
- header parsing for every field and raw-code value
- invalid magic/version/header length cases
- section offset/length arithmetic, including overflow and legal non-canonical section order
- root directory contained in first 16 KiB
- TileID examples from the PMTiles spec
- Z/X/Y ↔ TileID round trips, including random high-zoom samples
- directory decoding with contiguous offsets, explicit offsets, leaf entries, run-length entries,
  and deduplicated offsets
- zero-length directory entries, empty directories, unsorted TileIDs, overflowed run ranges,
  malformed offset encodings
- metadata JSON object validation, MVT `vector_layers`, unknown metadata keys, invalid UTF-8, and
  PMTiles-defined metadata keys
- `None` and gzip decompression for internal and tile payload purposes, plus unsupported
  unknown/brotli/zstd failure behavior

### 18.2 Source conformance

The archive reader must be tested with fake `ByteRangeSource` implementations covering:

- exact range reads
- short reads
- out-of-bounds ranges
- concurrent reads
- archive close during in-flight read
- source mutation or truncation
- Apple `ByteRangeDataSource` `NSData` results with exact, short, and long lengths

### 18.3 Interop conformance

Exported JVM APIs must be tested from Kotlin source. Apple interop must be tested through
Kotlin/Native Apple target compilation, Apple-source-set Kotlin tests, and API dumps:

| Surface              | Required checks                                                                                                                                                                                     |
| -------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| JVM Kotlin           | Kotlin source compilation, coroutine calls, exceptions, `AutoCloseable`, `UInt`/`ULong`, and custom `ByteRangeSource` implementation.                                                               |
| Apple Kotlin/Native  | Apple target compilation, `NSData` extension access, `ByteRangeDataSource` opener behavior, exact/short/long `NSData` reads, copy behavior, missing tiles, errors, and `UInt`/`ULong` declarations. |
| Apple export hygiene | API dumps include the public Apple declarations. Public source declarations include the `@HiddenFromObjC`, `@ObjCName`, and `@Throws(PmTilesException::class)` annotations required by this spec.   |

The implementation series does not add SwiftPM, XCTest, `swiftc`, or generated-framework tests.

---

## 19. References

1. [PMTiles Version 3 Specification](https://github.com/protomaps/PMTiles/blob/main/spec/v3/spec.md)
2. [PMTiles v3 Changelog](https://github.com/protomaps/PMTiles/blob/main/spec/v3/CHANGELOG.md)
3. [Protomaps PMTiles documentation](https://docs.protomaps.com/pmtiles/)
4. [Protomaps go-pmtiles](https://github.com/protomaps/go-pmtiles)
5. [TileJSON 3.0 `vector_layers`](https://github.com/mapbox/tilejson-spec/blob/22f5f91e643e8980ef2656674bef84c2869fbe76/3.0.0/README.md#33-vector_layers)
6. [Kotlin release process and release history](https://kotlinlang.org/docs/releases.html)
7. [Kotlin: Interoperability with Swift/Objective-C](https://kotlinlang.org/docs/native-objc-interop.html)
8. [Kotlin: Interoperability with Swift using Swift export](https://kotlinlang.org/docs/native-swift-export.html)
