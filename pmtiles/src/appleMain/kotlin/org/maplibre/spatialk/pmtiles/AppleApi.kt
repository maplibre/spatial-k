package org.maplibre.spatialk.pmtiles

import kotlin.coroutines.cancellation.CancellationException
import platform.Foundation.NSData

public val ArchiveTile.data: NSData
    get() = throw NotImplementedError()

public interface ByteRangeDataSource {
    public suspend fun size(): ULong

    public suspend fun read(offset: ULong, length: Int): NSData
}

@Throws(PmTilesException::class, CancellationException::class)
public suspend fun PmTilesArchive.Companion.open(
    source: ByteRangeDataSource,
    options: ArchiveOpenOptions = ArchiveOpenOptions.Default,
): PmTilesArchive = throw NotImplementedError()
