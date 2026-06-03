package org.maplibre.spatialk.pmtiles

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.maplibre.spatialk.pmtiles.internal.buildArchive
import org.maplibre.spatialk.pmtiles.internal.runSuspending

class KotlinInteropJvmTest {
    @Test
    fun kotlinCallerCanUsePublicArchiveApi() = runSuspending {
        val source: ByteRangeSource = CallerByteRangeSource(buildArchive())
        val archive =
            PmTilesArchive.open(
                source = source,
                options = ArchiveOpenOptions(tileReadMode = TileReadMode.CompressedBytes),
            )

        assertEquals(3, archive.header.specVersion)
        assertEquals(127uL, archive.header.rootDirectory.offset)
        assertEquals(Compression.None, archive.internalCompression)
        assertEquals(TileType.Unknown, archive.tileType)
        assertEquals(Compression.None.code, archive.internalCompression.code)
        assertEquals(TileType.Unknown.code, archive.tileType.code)
        assertEquals(0, archive.warningCount)
        assertEquals(null, archive.warningAt(0))

        val coord = TileCoord(z = 0, x = 0, y = 0)
        val tileRange = assertNotNull(archive.getTileRange(coord.z, coord.x, coord.y))
        val tile = assertNotNull(archive.getTile(coord))
        assertEquals(TileIds.fromZxy(coord.z, coord.x, coord.y), tile.tileId)
        assertEquals(tileRange.archiveRange, tile.range.archiveRange)
        assertTrue(archive.containsTile(coord.z, coord.x, coord.y))

        val closeable: AutoCloseable = archive
        closeable.close()
    }

    @Test
    fun kotlinCallerReceivesPmTilesException() {
        val error =
            assertFailsWith<PmTilesException> {
                runSuspending {
                    PmTilesArchive.open(CallerByteRangeSource(ByteArray(4)))
                }
            }

        assertEquals(PmTilesErrorCode.InvalidHeader, error.code)
    }

    private class CallerByteRangeSource(private val bytes: ByteArray) : ByteRangeSource {
        override suspend fun size(): ULong = bytes.size.toULong()

        override suspend fun read(range: ByteRange): ByteArray {
            val start = range.offset.toInt()
            return bytes.copyOfRange(start, start + range.length)
        }
    }
}
