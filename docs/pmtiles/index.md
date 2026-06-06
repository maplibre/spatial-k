# PMTiles

The `pmtiles` module reads [PMTiles v3](https://docs.protomaps.com/pmtiles/) archives from a
caller-provided byte range source.

Details can be found in the [API reference](../api/pmtiles/index.html).

## Installation

=== "Multiplatform"

    ```kotlin
    commonMain {
        dependencies {
            implementation("org.maplibre.spatialk:pmtiles:{{ gradle.project_version }}")
        }
    }
    ```

=== "JVM"

    ```kotlin
    dependencies {
        implementation("org.maplibre.spatialk:pmtiles-jvm:{{ gradle.project_version }}")
    }
    ```

## Specification coverage

This module implements PMTiles v3 reading. Writing may be implemented in the future.

It does not provide filesystem or HTTP sources, or decode tile payload formats such as MVT, raster
images, or MLT.

Uncompressed data is supported on all platforms. Gzip is built in on JVM, native, and web targets
where the runtime provides `DecompressionStream`; brotli, zstd, and other compression codes require
custom decompressors.

## Byte range sources

`PmTilesArchive` reads through `ByteRangeSource`, so callers can back archives with files, network
requests, memory, or any other source that can return exact byte ranges.

=== "Kotlin"

    ```kotlin
    --8<-- "pmtiles/src/commonTest/kotlin/org/maplibre/spatialk/pmtiles/KotlinDocsTest.kt:byteRangeSource"
    ```

=== "Swift"

    ```swift
    --8<-- "pmtiles/src/swiftTest/SwiftDocsTest.swift:byteRangeDataSource"
    ```

## Opening archives

Open an archive with `PmTiles.open`, then read header fields, metadata, tile ranges, or tile payload
bytes.

=== "Kotlin"

    ```kotlin
    --8<-- "pmtiles/src/commonTest/kotlin/org/maplibre/spatialk/pmtiles/KotlinDocsTest.kt:openArchive"
    ```

=== "Swift"

    ```swift
    --8<-- "pmtiles/src/swiftTest/SwiftDocsTest.swift:openArchive"
    ```

Use `readStoredTile` to read tile payload bytes as stored in the archive, or `readDecompressedTile`
to decompress supported tile payloads.

=== "Kotlin"

    ```kotlin
    --8<-- "pmtiles/src/commonTest/kotlin/org/maplibre/spatialk/pmtiles/KotlinDocsTest.kt:decompressedTiles"
    ```

=== "Swift"

    ```swift
    --8<-- "pmtiles/src/swiftTest/SwiftDocsTest.swift:decompressedTiles"
    ```

Use `readStoredTiles` to read a batch of tile payloads with coalesced source ranges.

=== "Kotlin"

    ```kotlin
    --8<-- "pmtiles/src/commonTest/kotlin/org/maplibre/spatialk/pmtiles/KotlinDocsTest.kt:batchTiles"
    ```

=== "Swift"

    ```swift
    --8<-- "pmtiles/src/swiftTest/SwiftDocsTest.swift:batchTiles"
    ```

## Decompressors

Spatial-K includes platform defaults for uncompressed data and gzip where available. Register other
compression codecs, such as brotli or zstd, through `ArchiveOpenOptions`.

=== "Kotlin"

    ```kotlin
    --8<-- "pmtiles/src/commonTest/kotlin/org/maplibre/spatialk/pmtiles/KotlinDocsTest.kt:customDecompressor"
    ```

=== "Swift"

    ```swift
    --8<-- "pmtiles/src/swiftTest/SwiftDocsTest.swift:customDecompressor"
    ```

## Validation

Archives open in strict mode by default. `ValidationMode.Lenient` preserves recoverable issues as
warnings, which can be inspected after opening or after metadata and tile lookups.

=== "Kotlin"

    ```kotlin
    --8<-- "pmtiles/src/commonTest/kotlin/org/maplibre/spatialk/pmtiles/KotlinDocsTest.kt:lenientWarnings"
    ```

=== "Swift"

    ```swift
    --8<-- "pmtiles/src/swiftTest/SwiftDocsTest.swift:lenientWarnings"
    ```
