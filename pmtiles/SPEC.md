# PMTiles v3 Kotlin Multiplatform Library Specification

This is a complete-state design specification for a Kotlin Multiplatform PMTiles v3 library. It
describes the completed library’s specified public surface and runtime behavior. It intentionally
does **not** duplicate the PMTiles binary specification; the official PMTiles v3 specification and
changelog remain normative.

## 1. Summary

The library implements PMTiles v3 as a **single-file tile archive/container**. It is responsible for
locating tiles, reading PMTiles headers, decoding directories, resolving Hilbert TileIDs, reading
metadata, applying PMTiles-level compression, writing valid archives, and exposing typed archive
information. It is **not** responsible for decoding MVT, MLT, PNG, JPEG, WebP, AVIF, terrain pixels,
or application-specific tile payloads.

The central design is:

```text
ByteRangeSource -> PmTilesArchive -> tile ranges / tile bytes / metadata / writer models
```

Most of the library lives in `commonMain`: binary decoding, unsigned/varint handling, Hilbert math,
directory encode/decode, metadata parsing, validation, caches, read orchestration, write planning,
and API models. Platform source sets supply built-in source adapters for platform consumers.
Separate Kotlin-oriented source modules bridge to Ktor and kotlinx-io so Kotlin users plug in their
preferred IO stack without forcing those dependencies into the core archive model.

The public API is Kotlin-first. Swift/Apple and JavaScript/TypeScript entry points use Kotlin
Multiplatform export mechanisms:

| Audience                | Primary API style                                                                                                                                  |
| ----------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------- |
| Kotlin                  | `suspend` functions, immutable data classes, nullable missing tiles, pluggable sources/codecs/caches.                                              |
| JVM                     | Kotlin API plus JVM source adapters for `Path`, `FileChannel`, and JDK HTTP. No dedicated pure-Java wrapper is part of the initial implementation. |
| Swift / Apple           | Kotlin/Native exported APIs: suspend functions as completion handlers and Swift async calls, with Apple source adapters.                           |
| JavaScript / TypeScript | `@JsExport` API: suspend functions as promises, typed arrays for bytes, `bigint` for large numeric values, and JS source adapters.                 |

PMTiles v2 and earlier formats are not supported by the core reader or writer. Any old-format
converter belongs in a separate compatibility tool.

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
| [MDN: HTTP range requests](https://developer.mozilla.org/en-US/docs/Web/HTTP/Guides/Range_requests)                                                  | HTTP source behavior reference.                                                                                                                                                                    |
| [Kotlin releases](https://kotlinlang.org/docs/releases.html)                                                                                         | Kotlin release currency. This spec assumes the Kotlin 2.3.x-era feature set as of 2026-06-03.                                                                                                      |
| [Kotlin Swift/Objective-C interop](https://kotlinlang.org/docs/native-objc-interop.html)                                                             | Stable Apple interop baseline.                                                                                                                                                                     |
| [Kotlin Swift export](https://kotlinlang.org/docs/native-swift-export.html)                                                                          | Experimental direct Swift export constraints and future-facing API hygiene.                                                                                                                        |
| [Kotlin JS interop](https://kotlinlang.org/docs/js-to-kotlin-interop.html)                                                                           | JS export constraints, `@JsExport`, generated TypeScript, and naming behavior.                                                                                                                     |
| [Kotlin 2.3.0 notes](https://kotlinlang.org/docs/whatsnew23.html)                                                                                    | JS suspend export and related JS interop improvements.                                                                                                                                             |
| [Kotlin 2.3.20 notes](https://kotlinlang.org/docs/whatsnew2320.html)                                                                                 | TypeScript implementation of exported Kotlin interfaces and related JS interop improvements.                                                                                                       |

---

## 3. PMTiles v3 coverage contract

The library is considered PMTiles v3 complete when every official v3 feature is represented in the
reader, writer, validator, and public model. The following matrix is the library’s coverage
checklist, not a restatement of the spec.

| PMTiles v3 area                    | Library behavior                                                                                                                                                                                                                                                                                             |
| ---------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| Magic and version                  | Reader validates `PMTiles` magic and version `3`. Writer emits only version `3`. Unsupported versions fail with a deterministic error.                                                                                                                                                                       |
| Fixed 127-byte header              | Reader parses all fields into an immutable `PmTilesHeader`. Writer emits a canonical 127-byte header. Unknown/future enum values are preserved where safe.                                                                                                                                                   |
| Section offsets and lengths        | Reader validates section arithmetic, overflow, overlap policy, and configured operational limits. Writer emits canonical section order: header, root directory, metadata, leaf directories, tile data. Reader still supports legal non-canonical relocation except where the official spec restricts layout. |
| Root directory within first 16 KiB | Reader opens with an initial first-16-KiB range read and requires the complete compressed root directory to be available there. Writer ensures the compressed root directory plus preceding bytes fit in the first 16 KiB.                                                                                   |
| JSON metadata                      | Reader exposes raw UTF-8 JSON and typed known fields. Writer emits a valid UTF-8 JSON object.                                                                                                                                                                                                                |
| Optional leaf directories          | Reader supports leaf traversal, caching, and configurable nested-leaf depth. Writer emits leaf directories when needed and orders them by starting TileID. Writer does not intentionally create nested leaf directories.                                                                                     |
| Tile data section                  | Reader returns compressed ranges, compressed tile bytes, or PMTiles-decompressed tile bytes according to read mode. Writer lays out tile blobs and directory offsets relative to the tile data section.                                                                                                      |
| Hilbert TileID scheme              | Common code implements Z/X/Y to TileID conversion and reverse conversion, including zoom-start offsets and overflow checks.                                                                                                                                                                                  |
| Directory entries                  | Common code supports TileID deltas, run lengths, lengths, offsets, leaf entries, tile entries, contiguous-offset shorthand, whole-directory encode/decode, and binary search by predecessor entry.                                                                                                           |
| Varint encoding                    | Common code implements bounded unsigned varint read/write with malformed, unterminated, and overflow detection.                                                                                                                                                                                              |
| Internal compression               | Applies to root directory, metadata, and each leaf directory independently. Required for opening and directory traversal.                                                                                                                                                                                    |
| Tile compression                   | Applies uniformly to all tile payloads in the archive. Required only when callers request decompressed tile payloads. Range APIs work without tile decompression.                                                                                                                                            |
| Compression enum                   | Supports `Unknown`, `None`, `gzip`, `brotli`, and `zstd` model values. Unknown values are preserved in raw header models; unregistered internal compression fails at open and unregistered tile compression fails when tile decoding is requested.                                                           |
| Tile type enum                     | Supports `Unknown/Other`, `MVT`, `PNG`, `JPEG`, `WebP`, `AVIF`, and `MLT`, plus unknown raw codes. Tile payloads remain opaque.                                                                                                                                                                              |
| Clustered flag                     | Reader reports the flag. Strict full-archive validation verifies clustered constraints. Writer sets the flag only when the emitted tile layout satisfies it.                                                                                                                                                 |
| Counts                             | Header counts are exposed as nullable semantic values because PMTiles uses `0` for “unknown”. Raw unsigned values remain accessible for diagnostics.                                                                                                                                                         |
| Bounds and center                  | Header positions are decoded into lon/lat models, with strict validation of sane coordinate ranges. Writer encodes bounds/center using PMTiles’ integer-scaled representation.                                                                                                                               |
| MVT metadata requirement           | Strict mode requires `vector_layers` when tile type is MVT. Lenient mode warns. Writer fails without it when tile type is MVT.                                                                                                                                                                               |
| `terrarium` metadata encoding      | Reported as metadata. Core does not decode elevation pixels.                                                                                                                                                                                                                                                 |
| Recommended MIME type              | Server helpers report `application/vnd.pmtiles` for full archive responses. Tile response helpers report tile-type-specific content types.                                                                                                                                                                   |

---

## 4. Goals and non-goals

### Goals

- PMTiles v3 read support covering header, root directory, metadata, leaf directories, tile data,
  TileIDs, compression, and validation.
- PMTiles v3 write support covering canonical layout, directory encoding, root-size fitting,
  metadata, clustering flag correctness, deduplication, run-length entries, and compression
  selection.
- Tile-payload agnosticism: core returns bytes and metadata; payload decoders live elsewhere.
- One byte-range abstraction for HTTP, files, memory, Blob/File, Node files, object stores,
  encrypted blobs, and custom sources.
- Fast HTTP-oriented reads: single first-16-KiB open read, leaf-directory cache, tile payload cache
  disabled by default, request coalescing, validator pinning.
- Safe behavior on untrusted archives: explicit limits, bounded decompression, overflow checks,
  deterministic errors.
- Kotlin-first API that exports cleanly to Swift, JavaScript, and TypeScript using Kotlin
  Multiplatform export support.
- Export-aware DTOs that avoid Kotlin-specific footguns such as unsigned types, deep generics,
  Kotlin collections in hot paths, and overloaded names that mangle poorly.

### Non-goals

- No PMTiles v1/v2 support in the core reader or writer.
- No MVT, MLT, image, raster terrain, or vector-tile semantic decoding in core.
- No in-place PMTiles mutation. Writers create new archives.
- No map renderer integration in core.
- No package publishing, Gradle metadata, artifact-coordinate, build-system, or repository-layout
  specification.
- No assumption that HTTP is the only storage backend.

---

## 5. Architecture

```text
+----------------------------------------------------------------------------------+
| Exported Kotlin API                                                              |
|                                                                                  |
| Kotlin/JVM: suspend API + JVM source adapters                                     |
| Swift/Apple: suspend functions as completions / async calls                       |
| JS/TS: @JsExport suspend functions as Promise + typed-array payloads              |
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
|   - Writer planning and directory encoding                                        |
|                                                                                  |
| ByteRangeSource / ByteSink abstractions                                           |
+------------------------+------------------------+--------------------------------+
                         |                        |
                         v                        v
+------------------------------------+   +-----------------------------------------+
| Built-in target adapters           |   | Kotlin source implementation modules    |
|                                    |   |                                         |
| JVM Path + JDK HttpClient          |   | Ktor ByteRangeSource                    |
| Apple Foundation file + URLSession |   | kotlinx-io ByteRangeSource/ByteSink     |
| JS fetch + Blob/File + Node fs     |   | future provider-specific sources        |
| common in-memory source            |   |                                         |
+------------------------------------+   +-----------------------------------------+
```

### 5.1 Core responsibilities

| Subsystem                   | Location                    | Responsibility                                                                                                                                                   |
| --------------------------- | --------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Binary primitives           | `commonMain`                | Little-endian numeric reads/writes, unsigned 64-bit parsing policy, varints, bounds checks.                                                                      |
| Tile ID math                | `commonMain`                | Hilbert conversion, zoom-start offsets, coordinate validation, TileID-to-Z/X/Y reverse conversion.                                                               |
| Directories                 | `commonMain`                | Directory encode/decode, validation, predecessor binary search, run-length handling, leaf traversal.                                                             |
| Metadata                    | `commonMain`                | Load internal-compressed UTF-8 JSON; expose raw JSON and typed convenience fields.                                                                               |
| Compression API             | `commonMain`                | Codec registry, codec lookup, decode/encode limits, read modes.                                                                                                  |
| Compression implementations | platform source sets        | `None` and gzip are built-in on every target; brotli/zstd are available only through `CompressionRegistry` extension codecs.                                     |
| Cache                       | `commonMain`                | Header/root memoization, lazy metadata, leaf-directory LRU, tile LRU enabled only when `PmTilesCache.tilePayloadCapacity > 0`, in-flight request de-duplication. |
| HTTP/file adapters          | target source sets          | Bridge JDK, Foundation, JS fetch/Blob/Node, and similar APIs into `ByteRangeSource`.                                                                             |
| Ktor/kotlinx-io adapters    | separate KMP source modules | Kotlin-consumer convenience without making Ktor or kotlinx-io part of the archive core.                                                                          |
| Host exports                | target source sets          | Export annotations, names, and adapters for Swift/Apple and JS/TS; JVM remains Kotlin-first.                                                                     |

### 5.2 Object lifecycle

`PmTilesArchive` owns parsed archive state and caches. `open(source)` treats the supplied
`ByteRangeSource` as caller-owned and does not close it. Library factory methods that create a
source for the caller, such as file-path and URL factories, mark that source as archive-owned.
Closing an archive closes only archive-owned sources and applies the cancellation rules in Section
13.3. Closing is idempotent.

The archive object is immutable except for caches, request de-duplication maps, and close state.
Header, root directory, options, source identity, and codec registry are fixed after `open`.

---

## 6. API principles

| Principle             | Consequence                                                                                                                                |
| --------------------- | ------------------------------------------------------------------------------------------------------------------------------------------ |
| Opaque tile payloads  | Tiles are bytes plus tile type/compression metadata. Core never parses MVT, MLT, raster image formats, or terrain pixels.                  |
| Source agnostic       | All reads go through byte ranges. HTTP, filesystem, memory, object-store, and custom sources are interchangeable.                          |
| Coroutine core        | Kotlin IO APIs are suspending. Kotlin/Native and Kotlin/JS export suspension into the host ecosystem’s async idiom.                        |
| Range-first design    | `getTileRange` and `getTileCompressed` are first-class; not every caller wants decompressed bytes.                                         |
| Validation by mode    | Strict mode rejects spec violations; lenient mode surfaces warnings for recoverable anomalies; server mode favors range serving.           |
| Interop-safe DTOs     | Exported APIs avoid unsigned public types, deep generics, Kotlin collections in hot paths, and overloaded names that mangle poorly.        |
| Explicit limits       | Metadata bytes, directory bytes, tile bytes, varint length, directory entries, recursion depth, and coalesced read sizes are configurable. |
| Future enum tolerance | Unknown compression/tile-type codes are preserved in raw header models. Operations that require decoding fail explicitly.                  |
| Deterministic errors  | All failures carry a stable error code suitable for Kotlin, Swift, and JS consumers.                                                       |

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
    public suspend fun size(): Long?
    public suspend fun read(range: ByteRange): ByteArray
    public suspend fun close()
}
```

`offset` is absolute from the start of the archive. `length` is an `Int` because all supported
targets ultimately allocate byte arrays with platform-specific maximum sizes. PMTiles unsigned
64-bit offsets and lengths are parsed internally, but any operation requiring a platform allocation
beyond configured limits fails with `LIMIT_EXCEEDED` or `RANGE_OUT_OF_BOUNDS`.

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
    public val offset: Long,
    public val length: Long
)

public data class PmTilesCounts(
    public val addressedTiles: Long?,
    public val tileEntries: Long?,
    public val tileContents: Long?,
    public val rawAddressedTiles: ULongString,
    public val rawTileEntries: ULongString,
    public val rawTileContents: ULongString
)
```

Counts use nullable semantic values because PMTiles uses `0` to mean “unknown”. Exported APIs expose
raw unsigned count values as decimal strings on every target. JavaScript exports also expose archive
offsets and lengths as `bigint` because those fields are signed `Long` values in the Kotlin model.

### 7.3 Enums and future raw values

```kotlin
public sealed interface Compression {
    public val code: Int
    public data object Unknown : Compression { override val code: Int = 0 }
    public data object None : Compression { override val code: Int = 1 }
    public data object Gzip : Compression { override val code: Int = 2 }
    public data object Brotli : Compression { override val code: Int = 3 }
    public data object Zstd : Compression { override val code: Int = 4 }
    public data class UnknownCode(override val code: Int) : Compression
}

public sealed interface TileType {
    public val code: Int
    public data object Unknown : TileType { override val code: Int = 0 }
    public data object Mvt : TileType { override val code: Int = 1 }
    public data object Png : TileType { override val code: Int = 2 }
    public data object Jpeg : TileType { override val code: Int = 3 }
    public data object Webp : TileType { override val code: Int = 4 }
    public data object Avif : TileType { override val code: Int = 5 }
    public data object Mlt : TileType { override val code: Int = 6 }
    public data class UnknownCode(override val code: Int) : TileType
}
```

The common Kotlin API uses sealed interfaces for extensible enum values. Exported Swift/JS surfaces
flatten these to raw-code DTOs with named constants, because raw-code preservation is required for
unknown compression and tile-type codes.

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

| Requirement       | Behavior                                                                                                                                           |
| ----------------- | -------------------------------------------------------------------------------------------------------------------------------------------------- |
| Addressing        | `offset` is absolute from the start of the archive. `length` is a non-negative byte count.                                                         |
| Exact reads       | `read(range)` returns exactly `length` bytes or fails. Partial arrays are never returned.                                                          |
| Zero-length reads | Allowed and return an empty array. PMTiles reader code uses zero-length reads only in tests and adapter probes.                                    |
| Concurrency       | Sources accept concurrent reads. Adapters for non-thread-safe APIs serialize internally before exposing `ByteRangeSource`.                         |
| Stability         | Sources represent a stable archive snapshot or fail with `SOURCE_CHANGED`. HTTP and file sources apply the target-specific rules below.            |
| Size              | `size()` returns total bytes when the adapter knows the size without reading the whole archive. Custom sources return `null` when size is unknown. |
| Close             | `close()` is idempotent, releases resources, and applies the cancellation rules in Section 13.3.                                                   |
| Errors            | Source failures are wrapped or mapped to stable PMTiles error codes.                                                                               |

### 8.2 Built-in platform source adapters

| Target                  | Core adapters                                              | Convenience entry points                                                            |
| ----------------------- | ---------------------------------------------------------- | ----------------------------------------------------------------------------------- |
| JVM                     | NIO `Path` / `FileChannel`; JDK `HttpClient` range source. | Kotlin factories for `Path`, `URI`, and custom `ByteRangeSource`.                   |
| Apple / Swift           | Foundation file URL source; `URLSession` range source.     | Exported Kotlin factories for file URL, URLSession-backed URL, and custom provider. |
| Browser JS              | `fetch` range source; `Blob` / `File` slice source.        | `@JsExport` factories for fetch, Blob/File, and custom JS source.                   |
| Node JS                 | Node file source; Node 20+ global `fetch` range source.    | `@JsExport` factories for Node file and fetch sources.                              |
| Common tests / embedded | In-memory `ByteArrayRangeSource`.                          | Useful for fixtures, small archives, and tests.                                     |

### 8.3 Kotlin source implementation modules

These are source implementations, not changes to archive semantics.

| Source module             | Purpose                                                                             | Design constraint                                                                                  |
| ------------------------- | ----------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------- |
| Ktor source               | Adapts caller-owned `io.ktor.client.HttpClient` into `ByteRangeSource`.             | Must reuse caller configuration: engine, auth, headers, proxy, retry, logging, TLS, and lifecycle. |
| kotlinx-io source/sink    | Adapts kotlinx-io filesystem/path abstractions into range sources and writer sinks. | Must not alter core archive behavior or require kotlinx-io for non-Kotlin consumers.               |
| Provider-specific sources | S3, GCS, Azure, signed URLs, encrypted blobs, cache-backed sources.                 | Implement normal `ByteRangeSource`; no special cases in archive reader.                            |

### 8.4 HTTP source requirements

HTTP sources read with `Range: bytes=start-end`.

Required behavior:

- Issue one initial `GET` range request for `bytes=0-16383` during `open`.
- Do not issue `HEAD` during `open`. Explicit capability-check and size-probe APIs use `HEAD`;
  normal tile reads do not.
- Accept `206 Partial Content` with the expected `Content-Range` and exact body length.
- Reject `200 OK` for range requests. A server that ignores `Range` is not a usable PMTiles range
  source.
- Map `416 Requested Range Not Satisfiable` to a range/source error.
- Capture `ETag` and `Last-Modified` response headers. If neither header is exposed on the first
  response, record `SOURCE_VALIDATOR_UNAVAILABLE` and continue without validator pinning.
- Subsequent range requests send `If-Range` with the captured `ETag` when present; otherwise they
  send `If-Range` with the captured `Last-Modified` value when present.
- If a later response exposes a different `ETag` or `Last-Modified`, fail with `SOURCE_CHANGED`.
- Allow caller-specified headers for auth, API keys, cookies, user-agent, and cache control.
- Browser JS fetch sources fail with `SOURCE_PROTOCOL_ERROR` when CORS hides `Content-Range` on a
  `206` response. Hidden `ETag` and `Last-Modified` headers are handled by the validator-unavailable
  rule above.

### 8.5 Filesystem source requirements

Filesystem sources must support random reads without changing archive state. Built-in filesystem
sources detect these invalidation cases:

- file missing at open
- file truncated before or during range read
- file modified after open according to the platform file identity, size, and modification-time
  fields captured at open
- permission errors
- close while reads are in flight

Built-in filesystem sources open a stable file handle at source construction. Before each range
read, they compare current file metadata with the metadata captured at open. A changed file
identity, size, or modification time fails with `SOURCE_CHANGED`. Platforms that cannot expose
modification time use file identity plus size and record `SOURCE_VALIDATOR_UNAVAILABLE`.

---

## 9. Reader behavior

### 9.1 Open sequence

`PmTilesArchive.open(source, options)` performs:

1. Read bytes `0..16383` as the initial range. If a source reports a size smaller than 16 KiB, read
   exactly the reported size instead.
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
would exceed that depth. The writer must not create nested leaf directories.

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
| `getTileRange(z, x, y)`      | Absolute archive byte range, compressed length, TileID, tile type, compression, directory depth. | HTTP proxying, diagnostics, CDN-aware serving, custom IO. |
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
| `STRICT`  | Rejects spec violations during open and traversal. Intended for ingestion, tests, validators, and writer round-trips.                                                                                      |
| `LENIENT` | Allows recoverable anomalies, records warnings, and fails only when safe operation is impossible. It never ignores malformed byte lengths, overflow, or unsafe ranges.                                     |
| `SERVER`  | Strict about archive structure, defaults to compressed-range APIs, performs tile decompression only when the caller sets `tileReadMode=DECOMPRESSED_BYTES`, and tunes cache behavior for repeated lookups. |

---

## 10. Writer behavior

Writers create new PMTiles v3 archives. They do not mutate existing archives in place.

### 10.1 Writer surface

```kotlin
public interface ByteSink {
    public suspend fun write(bytes: ByteArray)
    public suspend fun close()
}

public interface SeekableByteSink : ByteSink {
    public suspend fun position(): Long
    public suspend fun writeAt(offset: Long, bytes: ByteArray)
}

public class PmTilesWriter(
    public val sink: ByteSink,
    public val options: PmTilesWriteOptions
) {
    public suspend fun addTile(coord: TileCoord, bytes: ByteArray, contentHash: TileHash? = null)
    public suspend fun addTileById(tileId: Long, bytes: ByteArray, contentHash: TileHash? = null)
    public suspend fun setMetadataJson(json: String)
    public suspend fun finish(): PmTilesWriteSummary
}
```

The writer accepts sorted or unsorted input. Sorted input enables lower memory pressure, but the
completed archive must be equivalent regardless of input order.

### 10.2 Writer options

```kotlin
public data class PmTilesWriteOptions(
    public val tileType: TileType = TileType.Unknown,
    public val internalCompression: Compression = Compression.Gzip,
    public val tileCompression: Compression = Compression.None,
    public val inputTileCompression: InputTileCompression = InputTileCompression.Uncompressed,
    public val clustered: Boolean = true,
    public val deduplicate: Boolean = true,
    public val metadataValidation: MetadataValidation = MetadataValidation.STRICT,
    public val directoryStrategy: DirectoryStrategy = DirectoryStrategy.ADAPTIVE,
    public val limits: PmTilesLimits = PmTilesLimits.Default
)
```

| Option                 | Requirement                                                                                                                                                                                                                  |
| ---------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `tileType`             | Written to the PMTiles header. Does not imply payload validation.                                                                                                                                                            |
| `internalCompression`  | Applies to root directory, metadata, and every leaf directory. Codec must be registered for writing.                                                                                                                         |
| `tileCompression`      | Header value for all tile blobs. Mixed tile compression inside one archive is not supported.                                                                                                                                 |
| `inputTileCompression` | Prevents accidental double-compression when callers supply precompressed tile bytes.                                                                                                                                         |
| `clustered`            | Writer sets true only if emitted tile offsets satisfy PMTiles clustered semantics.                                                                                                                                           |
| `deduplicate`          | When `true`, identical tile contents share one tile data blob and consecutive TileIDs with identical contents become run-length entries. When `false`, each tile writes its own blob and every tile entry has `runLength=1`. |
| `metadataValidation`   | Writer always emits valid JSON object metadata. Strict writer mode also validates known metadata fields before writing. MVT archives require `vector_layers`.                                                                |
| `directoryStrategy`    | Chooses root/leaf split to satisfy the first-16-KiB root constraint.                                                                                                                                                         |

### 10.3 Writer layout

The writer emits canonical section order:

```text
Header -> Root Directory -> Metadata -> Leaf Directories -> Tile Data
```

Reader support for legal non-canonical layouts does not require the writer to produce them.

The writer must ensure:

- header is first and exactly 127 bytes
- compressed root directory is entirely within the first 16 KiB
- metadata is valid UTF-8 JSON after internal decompression
- leaf directories are individually compressed
- leaf directory order is ascending by starting TileID
- tile-entry offsets are relative to tile data section
- leaf-entry offsets are relative to leaf directory section
- tile data length counts actual stored tile blobs, not duplicate directory references
- header counts are exact. The writer does not emit `0` unknown counts.

### 10.4 Writer algorithm contract

A conforming writer behaves as if it performed these logical operations:

1. Validate tile coordinates or TileIDs.
2. Normalize all entries to TileID space.
3. Apply tile compression if requested and if inputs are declared uncompressed.
4. Hash and deduplicate tile contents if enabled.
5. Sort entries by TileID.
6. Reject duplicate TileIDs with `INVALID_DIRECTORY`.
7. When `deduplicate=true`, coalesce consecutive TileIDs into run-length entries only when they map
   to the same tile blob. When `deduplicate=false`, emit each tile entry with `runLength=1`.
8. Choose root and leaf directory split.
9. Encode directories using PMTiles v3 directory encoding.
10. Compress root, metadata, and each leaf directory with internal compression.
11. Iterate directory split until compressed root fits within the first 16 KiB.
12. Compute final section offsets and lengths.
13. Emit header and all sections.
14. Return a summary with counts, sizes, compression, warnings, and validation results.

### 10.5 Seekable and non-seekable sinks

A seekable sink reserves the header and backpatches it after final layout. A non-seekable sink
cannot emit a correct header until section offsets are known. Therefore, the writer uses exactly one
of these approaches:

| Sink type                                   | Required strategy                            |
| ------------------------------------------- | -------------------------------------------- |
| Seekable sink                               | Write placeholder header and backpatch.      |
| Non-seekable sink with complete input known | Compute layout before first write.           |
| Non-seekable sink with streaming input      | Fail with `UNSUPPORTED_SINK` before writing. |

The initial API does not provide caller-supplied temporary storage for non-seekable streaming
output.

---

## 11. Metadata

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

### 11.1 Metadata rules

| Requirement          | Reader behavior                                                                                                                                               | Writer behavior                                                                    |
| -------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------- |
| Valid JSON object    | Strict mode fails if metadata is not a JSON object. Lenient mode preserves raw JSON after successful UTF-8 decoding and records `INVALID_METADATA_RECOVERED`. | Always required before writing.                                                    |
| UTF-8                | Invalid UTF-8 fails.                                                                                                                                          | Writer encodes JSON as UTF-8.                                                      |
| Unknown keys         | Preserved in raw JSON.                                                                                                                                        | Preserved when caller supplies raw JSON.                                           |
| MVT `vector_layers`  | Strict reader mode requires it when tile type is MVT; lenient mode warns.                                                                                     | Always required when tile type is MVT.                                             |
| Attribution          | Preserved verbatim.                                                                                                                                           | Writer does not sanitize or interpret HTML.                                        |
| `encoding=terrarium` | Reported as metadata.                                                                                                                                         | Allowed. Core does not verify raster payload semantics.                            |
| TileJSON fields      | Lift known values when types are compatible.                                                                                                                  | Writer accepts raw JSON through `setMetadataJson` and validates it before writing. |

The typed metadata model is intentionally shallow. Nested vector layer information remains raw JSON
in this library.

---

## 12. Compression

PMTiles v3 has two compression concepts:

| Compression field    | Applies to                                     | Required for                                                                                        |
| -------------------- | ---------------------------------------------- | --------------------------------------------------------------------------------------------------- |
| Internal compression | Root directory, metadata, each leaf directory. | Opening, metadata loading, directory traversal, writer output.                                      |
| Tile compression     | All tile blobs.                                | Decompressed tile payload APIs only. Range and compressed-byte APIs do not need tile decompression. |

### 12.1 Codec registry

```kotlin
public interface CompressionCodec {
    public val compression: Compression
    public fun decode(bytes: ByteArray, limits: DecodeLimits): ByteArray
    public fun encode(bytes: ByteArray, options: EncodeOptions = EncodeOptions.Default): ByteArray
}

public class CompressionRegistry private constructor(...) {
    public fun codecFor(compression: Compression): CompressionCodec?
    public fun requireCodec(compression: Compression): CompressionCodec
}
```

### 12.2 Codec policy

| Compression | Reader policy                                                                                                                                                               | Writer policy                                                      |
| ----------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------ |
| Unknown     | Preserve code in header models. Fail at `open` when used as internal compression. Fail only at tile decode time when used as tile compression and no custom codec exists.   | Do not emit.                                                       |
| None        | Built-in on every target.                                                                                                                                                   | Built-in on every target.                                          |
| gzip        | Built-in on every target for internal compression, metadata, and decompressed tile APIs. This is required for compatibility with archives produced by Protomaps go-pmtiles. | Built-in on every target for internal compression and tile output. |
| brotli      | Enum value is supported and custom codecs are registered through `CompressionRegistry`. The base library does not ship brotli support.                                      | Available only through registered custom codecs.                   |
| zstd        | Enum value is supported and custom codecs are registered through `CompressionRegistry`. The base library does not ship zstd support.                                        | Available only through registered custom codecs.                   |

The base library deliberately standardizes on gzip rather than making compression availability vary
by platform. JVM uses `java.util.zip`; Apple/native targets use platform zlib bindings; JS and WASM
targets use a bundled gzip implementation and do not depend on `DecompressionStream` or
`CompressionStream` for correctness. Brotli and zstd remain valid PMTiles enum values, but
supporting them in the base library would require target-specific dependencies and is not needed for
current Protomaps-generated archives.

### 12.3 Decompression limits

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

## 13. Caching, concurrency, and performance

### 13.1 Cache layers

| Layer            | Default                       | Key                                                                   | Invalidation                                     |
| ---------------- | ----------------------------- | --------------------------------------------------------------------- | ------------------------------------------------ |
| Header/root      | Always cached per archive.    | Archive instance.                                                     | Archive close.                                   |
| Metadata         | Lazy cached after first read. | Archive instance + metadata section.                                  | Archive close or explicit clear.                 |
| Leaf directories | Enabled LRU.                  | Source identity + validator + offset + length + internal compression. | Archive close, validator change, explicit clear. |
| Tile payloads    | Disabled by default.          | Source identity + validator + TileID + range + read mode.             | Archive close, memory pressure, explicit clear.  |
| In-flight reads  | Enabled de-duplication.       | Range + read mode + compression purpose.                              | Completion or cancellation.                      |

### 13.2 HTTP performance

- Opening a normal archive costs one 16 KiB range request.
- Neighboring tile requests often share leaf directories; cache leaf directories aggressively
  relative to tile payloads.
- Range coalescing is enabled in `SERVER` mode and disabled in `STRICT` and `LENIENT` modes. When
  enabled, it merges near-adjacent reads whose merged range is within `maxCoalescedReadBytes`.
- Coalescing must not change observable bytes or error behavior.
- Server mode favors `getTileRange` and `getTileCompressed` by using `COMPRESSED_BYTES` as its
  default `tileReadMode`.
- Built-in source adapters expose metrics: request count, bytes fetched, cache hits, validator
  changes, retries, and coalesced reads.

### 13.3 Concurrency

`PmTilesArchive` is safe for concurrent read operations. Internal mutable state is restricted to
caches, close state, and in-flight maps protected by a multiplatform lock or coroutine mutex. Source
adapters declare concurrency capability; non-concurrent sources are wrapped in a serializing
adapter.

Cancellation propagates from Kotlin coroutines to platform requests according to this table:

| Target           | Cancellation behavior                                                                                  |
| ---------------- | ------------------------------------------------------------------------------------------------------ |
| JVM HTTP         | Cancel the underlying `CompletableFuture` and discard bytes delivered after cancellation.              |
| Apple URLSession | Cancel the associated `URLSessionTask` and discard bytes delivered after cancellation.                 |
| JS fetch         | Create an `AbortController` for each request, abort it on coroutine cancellation, and reject the read. |
| File IO          | Do not preempt an OS read that has already started; discard its result after cancellation or close.    |

---

## 14. Kotlin API

The Kotlin API is the canonical, richest API. It is suspending, nullable, and model-heavy.

### 14.1 Opening archives

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

### 14.2 Open options

```kotlin
public data class PmTilesOpenOptions(
    public val validationMode: ValidationMode = ValidationMode.STRICT,
    public val tileReadMode: TileReadMode = TileReadMode.COMPRESSED_BYTES,
    public val codecRegistry: CompressionRegistry = CompressionRegistry.Default,
    public val cache: PmTilesCache = PmTilesCache.Default,
    public val limits: PmTilesLimits = PmTilesLimits.Default,
    public val sourceConsistency: SourceConsistency = SourceConsistency.VALIDATE
) {
    public companion object {
        public val Default: PmTilesOpenOptions
        public val Server: PmTilesOpenOptions
        public val Lenient: PmTilesOpenOptions
    }
}
```

`SourceConsistency.VALIDATE` applies the HTTP and filesystem validator rules in Section 8.
`SourceConsistency.TRUST_SOURCE` disables validator checks after open and is available only for
caller-provided sources that document immutable snapshot behavior.

### 14.3 Kotlin usage

```kotlin
val source = KtorByteRangeSource(client, url)
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

### 14.4 Kotlin API constraints for export hygiene

The Kotlin-first API uses data classes, sealed interfaces, nullable returns, and `suspend`. The
Swift and JS exported subset consists of archive open/close APIs, tile lookup APIs, range APIs,
metadata APIs, source factories, source provider interfaces, error DTOs, warning DTOs, and the DTOs
needed by those APIs. Internal parser, writer-planning, cache, and codec helper types are not
exported. The exported subset follows these rules:

- no public mutable state
- no overloaded hot-path methods in exported declarations
- no public `ULong`/`UInt` in JS-exported declarations; PMTiles `uint64` diagnostics are decimal
  strings on every target
- no deep generic result wrappers
- no Kotlin `Result` in public API
- no Kotlin collection types in hot exported paths
- stable names with `@JvmName`, `@ObjCName`, or `@JsName` on every exported declaration whose
  generated name differs from the Kotlin source name

---

## 15. JVM API

JVM support is Kotlin-first. The library provides JVM source adapters for `Path`, `FileChannel`, and
JDK HTTP. The initial implementation does not provide a dedicated pure-Java wrapper.

Java callers use the generated Kotlin/JVM APIs. Java-specific conveniences such as
`CompletionStage`, Java builders, and `Optional` wrappers are outside this specification. The
implementation does not make core design decisions solely for pure-Java consumption.

---

## 16. Swift and Apple API

Apple consumers use the Kotlin/Native-exported Kotlin API. Kotlin suspend functions are exported to
Objective-C headers as completion-handler APIs, and Swift 5.5+ can call them as `async` functions
with the Kotlin/Native toolchain version targeted by this library. The initial implementation does
not provide a duplicate Apple wrapper.

### 16.1 Swift usage shape

```swift
let archive = try await PmTilesArchive.open(url: pmtilesURL)
let header = archive.header
let metadata = try await archive.metadata()

if let tile = try await archive.getTile(z: 12, x: 654, y: 1583) {
    let data: Data = tile.data
    let type = tile.tileType
}
```

### 16.2 Apple export requirements

| Area        | Requirement                                                                                                                                                        |
| ----------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| Classes     | Export final classes and simple DTOs. Do not export inheritance-heavy models.                                                                                      |
| Errors      | Annotate every exported Kotlin API that throws `PmTilesException` with `@Throws(PmTilesException::class)`.                                                         |
| Async       | Use exported Kotlin `suspend` functions. Completion handlers are the Objective-C header shape; Swift async calls are the Swift usage shape.                        |
| Bytes       | Expose payloads in a form that can bridge to `Data`/`NSData`. Copy behavior must be documented.                                                                    |
| Collections | Avoid nested Kotlin collections in exported types. Use arrays of simple DTOs or raw JSON strings.                                                                  |
| Enums       | Export simple raw-code DTOs for compression and tile type. Export Kotlin `enum class` for validation mode, tile read mode, and options without unknown raw codes.  |
| Sources     | Provide exported factories for URL, file URL, and custom callback/provider sources. Do not require Swift consumers to implement internal source plumbing directly. |
| Names       | Use explicit exported names for every exported declaration whose generated Objective-C or Swift name differs from the Kotlin source name.                          |

### 16.3 Swift custom source shape

```swift
public protocol PmTilesByteRangeProvider {
    func read(offset: Int64, length: Int32) async throws -> Data
    func size() async throws -> Int64?
    func close()
}
```

The Kotlin/Native Objective-C header exposes this provider as completion-handler methods; Swift uses
`async` methods.

---

## 17. JavaScript and TypeScript API

JS/TS uses the `@JsExport` surface from the Kotlin API, not a hand-written duplicate wrapper. Kotlin
2.3.x adds experimental direct export of suspend functions and TypeScript-side implementation of
exported Kotlin interfaces. The library enables `-Xenable-suspend-function-exporting`,
`-Xes-long-as-bigint`, `-XXLanguage:+JsAllowLongInExportedDeclarations`, and the interface-export
feature required by the targeted Kotlin version.

### 17.1 TypeScript usage shape

```ts
import { PmTiles } from "@example/pmtiles";

const archive = await PmTiles.openFetch("https://example.com/world.pmtiles");
const metadata = await archive.metadataJson();
const tile = await archive.getTile(12, 654, 1583);

if (tile) {
  const bytes: Uint8Array = tile.bytes;
  const type = tile.tileType; // "mvt", "png", "mlt", etc.
}
```

### 17.2 JS/TS export requirements

| Area          | Requirement                                                                                                                                                                                                                        |
| ------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Export        | Use `@JsExport` on archive APIs, source factories, source provider interfaces, error/warning DTOs, metadata DTOs, range/tile DTOs, and option DTOs. Do not export internal helpers.                                                |
| Async         | Export suspend functions so JS receives promises when `-Xenable-suspend-function-exporting` is enabled.                                                                                                                            |
| Bytes         | Present `Uint8Array` ergonomics even if Kotlin `ByteArray` maps through signed `Int8Array`.                                                                                                                                        |
| Large numbers | Use `bigint` for offsets, lengths that can exceed safe JS `number`, and raw counts. Enable `-Xes-long-as-bigint` and `-XXLanguage:+JsAllowLongInExportedDeclarations`. Use `number` only for bounded lengths and tile coordinates. |
| Sources       | Provide fetch, Blob/File, and Node file source implementations. Expose the TS-implementable `JsByteRangeSource` interface.                                                                                                         |
| Names         | Use `@JsName` to avoid overload/name-mangling leakage. Generated `.d.ts` quality is part of API quality.                                                                                                                           |
| Errors        | Reject promises with an `Error` carrying `code`, `message`, and optionally `cause`.                                                                                                                                                |

### 17.3 JS custom source

```ts
export interface JsByteRangeSource {
  read(offset: bigint, length: number): Promise<Uint8Array>;
  size?(): Promise<bigint | null>;
  close?(): void | Promise<void>;
}
```

### 17.4 JS export sketch

```ts
export class PmTilesArchive {
  readonly header: PmTilesHeader;

  metadataJson(): Promise<string>;
  metadata(): Promise<PmTilesMetadata>;

  getTile(z: number, x: number, y: number): Promise<PmTile | null>;
  getTileRange(z: number, x: number, y: number): Promise<PmTileRange | null>;
  getCompressedTile(z: number, x: number, y: number): Promise<PmTile | null>;

  close(): void;
}
```

Browser fetch sources must account for CORS exposure of range headers. Node file sources must avoid
reading the full archive into memory.

---

## 18. Errors, warnings, and limits

### 18.1 Error model

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
    UNSUPPORTED_SINK,
    CLOSED,
    CANCELLED,
    INTERNAL_ERROR
}
```

Host-language mappings:

| Platform   | Mapping                                                                      |
| ---------- | ---------------------------------------------------------------------------- |
| Kotlin/JVM | Throw `PmTilesException`; missing tile is `null`.                            |
| Swift      | `NSError`/Swift `Error` domain plus code; missing tile is `nil`.             |
| JS/TS      | Promise rejects with `PmTilesError` carrying `code`; missing tile is `null`. |

### 18.2 Warning model

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
    NESTED_LEAF_DIRECTORY,
    SOURCE_VALIDATOR_UNAVAILABLE
}
```

Warnings are not a substitute for errors. Unsafe ranges, overflow, malformed directories, and
decompression failures must fail even in lenient mode.

### 18.3 Default limits

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
| `maxCoalescedReadBytes`         | Bound HTTP range coalescing.                | Configurable by network/cache profile.                                                            |
| `maxVarintBytes`                | Reject unterminated/overflowing varints.    | Must be fixed and small enough for 64-bit values.                                                 |

---

## 19. Security and robustness requirements

PMTiles archives are treated as untrusted input. The library must defend against malformed or
malicious input.

| Threat                               | Required mitigation                                                                       |
| ------------------------------------ | ----------------------------------------------------------------------------------------- |
| Header integer overflow              | Parse unsigned fields carefully; validate before converting to signed platform values.    |
| Section overlap or impossible layout | Strict validation rejects invalid layouts; lenient mode never reads unsafe ranges.        |
| Root outside first 16 KiB            | Reject at open.                                                                           |
| Malformed varints                    | Reject too-long, unterminated, or overflowing varints.                                    |
| Directory bombs                      | Enforce compressed and decompressed directory limits plus entry count limits.             |
| Decompression bombs                  | Codec-level limits for metadata, directories, and tiles.                                  |
| Recursive leaf loops                 | Enforce `maxDirectoryDepth` and track visited leaf ranges within one lookup.              |
| Huge tile reads                      | Enforce compressed and decompressed tile limits.                                          |
| Source mutation                      | Apply the HTTP and filesystem consistency rules in Section 8; fail with `SOURCE_CHANGED`. |
| HTTP protocol surprises              | Validate status, content range, content length, and exact bytes.                          |
| Cancellation races                   | Ensure closed archives do not complete future reads with partial data.                    |
| Duplicate or unsorted entries        | Strict validation rejects; writer sorts unsorted input and rejects duplicate TileIDs.     |

No checksum is defined by PMTiles v3. The library does not imply integrity beyond successful
structural validation and source consistency checks.

---

## 20. Conformance requirements

This section defines required validation coverage for the completed library.

### 20.1 Format conformance

The implementation must include tests or equivalent verification for:

- official PMTiles v3 fixture archives across vector, raster, and terrain-like metadata cases
- header parsing for every field and enum
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
- each compression enum with present and absent codecs

### 20.2 Source conformance

Source adapters must be tested against:

- exact range reads
- short reads
- out-of-bounds ranges
- concurrent reads
- close during in-flight read
- source mutation or truncation
- HTTP `206`, unexpected `200`, `416`, missing/incorrect `Content-Range`, redirects, validators, and
  auth headers
- browser CORS header exposure behavior where applicable
- JS Blob/File slicing and Node file range reads
- Apple file URL and URLSession range reads
- JVM Path and JDK HTTP range reads

### 20.3 Interop conformance

Exported APIs must be tested from the host language, not only from Kotlin:

| Surface    | Required checks                                                                                                                                      |
| ---------- | ---------------------------------------------------------------------------------------------------------------------------------------------------- |
| JVM Kotlin | Kotlin source compilation, JVM source adapters, coroutine calls, exceptions, `AutoCloseable`, and custom `ByteRangeSource` implementation.           |
| Swift      | Swift source compilation, async or completion usage, `Data` bridging, errors, missing tiles, URL/file factories, and custom provider shape.          |
| JS/TS      | Generated `.d.ts`, Promise APIs from suspend exports, `Uint8Array` payloads, `bigint` offsets, fetch source, Blob/File source, and custom TS source. |

### 20.4 Writer conformance

Writer output must round-trip through the strict reader and through at least one independent PMTiles
v3 implementation or fixture validator. Required cases:

- rejection of empty archives and output of a minimal one-tile archive
- single tile
- dense adjacent tile runs
- sparse tile set requiring leaf directories
- root directory near first-16-KiB limit
- duplicate tile content with deduplication
- clustered and non-clustered output
- MVT metadata with `vector_layers`
- raster tile types
- unknown tile type
- `None` and gzip internal compression
- precompressed tile input without double-compression

---

## 21. References

1. [PMTiles Version 3 Specification](https://github.com/protomaps/PMTiles/blob/main/spec/v3/spec.md)
2. [PMTiles v3 Changelog](https://github.com/protomaps/PMTiles/blob/main/spec/v3/CHANGELOG.md)
3. [Protomaps PMTiles documentation](https://docs.protomaps.com/pmtiles/)
4. [Protomaps go-pmtiles](https://github.com/protomaps/go-pmtiles)
5. [TileJSON 3.0 `vector_layers`](https://github.com/mapbox/tilejson-spec/blob/22f5f91e643e8980ef2656674bef84c2869fbe76/3.0.0/README.md#33-vector_layers)
6. [MDN: HTTP range requests](https://developer.mozilla.org/en-US/docs/Web/HTTP/Guides/Range_requests)
7. [Kotlin release process and release history](https://kotlinlang.org/docs/releases.html)
8. [Kotlin: Interoperability with Swift/Objective-C](https://kotlinlang.org/docs/native-objc-interop.html)
9. [Kotlin: Interoperability with Swift using Swift export](https://kotlinlang.org/docs/native-swift-export.html)
10. [Kotlin: Use Kotlin code from JavaScript](https://kotlinlang.org/docs/js-to-kotlin-interop.html)
11. [Kotlin: What’s new in Kotlin 2.3.0](https://kotlinlang.org/docs/whatsnew23.html)
12. [Kotlin: What’s new in Kotlin 2.3.20](https://kotlinlang.org/docs/whatsnew2320.html)
