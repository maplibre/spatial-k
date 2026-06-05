@file:OptIn(ExperimentalObjCName::class)

package org.maplibre.spatialk.pmtiles

import kotlin.coroutines.cancellation.CancellationException
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UnsafeNumber
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.readBytes
import kotlinx.cinterop.usePinned
import org.maplibre.spatialk.pmtiles.internal.pmTilesException
import platform.Foundation.NSData
import platform.Foundation.NSMutableData
import platform.posix.memcpy

/** Returns a copy of this byte payload as a Foundation value. */
public fun ByteString.toNSData(): NSData = withBytesUnsafe { it.toNSData() }

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

/** Returns a copy of these limits with [maxDirectoryEntries]. */
@ObjCName(swiftName = "with")
public fun ArchiveLimits.withMaxDirectoryEntries(
    @ObjCName(swiftName = "maxDirectoryEntries") maxDirectoryEntries: Int
): ArchiveLimits =
    toBuilder()
        .apply {
            this.maxDirectoryEntries = maxDirectoryEntries
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
): TileReadCoalescing = copy(maxCoalescedBytes = maxCoalescedBytes)

/** Returns a copy of this coalescing configuration with [maxGapBytes]. */
@ObjCName(swiftName = "with")
public fun TileReadCoalescing.withMaxGapBytes(
    @ObjCName(swiftName = "maxGapBytes") maxGapBytes: ULong
): TileReadCoalescing = copy(maxGapBytes = maxGapBytes)

/** Returns a copy of these options with [decompressor] registered for [compression]. */
public fun ArchiveOpenOptions.withDecompressor(
    compression: CompressionCode,
    decompressor: DataDecompressor,
): ArchiveOpenOptions =
    toBuilder()
        .decompressor(
            compression,
            Decompressor { bytes, limits ->
                decompressor.decompress(bytes.toNSData(), limits).toByteArray()
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

                override suspend fun read(range: ByteRange): ByteArray =
                    source.read(range.offset, range.length).toByteArray()
            },
        options = options,
    )

internal fun NSData.toByteArray(): ByteArray {
    val byteCount =
        @OptIn(ExperimentalForeignApi::class, UnsafeNumber::class) length.convert<ULong>()
    if (byteCount == 0uL) return ByteArray(0)
    if (byteCount > Int.MAX_VALUE.toULong()) {
        throw pmTilesException(
            PmTilesErrorCode.SourceUnavailable,
            "Byte range data source returned $byteCount bytes, which exceeds Int.MAX_VALUE.",
        )
    }
    return readBytes(byteCount.toInt())
}

internal fun ByteArray.toNSData(): NSData {
    val data = NSMutableData()
    @OptIn(ExperimentalForeignApi::class, UnsafeNumber::class) data.setLength(size.convert())
    if (isNotEmpty()) {
        @OptIn(ExperimentalForeignApi::class)
        usePinned { pinned ->
            @OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)
            memcpy(data.mutableBytes, pinned.addressOf(0), size.convert())
        }
    }
    return data
}

internal fun NSData.readBytes(length: Int): ByteArray {
    val bytesPointer =
        @OptIn(ExperimentalForeignApi::class) bytes
            ?: throw pmTilesException(
                PmTilesErrorCode.SourceUnavailable,
                "Byte range data source returned non-empty NSData with a null bytes pointer.",
            )
    return (@OptIn(ExperimentalForeignApi::class) bytesPointer.readBytes(length))
}
