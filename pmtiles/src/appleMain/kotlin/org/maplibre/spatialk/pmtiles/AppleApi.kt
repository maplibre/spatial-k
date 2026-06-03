@file:OptIn(kotlin.experimental.ExperimentalObjCName::class)

package org.maplibre.spatialk.pmtiles

import kotlin.coroutines.cancellation.CancellationException
import kotlin.native.ObjCName
import org.maplibre.spatialk.pmtiles.internal.pmTilesException
import platform.Foundation.NSData

public val ArchiveTile.data: NSData
    get() = bytes.toNSData()

public interface ByteRangeDataSource {
    @Throws(PmTilesException::class, CancellationException::class) public suspend fun size(): ULong

    @Throws(PmTilesException::class, CancellationException::class)
    public suspend fun read(offset: ULong, length: Int): NSData
}

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

internal expect fun ByteArray.toNSData(): NSData

internal fun NSData.toByteArray(): ByteArray {
    val byteCount = byteCount()
    if (byteCount == 0uL) return ByteArray(0)
    if (byteCount > Int.MAX_VALUE.toULong()) {
        throw pmTilesException(
            PmTilesErrorCode.SourceUnavailable,
            "Byte range data source returned $byteCount bytes, which exceeds Int.MAX_VALUE.",
        )
    }
    return readBytes(byteCount.toInt())
}

internal expect fun NSData.byteCount(): ULong

internal expect fun NSData.readBytes(length: Int): ByteArray
