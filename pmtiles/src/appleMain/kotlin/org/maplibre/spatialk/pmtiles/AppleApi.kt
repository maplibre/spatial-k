@file:OptIn(ExperimentalObjCName::class)

package org.maplibre.spatialk.pmtiles

import kotlin.coroutines.cancellation.CancellationException
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName
import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.toByteString
import kotlinx.io.bytestring.toNSData
import platform.Foundation.NSData

public interface ByteRangeDataSource {
    public fun size(): ULong

    @Throws(PmTilesException::class, CancellationException::class)
    public suspend fun read(offset: ULong, length: ULong): NSData
}

/** Apple-friendly decompressor that uses [NSData] payloads. */
public interface DataDecompressor {
    /**
     * Returns decompressed [data].
     *
     * Implementations should enforce [limits] while decoding. The reader also validates the
     * returned byte count before using it.
     */
    @Throws(PmTilesException::class, CancellationException::class)
    public suspend fun decompress(data: NSData, limits: DecompressionLimits): NSData
}

/** Returns a copy of these options with [validationMode]. */
public fun ArchiveOpenOptions.with(validationMode: ValidationMode): ArchiveOpenOptions =
    toBuilder()
        .apply {
            this.validationMode = validationMode
        }
        .build()

/** Returns a copy of these options with [limits]. */
public fun ArchiveOpenOptions.with(limits: ArchiveLimits): ArchiveOpenOptions =
    toBuilder()
        .apply {
            this.limits = limits
        }
        .build()

/** Returns a copy of these options with [validationMode] and [limits]. */
public fun ArchiveOpenOptions.with(
    validationMode: ValidationMode,
    limits: ArchiveLimits,
): ArchiveOpenOptions =
    toBuilder()
        .apply {
            this.validationMode = validationMode
            this.limits = limits
        }
        .build()

/** Returns a copy of these limits with [maxInitialReadBytes]. */
@ObjCName(swiftName = "with")
public fun ArchiveLimits.withMaxInitialReadBytes(
    @ObjCName(swiftName = "maxInitialReadBytes") maxInitialReadBytes: ULong
): ArchiveLimits =
    toBuilder()
        .apply {
            this.maxInitialReadBytes = maxInitialReadBytes
        }
        .build()

/** Returns a copy of these limits with [maxMetadataBytes]. */
@ObjCName(swiftName = "with")
public fun ArchiveLimits.withMaxMetadataBytes(
    @ObjCName(swiftName = "maxMetadataBytes") maxMetadataBytes: ULong
): ArchiveLimits =
    toBuilder()
        .apply {
            this.maxMetadataBytes = maxMetadataBytes
        }
        .build()

/** Returns a copy of these limits with [maxDirectoryCompressedBytes]. */
@ObjCName(swiftName = "with")
public fun ArchiveLimits.withMaxDirectoryCompressedBytes(
    @ObjCName(swiftName = "maxDirectoryCompressedBytes") maxDirectoryCompressedBytes: ULong
): ArchiveLimits =
    toBuilder()
        .apply {
            this.maxDirectoryCompressedBytes = maxDirectoryCompressedBytes
        }
        .build()

/** Returns a copy of these limits with [maxDirectoryDecompressedBytes]. */
@ObjCName(swiftName = "with")
public fun ArchiveLimits.withMaxDirectoryDecompressedBytes(
    @ObjCName(swiftName = "maxDirectoryDecompressedBytes") maxDirectoryDecompressedBytes: ULong
): ArchiveLimits =
    toBuilder()
        .apply {
            this.maxDirectoryDecompressedBytes = maxDirectoryDecompressedBytes
        }
        .build()

/** Returns a copy of these limits with [maxTileCompressedBytes]. */
@ObjCName(swiftName = "with")
public fun ArchiveLimits.withMaxTileCompressedBytes(
    @ObjCName(swiftName = "maxTileCompressedBytes") maxTileCompressedBytes: ULong
): ArchiveLimits =
    toBuilder()
        .apply {
            this.maxTileCompressedBytes = maxTileCompressedBytes
        }
        .build()

/** Returns a copy of these limits with [maxTileDecompressedBytes]. */
@ObjCName(swiftName = "with")
public fun ArchiveLimits.withMaxTileDecompressedBytes(
    @ObjCName(swiftName = "maxTileDecompressedBytes") maxTileDecompressedBytes: ULong
): ArchiveLimits =
    toBuilder()
        .apply {
            this.maxTileDecompressedBytes = maxTileDecompressedBytes
        }
        .build()

/** Returns a copy of these limits with [maxDirectoryDepth]. */
@ObjCName(swiftName = "with")
public fun ArchiveLimits.withMaxDirectoryDepth(
    @ObjCName(swiftName = "maxDirectoryDepth") maxDirectoryDepth: Int
): ArchiveLimits =
    toBuilder()
        .apply {
            this.maxDirectoryDepth = maxDirectoryDepth
        }
        .build()

/** Returns a copy of these limits with [maxLeafDirectoryCacheEntries]. */
@ObjCName(swiftName = "with")
public fun ArchiveLimits.withMaxLeafDirectoryCacheEntries(
    @ObjCName(swiftName = "maxLeafDirectoryCacheEntries") maxLeafDirectoryCacheEntries: Int
): ArchiveLimits =
    toBuilder()
        .apply {
            this.maxLeafDirectoryCacheEntries = maxLeafDirectoryCacheEntries
        }
        .build()

/** Returns a copy of these limits with [maxVarintBytes]. */
@ObjCName(swiftName = "with")
public fun ArchiveLimits.withMaxVarintBytes(
    @ObjCName(swiftName = "maxVarintBytes") maxVarintBytes: Int
): ArchiveLimits =
    toBuilder()
        .apply {
            this.maxVarintBytes = maxVarintBytes
        }
        .build()

/** Returns a copy of this coalescing configuration with [maxCoalescedBytes]. */
@ObjCName(swiftName = "with")
public fun TileReadCoalescing.withMaxCoalescedBytes(
    @ObjCName(swiftName = "maxCoalescedBytes") maxCoalescedBytes: ULong
): TileReadCoalescing =
    toBuilder()
        .apply {
            this.maxCoalescedBytes = maxCoalescedBytes
        }
        .build()

/** Returns a copy of this coalescing configuration with [maxGapBytes]. */
@ObjCName(swiftName = "with")
public fun TileReadCoalescing.withMaxGapBytes(
    @ObjCName(swiftName = "maxGapBytes") maxGapBytes: ULong
): TileReadCoalescing =
    toBuilder()
        .apply {
            this.maxGapBytes = maxGapBytes
        }
        .build()

/** Returns a copy of these options with [decompressor] registered for [compression]. */
public fun ArchiveOpenOptions.withDecompressor(
    compression: CompressionCode,
    decompressor: DataDecompressor,
): ArchiveOpenOptions =
    toBuilder()
        .decompressor(
            compression,
            Decompressor { bytes, limits ->
                decompressor.decompress(bytes.toNSData(), limits).toByteString()
            },
        )
        .build()

@Throws(PmTilesException::class, CancellationException::class)
public suspend fun PmTiles.open(source: ByteRangeDataSource): PmTilesArchive =
    open(source = source, options = ArchiveOpenOptions())

@Throws(PmTilesException::class, CancellationException::class)
public suspend fun PmTiles.open(
    source: ByteRangeDataSource,
    options: ArchiveOpenOptions = ArchiveOpenOptions(),
): PmTilesArchive =
    open(
        source =
            object : ByteRangeSource {
                override suspend fun size(): ULong = source.size()

                override suspend fun read(range: ByteRange): ByteString =
                    source.read(range.offset, range.length).toByteString()
            },
        options = options,
    )
