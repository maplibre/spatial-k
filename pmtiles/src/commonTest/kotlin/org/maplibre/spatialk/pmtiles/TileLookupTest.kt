package org.maplibre.spatialk.pmtiles

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest
import org.maplibre.spatialk.pmtiles.internal.DirectoryEntry
import org.maplibre.spatialk.pmtiles.internal.HEADER_BYTES
import org.maplibre.spatialk.pmtiles.internal.TestByteRangeSource
import org.maplibre.spatialk.pmtiles.internal.TestHeaderFields
import org.maplibre.spatialk.pmtiles.internal.buildArchive
import org.maplibre.spatialk.pmtiles.internal.buildArchiveWithSections
import org.maplibre.spatialk.pmtiles.internal.encodeDirectory

class TileLookupTest {
    @Test
    fun locatesDirectRootTileAndMissingTile() = runTest {
        val source = TestByteRangeSource(buildArchive())
        val archive = PmTilesArchive.open(source)

        val range = archive.getTileRange(0, 0, 0)
        val missingCoord = TileIds.toZxy(1)

        assertEquals(
            TileRange(
                tileId = 0,
                coord = TileCoord(0, 0, 0),
                archiveRange =
                    ByteRange(
                        offset = HEADER_BYTES.toULong() + 5uL,
                        length = 1,
                    ),
                tileType = TileType.Unknown,
                compression = Compression.None,
                directoryDepth = 0,
            ),
            range,
        )
        assertEquals(true, archive.containsTile(0, 0, 0))
        assertNull(archive.getTileRange(missingCoord.z, missingCoord.x, missingCoord.y))
        assertEquals(1, source.reads.size)
    }

    @Test
    fun locatesRootRunCoverage() = runTest {
        val rootBytes =
            encodeDirectory(DirectoryEntry(tileId = 1, offset = 5uL, length = 2, runLength = 3))
        val fields =
            TestHeaderFields(
                rootLength = rootBytes.size.toULong(),
                tileDataOffset = HEADER_BYTES.toULong() + rootBytes.size.toULong(),
                tileDataLength = 7uL,
            )
        val archive =
            PmTilesArchive.open(TestByteRangeSource(buildArchive(fields, rootBytes = rootBytes)))
        val coord = TileIds.toZxy(2)

        val range = archive.getTileRange(coord.z, coord.x, coord.y)

        assertEquals(2, range?.tileId)
        assertEquals(coord, range?.coord)
        assertEquals(ByteRange(fields.tileDataOffset + 5uL, 2), range?.archiveRange)
        assertEquals(0, range?.directoryDepth)
    }

    @Test
    fun locatesLeafTileAndCachesLeafDirectory() = runTest {
        val leafBytes =
            encodeDirectory(DirectoryEntry(tileId = 2, offset = 3uL, length = 4, runLength = 1))
        val rootBytes =
            encodeDirectory(
                DirectoryEntry(tileId = 1, offset = 0uL, length = leafBytes.size, runLength = 0)
            )
        val fields =
            TestHeaderFields(
                rootLength = rootBytes.size.toULong(),
                leafDirectoriesOffset = 200uL,
                leafDirectoriesLength = leafBytes.size.toULong(),
                tileDataOffset = 300uL,
                tileDataLength = 7uL,
            )
        val source =
            TestByteRangeSource(
                buildArchiveWithLeafBytes(
                    fields = fields,
                    rootBytes = rootBytes,
                    leafBytes = leafBytes,
                )
            )
        val archive = PmTilesArchive.open(source)
        val coord = TileIds.toZxy(2)

        val range = archive.getTileRange(coord.z, coord.x, coord.y)
        val contains = archive.containsTile(coord.z, coord.x, coord.y)

        assertEquals(ByteRange(303uL, 4), range?.archiveRange)
        assertEquals(1, range?.directoryDepth)
        assertEquals(true, contains)
        assertEquals(
            listOf(
                ByteRange(0uL, 400),
                ByteRange(200uL, leafBytes.size),
            ),
            source.reads,
        )
    }

    @Test
    fun lenientModeLocatesNestedLeafAndRecordsWarning() = runTest {
        val tileLeafBytes =
            encodeDirectory(DirectoryEntry(tileId = 2, offset = 1uL, length = 2, runLength = 1))
        val nestedLeafOffset = 20uL
        val firstLeafBytes =
            encodeDirectory(
                DirectoryEntry(
                    tileId = 2,
                    offset = nestedLeafOffset,
                    length = tileLeafBytes.size,
                    runLength = 0,
                )
            )
        val rootBytes =
            encodeDirectory(
                DirectoryEntry(
                    tileId = 1,
                    offset = 0uL,
                    length = firstLeafBytes.size,
                    runLength = 0,
                )
            )
        val leafSection = ByteArray(nestedLeafOffset.toInt() + tileLeafBytes.size)
        firstLeafBytes.copyInto(leafSection)
        tileLeafBytes.copyInto(leafSection, destinationOffset = nestedLeafOffset.toInt())
        val fields =
            TestHeaderFields(
                rootLength = rootBytes.size.toULong(),
                leafDirectoriesOffset = 200uL,
                leafDirectoriesLength = leafSection.size.toULong(),
                tileDataOffset = 400uL,
                tileDataLength = 3uL,
                addressedTiles = 1uL,
                tileEntries = 1uL,
                tileContents = 1uL,
                tileType = TileType.Png.code,
            )
        val archive =
            PmTilesArchive.open(
                TestByteRangeSource(
                    buildArchiveWithLeafBytes(
                        fields = fields,
                        rootBytes = rootBytes,
                        leafBytes = leafSection,
                    )
                ),
                options = ArchiveOpenOptions.Lenient,
            )
        val coord = TileIds.toZxy(2)

        val range = archive.getTileRange(coord.z, coord.x, coord.y)

        assertEquals(ByteRange(401uL, 2), range?.archiveRange)
        assertEquals(2, range?.directoryDepth)
        assertEquals(1, archive.warningCount)
        assertEquals(ArchiveWarningCode.NestedLeafDirectory, archive.warningAt(0)?.code)
    }

    @Test
    fun strictModeRejectsNestedLeafDirectory() = runTest {
        val tileLeafBytes =
            encodeDirectory(DirectoryEntry(tileId = 2, offset = 1uL, length = 2, runLength = 1))
        val nestedLeafOffset = 20uL
        val firstLeafBytes =
            encodeDirectory(
                DirectoryEntry(
                    tileId = 2,
                    offset = nestedLeafOffset,
                    length = tileLeafBytes.size,
                    runLength = 0,
                )
            )
        val rootBytes =
            encodeDirectory(
                DirectoryEntry(
                    tileId = 1,
                    offset = 0uL,
                    length = firstLeafBytes.size,
                    runLength = 0,
                )
            )
        val leafSection = ByteArray(nestedLeafOffset.toInt() + tileLeafBytes.size)
        firstLeafBytes.copyInto(leafSection)
        tileLeafBytes.copyInto(leafSection, destinationOffset = nestedLeafOffset.toInt())
        val fields =
            TestHeaderFields(
                rootLength = rootBytes.size.toULong(),
                leafDirectoriesOffset = 200uL,
                leafDirectoriesLength = leafSection.size.toULong(),
                tileDataOffset = 400uL,
                tileDataLength = 3uL,
            )

        val error =
            assertFailsWith<PmTilesException> {
                val archive =
                    PmTilesArchive.open(
                        TestByteRangeSource(
                            buildArchiveWithLeafBytes(
                                fields = fields,
                                rootBytes = rootBytes,
                                leafBytes = leafSection,
                            )
                        )
                    )
                val coord = TileIds.toZxy(2)
                archive.getTileRange(coord.z, coord.x, coord.y)
            }

        assertEquals(PmTilesErrorCode.InvalidDirectory, error.code)
    }

    @Test
    fun failsWhenLeafDepthLimitIsExceeded() = runTest {
        val leafBytes =
            encodeDirectory(DirectoryEntry(tileId = 2, offset = 0uL, length = 1, runLength = 1))
        val rootBytes =
            encodeDirectory(
                DirectoryEntry(tileId = 1, offset = 0uL, length = leafBytes.size, runLength = 0)
            )
        val fields =
            TestHeaderFields(
                rootLength = rootBytes.size.toULong(),
                leafDirectoriesOffset = 200uL,
                leafDirectoriesLength = leafBytes.size.toULong(),
                tileDataOffset = 300uL,
                tileDataLength = 1uL,
            )
        val error =
            assertFailsWith<PmTilesException> {
                val archive =
                    PmTilesArchive.open(
                        TestByteRangeSource(
                            buildArchiveWithLeafBytes(
                                fields = fields,
                                rootBytes = rootBytes,
                                leafBytes = leafBytes,
                            )
                        ),
                        options =
                            ArchiveOpenOptions(
                                limits = ArchiveLimits.Default.copy(maxDirectoryDepth = 0)
                            ),
                    )
                val coord = TileIds.toZxy(2)
                archive.getTileRange(coord.z, coord.x, coord.y)
            }

        assertEquals(PmTilesErrorCode.LimitExceeded, error.code)
    }

    @Test
    fun failsWhenLookupRevisitsLeafRange() = runTest {
        val recursiveLeafBytes =
            encodeDirectory(DirectoryEntry(tileId = 0, offset = 0uL, length = 5, runLength = 0))
        val rootBytes =
            encodeDirectory(
                DirectoryEntry(
                    tileId = 0,
                    offset = 0uL,
                    length = recursiveLeafBytes.size,
                    runLength = 0,
                )
            )
        val fields =
            TestHeaderFields(
                rootLength = rootBytes.size.toULong(),
                leafDirectoriesOffset = 200uL,
                leafDirectoriesLength = recursiveLeafBytes.size.toULong(),
                tileDataOffset = 300uL,
                tileDataLength = 0uL,
            )
        val error =
            assertFailsWith<PmTilesException> {
                val archive =
                    PmTilesArchive.open(
                        TestByteRangeSource(
                            buildArchiveWithLeafBytes(
                                fields = fields,
                                rootBytes = rootBytes,
                                leafBytes = recursiveLeafBytes,
                            )
                        ),
                        options = ArchiveOpenOptions.Lenient,
                    )
                archive.getTileRange(0, 0, 0)
            }

        assertEquals(PmTilesErrorCode.LimitExceeded, error.code)
    }

    private fun buildArchiveWithLeafBytes(
        fields: TestHeaderFields,
        rootBytes: ByteArray,
        leafBytes: ByteArray,
    ): ByteArray =
        buildArchiveWithSections(fields = fields, rootBytes = rootBytes, leafBytes = leafBytes)
}
