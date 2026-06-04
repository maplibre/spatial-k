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
    @Throws(PmTilesException::class, CancellationException::class) public suspend fun size(): ULong

    @Throws(PmTilesException::class, CancellationException::class)
    public suspend fun read(offset: ULong, length: Int): NSData
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

/** Returns a copy of these options with [decompressor] registered for [compression]. */
public fun ArchiveOpenOptions.withDecompressor(
    compression: Compression,
    decompressor: DataDecompressor,
): ArchiveOpenOptions =
    withDecompressor(
        compression,
        Decompressor { bytes, limits ->
            decompressor.decompress(bytes.toNSData(), limits).toByteArray()
        },
    )

@OptIn(ExperimentalObjCName::class)
@ObjCName(name = "open", swiftName = "open")
@Throws(PmTilesException::class, CancellationException::class)
public suspend fun PmTilesArchive.Companion.open(
    source: ByteRangeDataSource,
    options: ArchiveOpenOptions = ArchiveOpenOptions.Default,
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
