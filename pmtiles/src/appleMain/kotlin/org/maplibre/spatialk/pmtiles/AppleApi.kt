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

/** Tile payload data as a Foundation value. The returned data is a copy. */
public val ArchiveTile.data: NSData
    get() = withPayloadBytesUnsafe { it.toNSData() }

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
    copy(validationMode = validationMode)

/** Returns a copy of these options with [limits]. */
public fun ArchiveOpenOptions.with(limits: ArchiveLimits): ArchiveOpenOptions =
    copy(limits = limits)

/** Returns a copy of these options with [validationMode] and [limits]. */
public fun ArchiveOpenOptions.with(
    validationMode: ValidationMode,
    limits: ArchiveLimits,
): ArchiveOpenOptions = copy(validationMode = validationMode, limits = limits)

/** Returns a copy of these limits with [maxInitialReadBytes]. */
@OptIn(ExperimentalObjCName::class)
public fun ArchiveLimits.withMaxInitialReadBytes(
    @ObjCName(swiftName = "_") maxInitialReadBytes: ULong
): ArchiveLimits = copy(maxInitialReadBytes = maxInitialReadBytes)

/** Returns a copy of these limits with [maxMetadataBytes]. */
@OptIn(ExperimentalObjCName::class)
public fun ArchiveLimits.withMaxMetadataBytes(
    @ObjCName(swiftName = "_") maxMetadataBytes: ULong
): ArchiveLimits = copy(maxMetadataBytes = maxMetadataBytes)

/** Returns a copy of these limits with [maxDirectoryCompressedBytes]. */
@OptIn(ExperimentalObjCName::class)
public fun ArchiveLimits.withMaxDirectoryCompressedBytes(
    @ObjCName(swiftName = "_") maxDirectoryCompressedBytes: ULong
): ArchiveLimits = copy(maxDirectoryCompressedBytes = maxDirectoryCompressedBytes)

/** Returns a copy of these limits with [maxDirectoryDecompressedBytes]. */
@OptIn(ExperimentalObjCName::class)
public fun ArchiveLimits.withMaxDirectoryDecompressedBytes(
    @ObjCName(swiftName = "_") maxDirectoryDecompressedBytes: ULong
): ArchiveLimits =
    copy(
        maxDirectoryDecompressedBytes = maxDirectoryDecompressedBytes,
        maxDirectoryEntries =
            if (
                minEncodedDirectoryBytes(maxDirectoryEntries).toULong() <=
                    maxDirectoryDecompressedBytes
            ) {
                maxDirectoryEntries
            } else {
                maxDirectoryDecompressedBytes.defaultDirectoryEntryLimit()
            },
    )

/** Returns a copy of these limits with [maxDirectoryEntries]. */
@OptIn(ExperimentalObjCName::class)
public fun ArchiveLimits.withMaxDirectoryEntries(
    @ObjCName(swiftName = "_") maxDirectoryEntries: Int
): ArchiveLimits = copy(maxDirectoryEntries = maxDirectoryEntries)

/** Returns a copy of these limits with [maxTileCompressedBytes]. */
@OptIn(ExperimentalObjCName::class)
public fun ArchiveLimits.withMaxTileCompressedBytes(
    @ObjCName(swiftName = "_") maxTileCompressedBytes: ULong
): ArchiveLimits = copy(maxTileCompressedBytes = maxTileCompressedBytes)

/** Returns a copy of these limits with [maxTileDecompressedBytes]. */
@OptIn(ExperimentalObjCName::class)
public fun ArchiveLimits.withMaxTileDecompressedBytes(
    @ObjCName(swiftName = "_") maxTileDecompressedBytes: ULong
): ArchiveLimits = copy(maxTileDecompressedBytes = maxTileDecompressedBytes)

/** Returns a copy of these limits with [maxDirectoryDepth]. */
@OptIn(ExperimentalObjCName::class)
public fun ArchiveLimits.withMaxDirectoryDepth(
    @ObjCName(swiftName = "_") maxDirectoryDepth: Int
): ArchiveLimits = copy(maxDirectoryDepth = maxDirectoryDepth)

/** Returns a copy of these limits with [maxLeafDirectoryCacheEntries]. */
@OptIn(ExperimentalObjCName::class)
public fun ArchiveLimits.withMaxLeafDirectoryCacheEntries(
    @ObjCName(swiftName = "_") maxLeafDirectoryCacheEntries: Int
): ArchiveLimits = copy(maxLeafDirectoryCacheEntries = maxLeafDirectoryCacheEntries)

/** Returns a copy of these limits with [maxVarintBytes]. */
@OptIn(ExperimentalObjCName::class)
public fun ArchiveLimits.withMaxVarintBytes(
    @ObjCName(swiftName = "_") maxVarintBytes: Int
): ArchiveLimits = copy(maxVarintBytes = maxVarintBytes)

/** Returns a copy of this coalescing configuration with [maxCoalescedBytes]. */
@OptIn(ExperimentalObjCName::class)
public fun TileReadCoalescing.withMaxCoalescedBytes(
    @ObjCName(swiftName = "_") maxCoalescedBytes: ULong
): TileReadCoalescing = copy(maxCoalescedBytes = maxCoalescedBytes)

/** Returns a copy of this coalescing configuration with [maxGapBytes]. */
@OptIn(ExperimentalObjCName::class)
public fun TileReadCoalescing.withMaxGapBytes(
    @ObjCName(swiftName = "_") maxGapBytes: ULong
): TileReadCoalescing = copy(maxGapBytes = maxGapBytes)

/** Returns a copy of these options with [decompressor] registered for [compression]. */
public fun ArchiveOpenOptions.withDecompressor(
    compression: CompressionCode,
    decompressor: DataDecompressor,
): ArchiveOpenOptions =
    withDecompressor(
        compression,
        Decompressor { bytes, limits ->
            decompressor.decompress(bytes.toNSData(), limits).toByteArray()
        },
    )

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

private const val DEFAULT_MIN_DIRECTORY_ENTRY_BYTES = 17
private const val MIN_ENCODED_DIRECTORY_ENTRY_BYTES = 4
private const val VARINT_PAYLOAD_BITS = 7

private fun ULong.defaultDirectoryEntryLimit(): Int =
    minOf(
            this / DEFAULT_MIN_DIRECTORY_ENTRY_BYTES.toULong(),
            Int.MAX_VALUE.toULong(),
        )
        .toInt()

private fun minEncodedDirectoryBytes(entryCount: Int): Long {
    val countBytes = varintByteCount(entryCount).toLong()
    val entryBytes = entryCount.toLong() * MIN_ENCODED_DIRECTORY_ENTRY_BYTES
    return countBytes + entryBytes
}

private fun varintByteCount(value: Int): Int {
    var bytes = 1
    var remaining = value ushr VARINT_PAYLOAD_BITS
    while (remaining != 0) {
        bytes += 1
        remaining = remaining ushr VARINT_PAYLOAD_BITS
    }
    return bytes
}
