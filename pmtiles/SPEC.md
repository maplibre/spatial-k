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
Callers provide a `ByteRangeSource`; future Ktor, kotlinx-io, and provider-specific integrations
belong in separate source modules.

The public API is a single Kotlin API designed to export cleanly to Swift/Apple through
Kotlin/Native interop:

| Audience      | Primary API style                                                                                                                                     |
| ------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------- |
| Kotlin        | `suspend` functions, immutable data classes, nullable missing tiles, caller-provided `ByteRangeSource`, pluggable compression codecs, and caches.     |
| JVM Kotlin    | Same Kotlin API. No JVM source adapters and no dedicated pure-Java wrapper are part of this specification.                                            |
| Swift / Apple | Kotlin/Native exported API: suspend functions as completion handlers and Swift async calls, simple DTOs, `UInt`/`ULong` values, and custom providers. |

PMTiles v2 and earlier formats are not supported by the core reader. Any old-format converter
belongs in a separate compatibility tool.

---

## 2. Normative references

The library must follow the official PMTiles v3 format definition. This document maps the format
into library behavior and APIs.

| Reference                                                                                                                                            | Role in this library spec                                                                                                                                                                          |
| ---------------------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| [PMTiles Version 3 Specification](https://github.com/protomaps/PMTiles/blob/main/spec/v3/spec.md)                                                    | Normative binary format definition: header, sections, directories, metadata, compression, TileIDs, and pseudocode.                                                                                 |
| [PMTiles v3 Changelog](https://github.com/protomaps/PMTiles/blob/main/spec/v3/CHANGELOG.md)                                                          | Normative change history for v3.x clarifications and enum additions. Current notable entries include AVIF, MLT, `terrarium`, MIME type, directory length clarifications, and nested-leaf guidance. |
| [Protomaps PMTiles documentation](https://docs.protomaps.com/pmtiles/)                                                                               | Conceptual background and practical PMTiles usage model.                                                                                                                                           |
| [Protomaps go-pmtiles](https://github.com/protomaps/go-pmtiles)                                                                                      | Compatibility target for archives produced by the primary Protomaps PMTiles tooling.                                                                                                               |
| [TileJSON 3.0 vector_layers](https://github.com/mapbox/tilejson-spec/blob/22f5f91e643e8980ef2656674bef84c2869fbe76/3.0.0/README.md#33-vector_layers) | Required metadata key when the PMTiles header tile type is MVT.                                                                                                                                    |
| [Kotlin releases](https://kotlinlang.org/docs/releases.html)                                                                                         | Kotlin release currency. This spec assumes the Kotlin 2.3.x-era feature set as of 2026-06-03.                                                                                                      |
| [Kotlin Swift/Objective-C interop](https://kotlinlang.org/docs/native-objc-interop.html)                                                             | Stable Apple interop baseline.                                                                                                                                                                     |
| [Kotlin Swift export](https://kotlinlang.org/docs/native-swift-export.html)                                                                          | Experimental direct Swift export constraints and future-facing API hygiene.                                                                                                                        |

---

## 3. PMTiles v3 coverage contract

The library is considered PMTiles v3 reader-complete when every official v3 read feature is
represented in the reader, validator, and public model. The following matrix is the library’s
coverage checklist, not a restatement of the spec.

| PMTiles v3 area                    | Library behavior                                                                                                                                                                                                                                   |
| ---------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Magic and version                  | Reader validates `PMTiles` magic and version `3`. Unsupported versions fail with a deterministic error.                                                                                                                                            |
| Fixed 127-byte header              | Reader parses all fields into an immutable `PmTilesHeader`. Unknown/future enum values are preserved in raw-code models.                                                                                                                           |
| Section offsets and lengths        | Reader validates section arithmetic, overflow, overlap policy, and configured operational limits. Reader supports legal non-canonical relocation except where the official spec restricts layout.                                                  |
| Root directory within first 16 KiB | Reader opens with an initial first-16-KiB range read and requires the complete compressed root directory to be available there.                                                                                                                    |
| JSON metadata                      | Reader exposes raw UTF-8 JSON and typed known fields.                                                                                                                                                                                              |
| Optional leaf directories          | Reader supports leaf traversal, caching, and configurable nested-leaf depth.                                                                                                                                                                       |
| Tile data section                  | Reader returns compressed ranges, compressed tile bytes, or PMTiles-decompressed tile bytes according to read mode.                                                                                                                                |
| Hilbert TileID scheme              | Common code implements Z/X/Y to TileID conversion and reverse conversion, including zoom-start offsets and overflow checks.                                                                                                                        |
| Directory entries                  | Common code supports TileID deltas, run lengths, lengths, offsets, leaf entries, tile entries, contiguous-offset shorthand, whole-directory encode/decode, and binary search by predecessor entry.                                                 |
| Varint encoding                    | Common code implements bounded unsigned varint reads with malformed, unterminated, and overflow detection.                                                                                                                                         |
| Internal compression               | Applies to root directory, metadata, and each leaf directory independently. Required for opening and directory traversal.                                                                                                                          |
| Tile compression                   | Applies uniformly to all tile payloads in the archive. Required only when callers request decompressed tile payloads. Range APIs work without tile decompression.                                                                                  |
| Compression codes                  | Supports `Unknown`, `None`, `gzip`, `brotli`, and `zstd` model values. Unknown values are preserved in raw header models; unregistered internal compression fails at open and unregistered tile compression fails when tile decoding is requested. |
| Tile type codes                    | Supports `Unknown/Other`, `MVT`, `PNG`, `JPEG`, `WebP`, `AVIF`, and `MLT`, plus unknown raw codes. Tile payloads remain opaque.                                                                                                                    |
| Clustered flag                     | Reader reports the flag. Strict full-archive validation verifies clustered constraints.                                                                                                                                                            |
| Counts                             | Header counts are exposed as nullable semantic values because PMTiles uses `0` for “unknown”. Raw unsigned values remain accessible for diagnostics.                                                                                               |
| Bounds and center                  | Header positions are decoded into lon/lat models, with strict validation of sane coordinate ranges.                                                                                                                                                |
| MVT metadata requirement           | Strict mode requires `vector_layers` when tile type is MVT. Lenient mode warns.                                                                                                                                                                    |
| `terrarium` metadata encoding      | Reported as metadata. Core does not decode elevation pixels.                                                                                                                                                                                       |

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
- Kotlin API that exports cleanly to Swift/Apple using Kotlin Multiplatform export support.
- Simple DTOs that work for Kotlin callers and Swift/Apple consumers without a separate rich API.

### Non-goals

- No PMTiles v1/v2 support in the core reader.
- No PMTiles writing. Writer behavior belongs in a separate future specification.
- No MVT, MLT, image, raster terrain, or vector-tile semantic decoding in core.
- No in-place PMTiles mutation.
- No built-in HTTP, filesystem, object-store, Blob/File, or Node source implementations.
- No JavaScript or TypeScript API in this specification.
- No map renderer integration in core.
- No package publishing, Gradle metadata, artifact-coordinate, build-system, or repository-layout
  specification.

---

## 5. Architecture

```text
+----------------------------------------------------------------------------------+
| Kotlin API exported to Swift/Apple                                                |
|                                                                                  |
| Kotlin/JVM: suspend API + caller-provided ByteRangeSource                         |
| Swift/Apple: suspend functions as completions / async calls                       |
+-------------------------------------+--------------------------------------------+
                                      |
                                      v
+----------------------------------------------------------------------------------+
| pmtiles-core commonMain                                                          |
|                                                                                  |
| PmTilesArchive                                                                   |
|   - Header parser and validator                                                   |
|   - Directory codec and binary search                                             |
|   - Hilbert TileID math                                                           |
|   - Metadata loader                                                               |
|   - Compression registry                                                          |
|   - Directory/tile cache                                                          |
|   - Reader orchestration                                                          |
|                                                                                  |
| ByteRangeSource abstraction                                                     |
+----------------------------------------------------------------------------------+
```

### 5.1 Core responsibilities

| Subsystem                   | Location             | Responsibility                                                                                                                                                   |
| --------------------------- | -------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Binary primitives           | `commonMain`         | Little-endian numeric reads/writes, unsigned 64-bit parsing policy, varints, bounds checks.                                                                      |
| Tile ID math                | `commonMain`         | Hilbert conversion, zoom-start offsets, coordinate validation, TileID-to-Z/X/Y reverse conversion.                                                               |
| Directories                 | `commonMain`         | Directory encode/decode, validation, predecessor binary search, run-length handling, leaf traversal.                                                             |
| Metadata                    | `commonMain`         | Load internal-compressed UTF-8 JSON; expose raw JSON and typed convenience fields.                                                                               |
| Compression API             | `commonMain`         | Codec registry, codec lookup, decode limits, read modes.                                                                                                         |
| Compression implementations | platform source sets | `None` and gzip are built-in on every supported target; brotli/zstd are available only through `CompressionRegistry` extension codecs.                           |
| Cache                       | `commonMain`         | Header/root memoization, lazy metadata, leaf-directory LRU, tile LRU enabled only when `PmTilesCache.tilePayloadCapacity > 0`, in-flight request de-duplication. |
| Host exports                | target source sets   | Export annotations and names for Swift/Apple; JVM remains Kotlin-first.                                                                                          |

### 5.2 Object lifecycle

`PmTilesArchive` owns parsed archive state and caches. `open(source)` treats the supplied
`ByteRangeSource` as caller-owned and does not close it. Closing an archive releases archive caches,
cancels archive-managed in-flight work, and leaves the source open. Closing is idempotent.

The archive object is immutable except for caches, request de-duplication maps, and close state.
Header, root directory, options, source identity, and codec registry are fixed after `open`.

---

## 6. API principles

| Principle             | Consequence                                                                                                                                |
| --------------------- | ------------------------------------------------------------------------------------------------------------------------------------------ |
| Opaque tile payloads  | Tiles are bytes plus tile type/compression metadata. Core never parses MVT, MLT, raster image formats, or terrain pixels.                  |
| Source agnostic       | All reads go through caller-provided byte ranges. Storage-specific behavior belongs to the caller’s `ByteRangeSource`.                     |
| Coroutine core        | Kotlin IO APIs are suspending. Kotlin/Native exports suspension into Swift/Apple’s async idiom.                                            |
| Range-first design    | `getTileRange` and `getTileCompressed` are first-class; not every caller wants decompressed bytes.                                         |
| Validation by mode    | Strict mode rejects spec violations; lenient mode surfaces warnings for recoverable anomalies; server mode favors range serving.           |
| Interop-safe DTOs     | Public APIs use simple DTOs, `UInt`/`ULong`, no deep generics, no Kotlin collections in hot paths, and no overloaded exported names.       |
| Explicit limits       | Metadata bytes, directory bytes, tile bytes, varint length, directory entries, recursion depth, and coalesced read sizes are configurable. |
| Future enum tolerance | Unknown compression/tile-type codes are preserved in raw header models. Operations that require decoding fail explicitly.                  |
| Deterministic errors  | All failures carry a stable error code suitable for Kotlin and Swift consumers.                                                            |

---

## 7. Core domain model

### 7.1 Byte ranges and sources

```kotlin
public data class ByteRange(
    public val offset: Long,
    public val length: Int
)

public interface ByteRangeSource {
    public val description: String?
    public suspend fun size(): ULong
    public suspend fun read(range: ByteRange): ByteArray
    public suspend fun close()
}
```

`offset` is absolute from the start of the archive. `length` is an `Int` because all supported
targets ultimately allocate byte arrays with platform-specific maximum sizes. PMTiles unsigned
64-bit offsets and lengths are parsed into `ULong` models. Any read whose offset cannot fit signed
`Long`, or whose byte allocation exceeds configured limits, fails with `LIMIT_EXCEEDED` or
`RANGE_OUT_OF_BOUNDS`.

The reader calls `size()` during open. The source must return a stable archive size for the lifetime
of the archive object.

### 7.2 Header

```kotlin
public data class PmTilesHeader(
    public val specVersion: Int,
    public val rootDirectory: ArchiveSection,
    public val metadata: ArchiveSection,
    public val leafDirectories: ArchiveSection,
    public val tileData: ArchiveSection,
    public val counts: PmTilesCounts,
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

public data class PmTilesCounts(
    public val addressedTiles: Long?,
    public val tileEntries: Long?,
    public val tileContents: Long?,
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
domain. `TileIds.fromZxy` rejects `z > 31` with `INVALID_TILE_COORDINATE`. `TileIds.toZxy` rejects
TileIDs outside the `z <= 31` range with `INVALID_TILE_COORDINATE`.

### 7.5 Directory entries

```kotlin
public data class DirectoryEntry(
    public val tileId: Long,
    public val offset: Long,
    public val length: Int,
    public val runLength: Int
) {
    public val isLeaf: Boolean get() = runLength == 0
    public val isTile: Boolean get() = runLength > 0
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
    public val z: Int?,
    public val x: Int?,
    public val y: Int?,
    public val archiveRange: ByteRange,
    public val compressedLength: Int,
    public val tileType: TileType,
    public val compression: Compression,
    public val directoryDepth: Int
)

public data class PmTile(
    public val tileId: Long,
    public val z: Int?,
    public val x: Int?,
    public val y: Int?,
    public val bytes: ByteArray,
    public val tileType: TileType,
    public val compression: Compression,
    public val wasDecompressed: Boolean,
    public val range: TileRange
)
```

Coordinates are nullable for APIs that start from raw TileID or for future archive-like operations
where reverse coordinate conversion is intentionally skipped.

---

## 8. Byte range sources

`ByteRangeSource` is the only IO dependency of the PMTiles core. A source implementation must return
exactly the requested bytes or throw a typed source error.

### 8.1 Source contract

| Requirement       | Behavior                                                                                                                   |
| ----------------- | -------------------------------------------------------------------------------------------------------------------------- |
| Addressing        | `offset` is absolute from the start of the archive. `length` is a non-negative byte count.                                 |
| Exact reads       | `read(range)` returns exactly `length` bytes or fails. Partial arrays are never returned.                                  |
| Zero-length reads | Allowed and return an empty array. PMTiles reader code uses zero-length reads only in tests and adapter probes.            |
| Concurrency       | Sources accept concurrent reads. Adapters for non-thread-safe APIs serialize internally before exposing `ByteRangeSource`. |
| Stability         | Sources represent a stable archive snapshot or fail with `SOURCE_CHANGED`.                                                 |
| Size              | `size()` returns the stable total archive size in bytes.                                                                   |
| Close             | `close()` is idempotent and releases source-owned resources. `PmTilesArchive.close()` does not call it.                    |
| Errors            | Source failures are wrapped or mapped to stable PMTiles error codes.                                                       |

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
8. Construct an immutable archive object with header, root directory, source, options, codec
   registry, caches, and warnings.

Failure to find the complete root directory inside the first 16 KiB is
`INVALID_ROOT_DIRECTORY_LOCATION`. The reader never issues extra root reads to rescue non-conforming
archives, because the first-16-KiB root constraint is central to PMTiles v3’s latency model.

### 9.2 Tile lookup

Tile lookup by Z/X/Y:

```kotlin
public suspend fun getTile(z: Int, x: Int, y: Int): PmTile? {
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
`maxDirectoryDepth`, records `NESTED_LEAF_DIRECTORY`, and fails with `LIMIT_EXCEEDED` when traversal
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

| API                          | Returns                                                                                          | Use case                                                  |
| ---------------------------- | ------------------------------------------------------------------------------------------------ | --------------------------------------------------------- |
| `getTileRange(z, x, y)`      | Absolute archive byte range, compressed length, TileID, tile type, compression, directory depth. | Range serving, diagnostics, CDN-aware serving, custom IO. |
| `getTileCompressed(z, x, y)` | Compressed bytes exactly as stored.                                                              | Tile servers preserving PMTiles tile compression.         |
| `getTile(z, x, y)`           | Bytes according to configured read mode.                                                         | Application consumption and tests.                        |
| `containsTile(z, x, y)`      | Boolean without fetching tile payload.                                                           | Render planning, sparse coverage checks.                  |

### 9.6 Tile read modes

```kotlin
public enum class TileReadMode {
    COMPRESSED_BYTES,
    DECOMPRESSED_BYTES
}
```

| Mode                 | Behavior                                                                                                                     |
| -------------------- | ---------------------------------------------------------------------------------------------------------------------------- |
| `COMPRESSED_BYTES`   | Returns bytes exactly as stored. `wasDecompressed=false`. No tile codec required.                                            |
| `DECOMPRESSED_BYTES` | Requires a registered tile compression codec unless compression is `None`. `wasDecompressed=true` when decompression occurs. |

### 9.7 Validation modes

```kotlin
public enum class ValidationMode {
    STRICT,
    LENIENT,
    SERVER
}
```

| Mode      | Behavior                                                                                                                                                                                                   |
| --------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `STRICT`  | Rejects spec violations during open and traversal. Intended for ingestion, tests, and validators.                                                                                                          |
| `LENIENT` | Allows recoverable anomalies, records warnings, and fails only when safe operation is impossible. It never ignores malformed byte lengths, overflow, or unsafe ranges.                                     |
| `SERVER`  | Strict about archive structure, defaults to compressed-range APIs, performs tile decompression only when the caller sets `tileReadMode=DECOMPRESSED_BYTES`, and tunes cache behavior for repeated lookups. |

---

## 10. Metadata

Metadata is internal-compressed UTF-8 JSON. Core exposes both raw JSON and a typed convenience
projection.

```kotlin
public data class PmTilesMetadata(
    public val json: String,
    public val name: String?,
    public val description: String?,
    public val attribution: String?,
    public val type: TilesetKind?,
    public val version: String?,
    public val encoding: String?,
    public val vectorLayersJson: String?,
    public val knownFields: PmTilesKnownMetadataFields,
    public val warnings: List<PmTilesWarning>
)
```

### 10.1 Metadata rules

| Requirement          | Reader behavior                                                                                                                                               |
| -------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Valid JSON object    | Strict mode fails if metadata is not a JSON object. Lenient mode preserves raw JSON after successful UTF-8 decoding and records `INVALID_METADATA_RECOVERED`. |
| UTF-8                | Invalid UTF-8 fails.                                                                                                                                          |
| Unknown keys         | Preserved in raw JSON.                                                                                                                                        |
| MVT `vector_layers`  | Strict reader mode requires it when tile type is MVT; lenient mode warns.                                                                                     |
| Attribution          | Preserved verbatim.                                                                                                                                           |
| `encoding=terrarium` | Reported as metadata.                                                                                                                                         |
| TileJSON fields      | Lift known values when types are compatible.                                                                                                                  |

The typed metadata model is intentionally shallow. Nested vector layer information remains raw JSON
in this library.

---

## 11. Compression

PMTiles v3 has two compression concepts:

| Compression field    | Applies to                                     | Required for                                                                                        |
| -------------------- | ---------------------------------------------- | --------------------------------------------------------------------------------------------------- |
| Internal compression | Root directory, metadata, each leaf directory. | Opening, metadata loading, and directory traversal.                                                 |
| Tile compression     | All tile blobs.                                | Decompressed tile payload APIs only. Range and compressed-byte APIs do not need tile decompression. |

### 11.1 Codec registry

```kotlin
public interface CompressionCodec {
    public val compression: Compression
    public fun decode(bytes: ByteArray, limits: DecodeLimits): ByteArray
}

public class CompressionRegistry private constructor(...) {
    public fun codecFor(compression: Compression): CompressionCodec?
    public fun requireCodec(compression: Compression): CompressionCodec
}
```

### 11.2 Codec policy

| Compression | Reader policy                                                                                                                                                               |
| ----------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Unknown     | Preserve code in header models. Fail at `open` when used as internal compression. Fail only at tile decode time when used as tile compression and no custom codec exists.   |
| None        | Built-in on every supported target.                                                                                                                                         |
| gzip        | Built-in on every supported target for internal compression, metadata, and decompressed tile APIs. This is required for compatibility with archives produced by go-pmtiles. |
| brotli      | Enum value is supported and custom codecs are registered through `CompressionRegistry`. The base library does not ship brotli support.                                      |
| zstd        | Enum value is supported and custom codecs are registered through `CompressionRegistry`. The base library does not ship zstd support.                                        |

The base library deliberately standardizes on gzip rather than making compression availability vary
by platform. JVM uses `java.util.zip`; Apple/native targets use platform zlib bindings. Brotli and
zstd remain valid PMTiles enum values, but supporting them in the base library would require
target-specific dependencies and is not needed for current Protomaps-generated archives.

### 11.3 Decompression limits

Every decode operation takes limits:

```kotlin
public data class DecodeLimits(
    public val maxCompressedBytes: Int,
    public val maxDecompressedBytes: Int,
    public val purpose: DecodePurpose
)

public enum class DecodePurpose {
    ROOT_DIRECTORY,
    LEAF_DIRECTORY,
    METADATA,
    TILE
}
```

The codec must fail before allocating beyond limits. Compressed-bomb behavior is a security
boundary, not a performance optimization.

---

## 12. Caching, concurrency, and performance

### 12.1 Cache layers

| Layer            | Default                       | Key                                                       | Invalidation                                    |
| ---------------- | ----------------------------- | --------------------------------------------------------- | ----------------------------------------------- |
| Header/root      | Always cached per archive.    | Archive instance.                                         | Archive close.                                  |
| Metadata         | Lazy cached after first read. | Archive instance + metadata section.                      | Archive close or explicit clear.                |
| Leaf directories | Enabled LRU.                  | Source identity + offset + length + internal compression. | Archive close or explicit clear.                |
| Tile payloads    | Disabled by default.          | Source identity + TileID + range + read mode.             | Archive close, memory pressure, explicit clear. |
| In-flight reads  | Enabled de-duplication.       | Range + read mode + compression purpose.                  | Completion or cancellation.                     |

### 12.2 Performance

- Opening a normal archive costs one source read of at most 16 KiB.
- Neighboring tile requests often share leaf directories; cache leaf directories aggressively
  relative to tile payloads.
- Server mode favors `getTileRange` and `getTileCompressed` by using `COMPRESSED_BYTES` as its
  default `tileReadMode`.

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
    public val header: PmTilesHeader
    public val tileType: TileType
    public val internalCompression: Compression
    public val tileCompression: Compression
    public val warnings: List<PmTilesWarning>

    public suspend fun metadataJson(): String
    public suspend fun metadata(): PmTilesMetadata

    public suspend fun getTile(z: Int, x: Int, y: Int): PmTile?
    public suspend fun getTile(coord: TileCoord): PmTile?
    public suspend fun getTileById(tileId: Long): PmTile?

    public suspend fun getTileRange(z: Int, x: Int, y: Int): TileRange?
    public suspend fun getTileCompressed(z: Int, x: Int, y: Int): PmTile?
    public suspend fun containsTile(z: Int, x: Int, y: Int): Boolean

    override public fun close()

    public companion object {
        public suspend fun open(
            source: ByteRangeSource,
            options: PmTilesOpenOptions = PmTilesOpenOptions.Default
        ): PmTilesArchive
    }
}
```

### 13.2 Open options

```kotlin
public data class PmTilesOpenOptions(
    public val validationMode: ValidationMode = ValidationMode.STRICT,
    public val tileReadMode: TileReadMode = TileReadMode.COMPRESSED_BYTES,
    public val codecRegistry: CompressionRegistry = CompressionRegistry.Default,
    public val cache: PmTilesCache = PmTilesCache.Default,
    public val limits: PmTilesLimits = PmTilesLimits.Default
) {
    public companion object {
        public val Default: PmTilesOpenOptions
        public val Server: PmTilesOpenOptions
        public val Lenient: PmTilesOpenOptions
    }
}
```

### 13.3 Kotlin usage

```kotlin
val archive = PmTilesArchive.open(
    source = source,
    options = PmTilesOpenOptions.Server
)

val metadata = archive.metadata()
val range = archive.getTileRange(12, 654, 1583)
val tile = archive.getTile(12, 654, 1583)

if (tile != null && tile.tileType == TileType.Mvt) {
    // Pass tile.bytes to an MVT decoder library.
}
```

### 13.4 API constraints for Swift export

The Kotlin API uses data classes, nullable returns, `UInt`/`ULong`, and `suspend`. The same public
API is used by Kotlin callers and Swift/Apple consumers. Internal parser, cache, and codec helper
types are not exported. The public API follows these rules:

- no public mutable state
- no overloaded hot-path methods in exported declarations
- no deep generic result wrappers
- no Kotlin `Result` in public API
- no Kotlin collection types in hot exported paths
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
not provide a duplicate Apple wrapper.

### 15.1 Swift usage shape

```swift
let archive = try await PmTilesArchive.open(source: source)
let header = archive.header
let metadata = try await archive.metadata()

if let tile = try await archive.getTile(z: 12, x: 654, y: 1583) {
    let data: Data = tile.data
    let type = tile.tileType
}
```

### 15.2 Apple export requirements

| Area        | Requirement                                                                                                                                                |
| ----------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Classes     | Export final classes and simple DTOs. Do not export inheritance-heavy models.                                                                              |
| Errors      | Annotate every exported Kotlin API that throws `PmTilesException` with `@Throws(PmTilesException::class)`.                                                 |
| Async       | Use exported Kotlin `suspend` functions. Completion handlers are the Objective-C header shape; Swift async calls are the Swift usage shape.                |
| Bytes       | Expose payloads in a form that can bridge to `Data`/`NSData`. Copy behavior must be documented.                                                            |
| Collections | Avoid nested Kotlin collections in exported types. Use arrays of simple DTOs or raw JSON strings.                                                          |
| Enums       | Export raw-code DTOs for compression and tile type. Export Kotlin `enum class` for validation mode, tile read mode, and options without unknown raw codes. |
| Sources     | Provide an exported custom callback/provider source interface. Do not provide URL or file URL factories in this specification.                             |
| Names       | Use explicit exported names for every exported declaration whose generated Objective-C or Swift name differs from the Kotlin source name.                  |

### 15.3 Swift custom source shape

```swift
public protocol PmTilesByteRangeProvider {
    func read(offset: Int64, length: Int32) async throws -> Data
    func size() async throws -> UInt64
    func close()
}
```

The Kotlin/Native Objective-C header exposes this provider as completion-handler methods; Swift uses
`async` methods.

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
    INVALID_MAGIC,
    UNSUPPORTED_VERSION,
    INVALID_HEADER,
    INVALID_SECTION_LAYOUT,
    INVALID_ROOT_DIRECTORY_LOCATION,
    INVALID_DIRECTORY,
    INVALID_VARINT,
    INVALID_TILE_COORDINATE,
    UNSUPPORTED_COMPRESSION,
    DECOMPRESSION_FAILED,
    INVALID_METADATA,
    RANGE_OUT_OF_BOUNDS,
    SOURCE_CHANGED,
    SOURCE_UNAVAILABLE,
    SOURCE_PROTOCOL_ERROR,
    SOURCE_PERMISSION_DENIED,
    LIMIT_EXCEEDED,
    CLOSED,
    CANCELLED,
    INTERNAL_ERROR
}
```

Host-language mappings:

| Platform   | Mapping                                                          |
| ---------- | ---------------------------------------------------------------- |
| Kotlin/JVM | Throw `PmTilesException`; missing tile is `null`.                |
| Swift      | `NSError`/Swift `Error` domain plus code; missing tile is `nil`. |

### 16.2 Warning model

```kotlin
public data class PmTilesWarning(
    public val code: PmTilesWarningCode,
    public val message: String,
    public val context: String? = null
)

public enum class PmTilesWarningCode {
    UNKNOWN_TILE_TYPE,
    UNKNOWN_COMPRESSION_CODE,
    UNKNOWN_COUNT,
    NON_CANONICAL_SECTION_ORDER,
    MISSING_VECTOR_LAYERS,
    INVALID_METADATA_RECOVERED,
    NESTED_LEAF_DIRECTORY
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
| `maxTileDecompressedBytes`      | Bound tile decompression.                   | Configurable; `SERVER` mode defaults to `COMPRESSED_BYTES`.                                       |
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
| Source mutation                      | Treat `SOURCE_CHANGED` from the caller-provided source as a hard failure.              |
| Cancellation races                   | Ensure closed archives do not complete future reads with partial data.                 |
| Duplicate or unsorted entries        | Strict validation rejects malformed directories.                                       |

No checksum is defined by PMTiles v3. The library does not imply integrity beyond successful
structural validation and source consistency checks.

---

## 18. Conformance requirements

This section defines required validation coverage for the completed library.

### 18.1 Format conformance

The implementation must include tests or equivalent verification for:

- official PMTiles v3 fixture archives across vector, raster, and terrain-like metadata cases
- header parsing for every field and raw-code value
- invalid magic/version/header length cases
- section offset/length arithmetic, including overflow and legal non-canonical section order
- root directory contained in first 16 KiB
- TileID examples from the PMTiles spec
- Z/X/Y ↔ TileID round trips, including random high-zoom samples
- directory encode/decode with contiguous offsets, explicit offsets, leaf entries, run-length
  entries, and deduplicated offsets
- zero-length directory entries, empty directories, unsorted TileIDs, overflowed run ranges,
  malformed offset encodings
- metadata JSON object validation, MVT `vector_layers`, unknown metadata keys, invalid UTF-8, and
  `terrarium` encoding
- each compression code with present and absent codecs

### 18.2 Source conformance

The archive reader must be tested with fake `ByteRangeSource` implementations covering:

- exact range reads
- short reads
- out-of-bounds ranges
- concurrent reads
- archive close during in-flight read
- source mutation or truncation

### 18.3 Interop conformance

Exported APIs must be tested from the host language, not only from Kotlin:

| Surface    | Required checks                                                                                                                          |
| ---------- | ---------------------------------------------------------------------------------------------------------------------------------------- |
| JVM Kotlin | Kotlin source compilation, coroutine calls, exceptions, `AutoCloseable`, `UInt`/`ULong`, and custom `ByteRangeSource` implementation.    |
| Swift      | Swift source compilation, async or completion usage, `Data` bridging, errors, missing tiles, `UInt`/`UInt64`, and custom provider shape. |

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
