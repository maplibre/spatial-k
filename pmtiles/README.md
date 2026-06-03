# PMTiles

`pmtiles` is a Kotlin Multiplatform PMTiles v3 reader. It reads a caller-provided `ByteRangeSource`,
parses the archive header and directories, exposes raw and typed metadata, and returns tile byte
ranges or tile bytes. Tile payloads stay opaque: this module does not decode MVT, MLT, PNG, JPEG,
WebP, AVIF, or terrain payload content.

## Source Contract

The reader has one IO dependency:

```kotlin
interface ByteRangeSource {
    suspend fun size(): ULong
    suspend fun read(range: ByteRange): ByteArray
}
```

`size()` returns the stable archive size for the lifetime of the opened archive. `read(range)`
returns exactly `range.length` bytes from the absolute archive offset or throws. Sources accept
concurrent reads. The archive does not own the source: `PmTilesArchive.close()` releases archive
caches and in-flight work, and it leaves the source open.

The module does not include HTTP, filesystem, object-store, browser `Blob`/`File`, or Node source
implementations. Callers provide those adapters.

## Kotlin Usage

```kotlin
val archive = PmTilesArchive.open(source)
try {
    val header = archive.header
    val metadata = archive.metadata()
    val rawMetadataJson = archive.rawMetadataJson()
    val range = archive.getTileRange(12, 654, 1583)

    val tile = archive.getTile(12, 654, 1583)
    if (tile != null && tile.tileType == TileType.Mvt) {
        // Pass tile.bytes to an MVT decoder.
    }

    repeat(archive.warningCount) { index ->
        val warning = archive.warningAt(index)
    }
} finally {
    archive.close()
}
```

`getTileRange` and `getTileCompressed` work without tile decompression. `getTile` follows
`ArchiveOpenOptions.tileReadMode`, which defaults to `TileReadMode.CompressedBytes`.

## Apple Usage Shape

Apple targets export the same Kotlin API through Kotlin/Native. Suspend functions are exported as
Objective-C completion-handler APIs and are callable from Swift as async functions. Apple targets
also expose a Foundation-friendly data source and an `NSData` tile convenience:

```swift
let source: ByteRangeDataSource = ...
let archive = try await PmTilesArchive.open(source: source)
let metadata = try await archive.metadata()
let rawMetadataJson = try await archive.rawMetadataJson()

if let tile = try await archive.getTile(z: 12, x: 654, y: 1583) {
    let bytes = tile.bytes
    let data: NSData = tile.data
    let type = tile.tileType
}
```

`ArchiveTile.data` is an Apple-source-set extension property. It copies the common Kotlin
`ByteArray` payload into a new immutable `NSData` each time it is accessed. `ArchiveTile` itself is
the common Kotlin data class.

## Metadata

`rawMetadataJson()` returns the complete internal-compressed PMTiles metadata JSON string after
UTF-8 decoding. `metadata()` parses the PMTiles-defined keys with `kotlinx.serialization.json`:
`name`, `description`, `attribution`, `type`, `version`, `encoding`, and `vector_layers`.

Custom metadata keys are preserved only in `rawMetadataJson()`. The typed `ArchiveMetadata` model is
intentionally shallow, and `vector_layers` is exposed as `vectorLayersJson`.

## Compression

`Compression.None` works on every supported target. `Compression.Gzip` works on JVM and
Kotlin/Native targets, including Apple, Linux, Windows, and Android Native. JVM gzip uses
`java.util.zip.GZIPInputStream`; Kotlin/Native gzip uses zlib C interop.

JavaScript and WASM targets compile the gzip path and throw `NotImplementedError` when gzip
decompression is invoked. `Compression.Brotli`, `Compression.Zstd`, `Compression.Unknown`, and
unregistered raw compression codes are preserved in header models and fail with
`UnsupportedCompression` whenever decompression is required.

## Warnings

Strict mode is the default and rejects spec violations. `ArchiveOpenOptions.Lenient` records
recoverable anomalies in append order. Use `warningCount` and `warningAt(index)` for the exported
warning surface. Kotlin callers can also use `warnings()` for a snapshot list; that list accessor is
hidden from Objective-C and Swift.

Warnings never replace hard failures for unsafe ranges, malformed directories, overflow,
decompression failures, or source instability.

## Unsupported Scope

This module does not implement:

- PMTiles writing.
- PMTiles v1 or v2 reading.
- Built-in HTTP, filesystem, object-store, browser `Blob`/`File`, or Node sources.
- Foreign JavaScript export APIs such as `@JsExport`, generated TypeScript declarations, or
  JavaScript facade APIs.
- JavaScript or WASM gzip decompression.
- Custom compression codec registration.
- Tile payload decoding or renderer integration.
