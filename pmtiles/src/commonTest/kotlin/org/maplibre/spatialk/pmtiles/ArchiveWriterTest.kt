package org.maplibre.spatialk.pmtiles

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlinx.coroutines.test.runTest
import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.buildByteString
import org.maplibre.spatialk.pmtiles.internal.TestByteRangeSource

class ArchiveWriterTest {
    @Test
    fun writesMinimalArchiveInMemoryAndReaderCanOpenIt() = runTest {
        val tilePayload = ByteString(0x89.toByte(), 0x50, 0x4e, 0x47)
        val bytes =
            PmTiles.writeToByteString(
                tiles = listOf(ArchiveWriteTile.stored(TileCoord(0, 0, 0), tilePayload)),
                config =
                    ArchiveWriteConfig.build {
                        tileType = TileTypeCodes.Png
                        bounds =
                            ArchiveWriteBounds(west = -1.0, south = -2.0, east = 3.0, north = 4.0)
                        center = ArchiveWriteCenter(longitude = 1.0, latitude = 2.0, zoom = 0)
                        metadataJson = """{"name":"demo"}"""
                    },
            )

        val archive = PmTiles.open(TestByteRangeSource(bytes))
        val tile = assertNotNull(archive.readStoredTile(0, 0, 0))

        assertEquals(TileTypeCodes.Png, archive.tileType)
        assertEquals(CompressionCodes.None, archive.internalCompression)
        assertEquals(CompressionCodes.None, archive.tileCompression)
        assertEquals(1uL, archive.header.counts.addressedTiles)
        assertEquals(1uL, archive.header.counts.tileEntries)
        assertEquals(1uL, archive.header.counts.tileContents)
        assertEquals("""{"name":"demo"}""", archive.rawMetadataJson())
        assertEquals(tilePayload, tile.payload)
        assertEquals(false, tile.wasDecompressed)
    }

    @Test
    fun writesToByteSinkAndClosesAfterFlush() = runTest {
        val sink = CollectingByteSink()
        PmTiles.write(
            sink = sink,
            tiles = listOf(ArchiveWriteTile.stored(TileCoord(0, 0, 0), ByteString(1, 2, 3))),
        )

        assertEquals(true, sink.flushed)
        assertEquals(true, sink.closed)
        assertEquals(
            sink.bytes,
            PmTiles.writeToByteString(
                listOf(ArchiveWriteTile.stored(TileCoord(0, 0, 0), ByteString(1, 2, 3)))
            ),
        )
    }

    @Test
    fun writeWrapsUnexpectedSinkFailures() = runTest {
        val error =
            assertFailsWith<PmTilesException> {
                PmTiles.write(
                    sink = FailingByteSink(),
                    tiles = listOf(ArchiveWriteTile.stored(TileCoord(0, 0, 0), ByteString(1))),
                )
            }

        assertEquals(PmTilesErrorCode.SinkUnavailable, error.code)
    }

    @Test
    fun writeFailsBeforeSinkReceivesBytesWhenCompressionUnsupported() = runTest {
        val sink = CollectingByteSink()
        val error =
            assertFailsWith<PmTilesException> {
                PmTiles.write(
                    sink = sink,
                    tiles =
                        listOf(ArchiveWriteTile.uncompressed(TileCoord(0, 0, 0), ByteString(1))),
                    options =
                        ArchiveWriteOptions.build { tileCompression = CompressionCodes.Brotli },
                )
            }

        assertEquals(PmTilesErrorCode.UnsupportedCompression, error.code)
        assertEquals(ByteString(), sink.bytes)
        assertEquals(false, sink.flushed)
        assertEquals(false, sink.closed)
    }
}

private class CollectingByteSink : ByteSink {
    private val chunks = mutableListOf<ByteString>()
    var flushed = false
    var closed = false

    val bytes: ByteString
        get() =
            buildByteString(chunks.sumOf { it.size }) {
                chunks.forEach { append(it.toByteArray()) }
            }

    override suspend fun write(bytes: ByteString) {
        chunks += bytes
    }

    override suspend fun flush() {
        flushed = true
    }

    override suspend fun close() {
        closed = true
    }
}

private class FailingByteSink : ByteSink {
    override suspend fun write(bytes: ByteString) {
        error("write failed")
    }

    override suspend fun flush() = Unit

    override suspend fun close() = Unit
}
