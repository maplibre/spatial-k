package org.maplibre.spatialk.pmtiles

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.maplibre.spatialk.pmtiles.internal.TestByteRangeSource
import org.maplibre.spatialk.pmtiles.internal.buildSingleTileArchive
import org.maplibre.spatialk.pmtiles.internal.helloBytes
import org.maplibre.spatialk.pmtiles.internal.helloGzipBytes
import org.maplibre.spatialk.pmtiles.internal.runSuspending

class TileGzipNativeTest {
    @Test
    fun decompressedModeDecodesGzipTiles() = runSuspending {
        val archive =
            PmTilesArchive.open(
                TestByteRangeSource(
                    buildSingleTileArchive(
                        tileBytes = helloGzipBytes,
                        tileCompression = Compression.Gzip.code,
                    )
                ),
                options = ArchiveOpenOptions(tileReadMode = TileReadMode.DecompressedBytes),
            )

        val tile = archive.getTile(0, 0, 0)

        requireNotNull(tile)
        assertContentEquals(helloBytes, tile.bytes)
        assertEquals(Compression.None, tile.compression)
        assertEquals(Compression.Gzip, tile.range.compression)
        assertEquals(true, tile.wasDecompressed)
    }

    @Test
    fun compressedBombFails() {
        val error =
            assertFailsWith<PmTilesException> {
                runSuspending {
                    val archive =
                        PmTilesArchive.open(
                            TestByteRangeSource(
                                buildSingleTileArchive(
                                    tileBytes = helloGzipBytes,
                                    tileCompression = Compression.Gzip.code,
                                )
                            ),
                            options =
                                ArchiveOpenOptions(
                                    tileReadMode = TileReadMode.DecompressedBytes,
                                    limits =
                                        ArchiveLimits.Default.copy(
                                            maxTileDecompressedBytes = helloBytes.size - 1
                                        ),
                                ),
                        )
                    archive.getTile(0, 0, 0)
                }
            }

        assertEquals(PmTilesErrorCode.LimitExceeded, error.code)
    }
}
