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

## Byte range sources

`PmTilesArchive` reads through `ByteRangeSource`, so callers can back archives with files, network
requests, memory, or any other source that can return exact byte ranges.

=== "Kotlin"

    ```kotlin
    --8<-- "pmtiles/src/commonTest/kotlin/org/maplibre/spatialk/pmtiles/KotlinDocsTest.kt:byteRangeSource"
    ```

## Opening archives

Open an archive with `PmTilesArchive.open`, then read header fields, metadata, tile ranges, or tile
payload bytes.

=== "Kotlin"

    ```kotlin
    --8<-- "pmtiles/src/commonTest/kotlin/org/maplibre/spatialk/pmtiles/KotlinDocsTest.kt:openArchive"
    ```

By default, tile reads return bytes as stored in the archive. Use `getTileCompressed` to request
stored bytes explicitly, or `getTileDecompressed` to decompress supported tile payloads.

=== "Kotlin"

    ```kotlin
    --8<-- "pmtiles/src/commonTest/kotlin/org/maplibre/spatialk/pmtiles/KotlinDocsTest.kt:decompressedTiles"
    ```

Use `getTiles` to read a batch of tile payloads with coalesced source ranges.

=== "Kotlin"

    ```kotlin
    --8<-- "pmtiles/src/commonTest/kotlin/org/maplibre/spatialk/pmtiles/KotlinDocsTest.kt:batchTiles"
    ```

## Decompressors

Spatial-K includes platform defaults for uncompressed data and gzip where available. Register other
compression codecs, such as brotli or zstd, with `ArchiveOpenOptions.withDecompressor`.

=== "Kotlin"

    ```kotlin
    --8<-- "pmtiles/src/commonTest/kotlin/org/maplibre/spatialk/pmtiles/KotlinDocsTest.kt:customDecompressor"
    ```

## Validation

Archives open in strict mode by default. `ArchiveOpenOptions.Lenient` preserves recoverable issues
as warnings, which can be inspected after opening or after metadata and tile lookups.

=== "Kotlin"

    ```kotlin
    --8<-- "pmtiles/src/commonTest/kotlin/org/maplibre/spatialk/pmtiles/KotlinDocsTest.kt:lenientWarnings"
    ```
