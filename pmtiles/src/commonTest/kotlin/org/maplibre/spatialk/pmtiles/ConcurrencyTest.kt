@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package org.maplibre.spatialk.pmtiles

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.append
import kotlinx.io.bytestring.buildByteString
import org.maplibre.spatialk.pmtiles.internal.DirectoryEntry
import org.maplibre.spatialk.pmtiles.internal.TestHeaderFields
import org.maplibre.spatialk.pmtiles.internal.buildArchiveWithSections
import org.maplibre.spatialk.pmtiles.internal.buildSingleTileArchive
import org.maplibre.spatialk.pmtiles.internal.encodeDirectory

class ConcurrencyTest {
    @Test
    fun concurrentLeafLookupsShareInFlightSourceRead() = runTest {
        val tileId = 2L
        val leafBytes =
            encodeDirectory(
                DirectoryEntry(tileId = tileId, offset = 1uL, length = 2, runLength = 1)
            )
        val rootBytes =
            encodeDirectory(
                DirectoryEntry(tileId = 1, offset = 0uL, length = leafBytes.size, runLength = 0)
            )
        val leafRange = ByteRange(200uL, leafBytes.size.toULong())
        val archiveBytes =
            buildArchiveWithSections(
                fields =
                    TestHeaderFields(
                        rootLength = rootBytes.size.toULong(),
                        leafDirectoriesOffset = leafRange.offset,
                        leafDirectoriesLength = leafRange.length,
                        tileDataOffset = 400uL,
                        tileDataLength = 3uL,
                    ),
                rootBytes = rootBytes,
                leafBytes = leafBytes,
            )
        val source = BlockingRangeSource(archiveBytes, blockedRange = leafRange)
        val archive = PmTiles.open(source)
        val coord = TileIds.toZxy(tileId)

        val first = async { archive.findTileRange(coord.z, coord.x, coord.y) }
        source.blockedReadStarted.await()
        val second = async { archive.findTileRange(coord.z, coord.x, coord.y) }
        runCurrent()

        assertEquals(1, source.blockedReadCount)

        source.releaseBlockedRead.complete(Unit)

        assertEquals(ByteRange(401uL, 2uL), first.await()?.archiveRange)
        assertEquals(ByteRange(401uL, 2uL), second.await()?.archiveRange)
        assertEquals(1, source.reads.count { it == leafRange })
    }

    @Test
    fun closeDuringInFlightReadFailsWithClosedAndIsIdempotent() = runTest {
        val archiveBytes = buildSingleTileArchive(ByteString(1, 2, 3))
        val tileRange = ByteRange(132uL, 3uL)
        val source = BlockingRangeSource(archiveBytes, blockedRange = tileRange)
        val archive = PmTiles.open(source)

        var readError: Throwable? = null
        val read = launch {
            try {
                archive.readStoredTile(0, 0, 0)
            } catch (error: Throwable) {
                readError = error
            }
        }
        source.blockedReadStarted.await()

        archive.close()
        archive.close()
        source.releaseBlockedRead.complete(Unit)
        read.join()

        val inFlightError =
            assertFailsWith<PmTilesException> {
                readError?.let { throw it }
            }
        assertEquals(PmTilesErrorCodes.Closed, inFlightError.code)

        val afterCloseError =
            assertSuspendFailsWith<PmTilesException> {
                archive.readStoredTile(0, 0, 0)
            }
        assertEquals(PmTilesErrorCodes.Closed, afterCloseError.code)
    }

    @Test
    fun leafDirectoryCacheEvictsLeastRecentlyUsedEntry() = runTest {
        val firstTileId = 2L
        val secondTileId = 3L
        val firstLeafBytes =
            encodeDirectory(
                DirectoryEntry(tileId = firstTileId, offset = 0uL, length = 1, runLength = 1)
            )
        val secondLeafBytes =
            encodeDirectory(
                DirectoryEntry(tileId = secondTileId, offset = 1uL, length = 1, runLength = 1)
            )
        val firstLeafRange = ByteRange(200uL, firstLeafBytes.size.toULong())
        val secondLeafRange =
            ByteRange(200uL + firstLeafBytes.size.toULong(), secondLeafBytes.size.toULong())
        val rootBytes =
            encodeDirectory(
                DirectoryEntry(
                    tileId = firstTileId,
                    offset = 0uL,
                    length = firstLeafBytes.size,
                    runLength = 0,
                ),
                DirectoryEntry(
                    tileId = secondTileId,
                    offset = firstLeafBytes.size.toULong(),
                    length = secondLeafBytes.size,
                    runLength = 0,
                ),
            )
        val archiveBytes =
            buildArchiveWithSections(
                fields =
                    TestHeaderFields(
                        rootLength = rootBytes.size.toULong(),
                        leafDirectoriesOffset = 200uL,
                        leafDirectoriesLength =
                            firstLeafBytes.size.toULong() + secondLeafBytes.size.toULong(),
                        tileDataOffset = 400uL,
                        tileDataLength = 2uL,
                    ),
                rootBytes = rootBytes,
                leafBytes =
                    buildByteString(firstLeafBytes.size + secondLeafBytes.size) {
                        append(firstLeafBytes)
                        append(secondLeafBytes)
                    },
            )
        val source = BlockingRangeSource(archiveBytes)
        val archive =
            PmTiles.open(
                source,
                options =
                    ArchiveOpenOptions.build {
                        limits = ArchiveLimits.build { maxLeafDirectoryCacheEntries = 1 }
                    },
            )
        val firstCoord = TileIds.toZxy(firstTileId)
        val secondCoord = TileIds.toZxy(secondTileId)

        archive.findTileRange(firstCoord.z, firstCoord.x, firstCoord.y)
        archive.findTileRange(secondCoord.z, secondCoord.x, secondCoord.y)
        archive.findTileRange(firstCoord.z, firstCoord.x, firstCoord.y)

        assertEquals(2, source.reads.count { it == firstLeafRange })
        assertEquals(1, source.reads.count { it == secondLeafRange })
    }
}

private class BlockingRangeSource(
    private val bytes: ByteString,
    private val blockedRange: ByteRange? = null,
) : ByteRangeSource {
    val reads = mutableListOf<ByteRange>()
    val blockedReadStarted = CompletableDeferred<Unit>()
    val releaseBlockedRead = CompletableDeferred<Unit>()
    var blockedReadCount = 0
        private set

    override suspend fun size(): ULong = bytes.size.toULong()

    override suspend fun read(range: ByteRange): ByteString {
        reads += range
        if (range == blockedRange) {
            blockedReadCount += 1
            blockedReadStarted.complete(Unit)
            releaseBlockedRead.await()
        }
        val start = range.offset.toInt()
        val length = range.length.toInt()
        return bytes.substring(start, start + length)
    }
}

private suspend inline fun <reified T : Throwable> assertSuspendFailsWith(
    noinline block: suspend () -> Unit
): T {
    var failure: Throwable? = null
    try {
        block()
    } catch (error: Throwable) {
        failure = error
    }
    return assertFailsWith<T> {
        failure?.let { throw it }
    }
}
