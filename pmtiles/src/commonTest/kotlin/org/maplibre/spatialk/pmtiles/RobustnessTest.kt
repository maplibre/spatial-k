package org.maplibre.spatialk.pmtiles

import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.test.runTest
import org.maplibre.spatialk.pmtiles.internal.MINIMAL_ROOT_DIRECTORY_BYTES
import org.maplibre.spatialk.pmtiles.internal.TestByteRangeSource
import org.maplibre.spatialk.pmtiles.internal.TestHeaderFields
import org.maplibre.spatialk.pmtiles.internal.buildArchiveWithSections
import org.maplibre.spatialk.pmtiles.internal.buildSingleTileArchive

class RobustnessTest {
    @Test
    fun hugeMetadataFails() = runTest {
        val metadataBytes = """{"name":"too large"}""".encodeToByteArray()
        val bytes =
            buildArchiveWithSections(
                fields =
                    TestHeaderFields(
                        metadataOffset = 200uL,
                        metadataLength = metadataBytes.size.toULong(),
                        tileDataOffset = 300uL,
                    ),
                rootBytes = MINIMAL_ROOT_DIRECTORY_BYTES,
                metadataBytes = metadataBytes,
            )

        val error =
            assertFailsWith<PmTilesException> {
                val archive =
                    PmTilesArchive.open(
                        TestByteRangeSource(bytes),
                        options =
                            ArchiveOpenOptions(
                                limits =
                                    ArchiveLimits.Default.copy(
                                        maxMetadataBytes = metadataBytes.size - 1
                                    )
                            ),
                    )
                archive.rawMetadataJson()
            }

        assertEquals(PmTilesErrorCode.LimitExceeded, error.code)
    }

    @Test
    fun hugeDirectoryFails() = runTest {
        val error =
            assertFailsWith<PmTilesException> {
                PmTilesArchive.open(
                    TestByteRangeSource(
                        buildArchiveWithSections(
                            fields =
                                TestHeaderFields(
                                    rootLength = MINIMAL_ROOT_DIRECTORY_BYTES.size.toULong()
                                ),
                            rootBytes = MINIMAL_ROOT_DIRECTORY_BYTES,
                        )
                    ),
                    options =
                        ArchiveOpenOptions(
                            limits =
                                ArchiveLimits.Default.copy(
                                    maxDirectoryCompressedBytes =
                                        MINIMAL_ROOT_DIRECTORY_BYTES.size - 1
                                )
                        ),
                )
            }

        assertEquals(PmTilesErrorCode.LimitExceeded, error.code)
    }

    @Test
    fun hugeTileFails() = runTest {
        val tileBytes = byteArrayOf(1, 2, 3, 4)
        val error =
            assertFailsWith<PmTilesException> {
                val archive =
                    PmTilesArchive.open(
                        TestByteRangeSource(buildSingleTileArchive(tileBytes)),
                        options =
                            ArchiveOpenOptions(
                                limits =
                                    ArchiveLimits.Default.copy(
                                        maxTileCompressedBytes = tileBytes.size - 1
                                    )
                            ),
                    )
                archive.getStoredTile(0, 0, 0)
            }

        assertEquals(PmTilesErrorCode.LimitExceeded, error.code)
    }

    @Test
    fun cancellationRaceDoesNotReturnPartialData() = runTest {
        val tileBytes = byteArrayOf(9, 8, 7)
        val source = FirstTileReadBlockingSource(buildSingleTileArchive(tileBytes))
        val archive = PmTilesArchive.open(source)
        val expectedTileRange = requireNotNull(archive.getTileRange(0, 0, 0)).archiveRange

        source.blockNextRead(expectedTileRange)
        val cancelledRead = async { archive.getStoredTile(0, 0, 0) }
        source.blockedReadStarted.await()
        cancelledRead.cancelAndJoin()

        var cancellationError: Throwable? = null
        try {
            cancelledRead.await()
        } catch (error: Throwable) {
            cancellationError = error
        }
        assertFailsWith<CancellationException> {
            cancellationError?.let { throw it }
        }

        source.releaseBlockedRead.complete(Unit)
        val tile = archive.getStoredTile(0, 0, 0)

        requireNotNull(tile)
        assertContentEquals(tileBytes, tile.bytes)
    }
}

private class FirstTileReadBlockingSource(private val bytes: ByteArray) : ByteRangeSource {
    val blockedReadStarted = CompletableDeferred<Unit>()
    val releaseBlockedRead = CompletableDeferred<Unit>()
    private var blockedByteRange: ByteRange? = null

    fun blockNextRead(range: ByteRange) {
        blockedByteRange = range
    }

    override suspend fun size(): ULong = bytes.size.toULong()

    override suspend fun read(range: ByteRange): ByteArray {
        if (range == blockedByteRange) {
            blockedByteRange = null
            blockedReadStarted.complete(Unit)
            releaseBlockedRead.await()
        }
        val start = range.offset.toInt()
        return bytes.copyOfRange(start, start + range.length)
    }
}
