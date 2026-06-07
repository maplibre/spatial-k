package org.maplibre.spatialk.pmtiles.internal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.io.bytestring.hexToByteString
import org.maplibre.spatialk.pmtiles.ArchiveHeader
import org.maplibre.spatialk.pmtiles.ArchiveSection
import org.maplibre.spatialk.pmtiles.CompressionCodes
import org.maplibre.spatialk.pmtiles.HeaderCounts
import org.maplibre.spatialk.pmtiles.LonLatBounds
import org.maplibre.spatialk.pmtiles.PmTilesErrorCodes
import org.maplibre.spatialk.pmtiles.PmTilesException
import org.maplibre.spatialk.pmtiles.TileCenter
import org.maplibre.spatialk.pmtiles.TileTypeCodes

class DirectoryEncodingTest {
    @Test
    fun encodesExplicitOffsets() {
        val bytes =
            encodeDirectory(
                listOf(
                    DirectoryEntry(tileId = 0, offset = 0uL, length = 2, runLength = 1),
                    DirectoryEntry(tileId = 5, offset = 10uL, length = 3, runLength = 1),
                )
            )

        assertEquals("02000501010203010b".hexToByteString(), bytes)
    }

    @Test
    fun encodesContiguousOffsetShorthand() {
        val bytes =
            encodeDirectory(
                listOf(
                    DirectoryEntry(tileId = 0, offset = 0uL, length = 2, runLength = 1),
                    DirectoryEntry(tileId = 1, offset = 2uL, length = 3, runLength = 1),
                )
            )

        assertEquals("020001010102030100".hexToByteString(), bytes)
    }

    @Test
    fun encodesRunLengthAndLeafEntries() {
        val bytes =
            encodeDirectory(
                listOf(
                    DirectoryEntry(tileId = 5, offset = 0uL, length = 4, runLength = 3),
                    DirectoryEntry(tileId = 10, offset = 0uL, length = 6, runLength = 0),
                )
            )

        assertEquals("020505030004060101".hexToByteString(), bytes)
    }

    @Test
    fun encodedDirectoryRoundTripsThroughDecoder() {
        val input =
            listOf(
                DirectoryEntry(tileId = 0, offset = 0uL, length = 2, runLength = 1),
                DirectoryEntry(tileId = 1, offset = 2uL, length = 3, runLength = 1),
                DirectoryEntry(tileId = 10, offset = 0uL, length = 6, runLength = 0),
            )

        val decoded =
            decodeDirectory(
                encodeDirectory(input),
                header(tileDataLength = 5uL, leafDirectoriesLength = 6uL),
                limits = org.maplibre.spatialk.pmtiles.ArchiveLimits(),
            )

        assertEquals(input, decoded)
    }

    @Test
    fun rejectsInvalidEntries() {
        listOf(
                emptyList(),
                listOf(DirectoryEntry(tileId = -1, offset = 0uL, length = 1, runLength = 1)),
                listOf(
                    DirectoryEntry(tileId = 1, offset = 0uL, length = 1, runLength = 1),
                    DirectoryEntry(tileId = 1, offset = 1uL, length = 1, runLength = 1),
                ),
                listOf(DirectoryEntry(tileId = 0, offset = 0uL, length = 0, runLength = 1)),
                listOf(DirectoryEntry(tileId = 0, offset = 0uL, length = 1, runLength = -1)),
                listOf(
                    DirectoryEntry(
                        tileId = 0,
                        offset = ULong.MAX_VALUE,
                        length = 1,
                        runLength = 1,
                    )
                ),
            )
            .forEach { entries ->
                val error = assertFailsWith<PmTilesException> { encodeDirectory(entries) }

                assertEquals(PmTilesErrorCodes.InvalidDirectory, error.code)
            }
    }

    private fun header(tileDataLength: ULong, leafDirectoriesLength: ULong): ArchiveHeader =
        ArchiveHeader(
            specVersion = 3,
            rootDirectory = ArchiveSection(offset = 127uL, length = 1uL),
            metadata = ArchiveSection(offset = 0uL, length = 0uL),
            leafDirectories = ArchiveSection(offset = 200uL, length = leafDirectoriesLength),
            tileData = ArchiveSection(offset = 300uL, length = tileDataLength),
            counts = HeaderCounts(addressedTiles = 1uL, tileEntries = 1uL, tileContents = 1uL),
            isClustered = true,
            internalCompression = CompressionCodes.None,
            tileCompression = CompressionCodes.None,
            tileType = TileTypeCodes.Unknown,
            minZoom = 0,
            maxZoom = 0,
            bounds = LonLatBounds(west = 0.0, south = 0.0, east = 0.0, north = 0.0),
            center = TileCenter(longitude = 0.0, latitude = 0.0, zoom = 0),
        )
}
