package org.maplibre.spatialk.pmtiles.internal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest
import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.encodeToByteString
import org.maplibre.spatialk.pmtiles.ArchiveWriteOptions
import org.maplibre.spatialk.pmtiles.ArchiveWriteTile
import org.maplibre.spatialk.pmtiles.CompressionCodes
import org.maplibre.spatialk.pmtiles.Compressor
import org.maplibre.spatialk.pmtiles.PmTilesErrorCodes
import org.maplibre.spatialk.pmtiles.PmTilesException
import org.maplibre.spatialk.pmtiles.TileCoord

class TileAssemblyTest {
    @Test
    fun computesFnv1a128Hash() {
        assertEquals(
            Fnv128Hash(high = 0x6c62272e07bb0142uL, low = 0x62b821756295c58duL),
            fnv1a128(ByteString()),
        )
        assertEquals(
            Fnv128Hash(high = 0xd228cb696f1a8cafuL, low = 0x78912b704e4a8964uL),
            fnv1a128("a".encodeToByteString()),
        )
        assertEquals(
            Fnv128Hash(high = 0x7fdba274300a175duL, low = 0x7a20922b340d0857uL),
            fnv1a128("hello pmtiles".encodeToByteString()),
        )
        assertEquals(
            Fnv128Hash(high = 0x62eed7b5e2757277uL, low = 0xb806e775e777e33buL),
            fnv1a128(ByteString(0x80.toByte(), 0xff.toByte(), 0x00, 0x01)),
        )
    }

    @Test
    fun sortsTilesByTileIdAndComputesContiguousOffsets() = runTest {
        val assembled =
            assembleTileData(
                listOf(
                    ArchiveWriteTile.stored(TileCoord(1, 0, 1), ByteString(4, 5)),
                    ArchiveWriteTile.stored(TileCoord(0, 0, 0), ByteString(1, 2, 3)),
                ),
                ArchiveWriteOptions(),
            )

        assertEquals(listOf(ByteString(1, 2, 3), ByteString(4, 5)), assembled.payloads)
        assertEquals(
            listOf(
                DirectoryEntry(tileId = 0, offset = 0uL, length = 3, runLength = 1),
                DirectoryEntry(tileId = 2, offset = 3uL, length = 2, runLength = 1),
            ),
            assembled.entries,
        )
        assertEquals(2uL, assembled.counts.addressedTiles)
        assertEquals(2uL, assembled.counts.tileEntries)
        assertEquals(2uL, assembled.counts.tileContents)
        assertEquals(5uL, assembled.length)
    }

    @Test
    fun rejectsDuplicateTileIds() = runTest {
        val error =
            assertFailsWith<PmTilesException> {
                assembleTileData(
                    listOf(
                        ArchiveWriteTile.stored(TileCoord(0, 0, 0), ByteString(1)),
                        ArchiveWriteTile.stored(TileCoord(0, 0, 0), ByteString(2)),
                    ),
                    ArchiveWriteOptions(),
                )
            }

        assertEquals(PmTilesErrorCodes.InvalidTileInput, error.code)
    }

    @Test
    fun uncompressedTilesUseRegisteredTileCompressor() = runTest {
        val assembled =
            assembleTileData(
                listOf(ArchiveWriteTile.uncompressed(TileCoord(0, 0, 0), ByteString(1, 2, 3))),
                ArchiveWriteOptions.build {
                    tileCompression = CompressionCodes.Brotli
                    compressor(CompressionCodes.Brotli, Compressor { _, _ -> ByteString(9, 8) })
                },
            )

        assertEquals(listOf(ByteString(9, 8)), assembled.payloads)
        assertEquals(2, assembled.entries.single().length)
    }

    @Test
    fun uncompressedTilesRejectUnsupportedTileCompression() = runTest {
        val error =
            assertFailsWith<PmTilesException> {
                assembleTileData(
                    listOf(ArchiveWriteTile.uncompressed(TileCoord(0, 0, 0), ByteString(1))),
                    ArchiveWriteOptions.build { tileCompression = CompressionCodes.Brotli },
                )
            }

        assertEquals(PmTilesErrorCodes.UnsupportedCompression, error.code)
    }

    @Test
    fun consecutiveDeduplicatedPayloadsBecomeRunLengthEntry() = runTest {
        val assembled =
            assembleTileData(
                listOf(
                    ArchiveWriteTile.stored(TileCoord(1, 0, 0), ByteString(7)),
                    ArchiveWriteTile.stored(TileCoord(1, 0, 1), ByteString(7)),
                ),
                ArchiveWriteOptions(),
            )

        assertEquals(listOf(ByteString(7)), assembled.payloads)
        assertEquals(
            listOf(DirectoryEntry(tileId = 1, offset = 0uL, length = 1, runLength = 2)),
            assembled.entries,
        )
        assertEquals(2uL, assembled.counts.addressedTiles)
        assertEquals(1uL, assembled.counts.tileEntries)
        assertEquals(1uL, assembled.counts.tileContents)
    }

    @Test
    fun nonConsecutiveDeduplicatedPayloadsPointToFirstStoredOffset() = runTest {
        val assembled =
            assembleTileData(
                listOf(
                    ArchiveWriteTile.stored(TileCoord(0, 0, 0), ByteString(7)),
                    ArchiveWriteTile.stored(TileCoord(1, 1, 0), ByteString(7)),
                ),
                ArchiveWriteOptions(),
            )

        assertEquals(listOf(ByteString(7)), assembled.payloads)
        assertEquals(
            listOf(
                DirectoryEntry(tileId = 0, offset = 0uL, length = 1, runLength = 1),
                DirectoryEntry(tileId = 4, offset = 0uL, length = 1, runLength = 1),
            ),
            assembled.entries,
        )
        assertEquals(2uL, assembled.counts.addressedTiles)
        assertEquals(2uL, assembled.counts.tileEntries)
        assertEquals(1uL, assembled.counts.tileContents)
    }

    @Test
    fun deduplicationCanBeDisabled() = runTest {
        val assembled =
            assembleTileData(
                listOf(
                    ArchiveWriteTile.stored(TileCoord(1, 0, 0), ByteString(7)),
                    ArchiveWriteTile.stored(TileCoord(1, 0, 1), ByteString(7)),
                ),
                ArchiveWriteOptions.build { deduplicateTilePayloads = false },
            )

        assertEquals(listOf(ByteString(7), ByteString(7)), assembled.payloads)
        assertEquals(
            listOf(
                DirectoryEntry(tileId = 1, offset = 0uL, length = 1, runLength = 1),
                DirectoryEntry(tileId = 2, offset = 1uL, length = 1, runLength = 1),
            ),
            assembled.entries,
        )
        assertEquals(2uL, assembled.counts.tileContents)
    }

    @Test
    fun deduplicatedUncompressedTilesReuseFirstStoredPayloadWhenFinalBytesMatch() = runTest {
        var compressionCalls = 0
        val assembled =
            assembleTileData(
                listOf(
                    ArchiveWriteTile.uncompressed(TileCoord(1, 0, 0), ByteString(7)),
                    ArchiveWriteTile.uncompressed(TileCoord(1, 0, 1), ByteString(7)),
                ),
                ArchiveWriteOptions.build {
                    tileCompression = CompressionCodes.Brotli
                    compressor(
                        CompressionCodes.Brotli,
                        Compressor { _, _ ->
                            compressionCalls += 1
                            ByteString(1, 2)
                        },
                    )
                },
            )

        assertEquals(2, compressionCalls)
        assertEquals(listOf(ByteString(1, 2)), assembled.payloads)
        assertEquals(2, assembled.entries.single().runLength)
    }

    @Test
    fun deduplicatesMixedPayloadModesOnlyAfterStoredBytesAreComputed() = runTest {
        val assembled =
            assembleTileData(
                listOf(
                    ArchiveWriteTile.stored(TileCoord(0, 0, 0), ByteString(7)),
                    ArchiveWriteTile.uncompressed(TileCoord(1, 0, 0), ByteString(7)),
                ),
                ArchiveWriteOptions.build {
                    tileCompression = CompressionCodes.Brotli
                    compressor(CompressionCodes.Brotli, Compressor { _, _ -> ByteString(9, 8) })
                },
            )

        assertEquals(listOf(ByteString(7), ByteString(9, 8)), assembled.payloads)
        assertEquals(
            listOf(
                DirectoryEntry(tileId = 0, offset = 0uL, length = 1, runLength = 1),
                DirectoryEntry(tileId = 1, offset = 1uL, length = 2, runLength = 1),
            ),
            assembled.entries,
        )
    }
}
