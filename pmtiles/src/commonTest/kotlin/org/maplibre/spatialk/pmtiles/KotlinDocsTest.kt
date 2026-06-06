@file:Suppress("UnusedVariable", "unused")

package org.maplibre.spatialk.pmtiles

import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import kotlinx.io.bytestring.ByteString
import org.maplibre.spatialk.pmtiles.internal.buildSingleTileArchive
import org.maplibre.spatialk.testutil.readResourceBytes

// These snippets are primarily intended to be included in documentation. Though they exist as
// part of the test suite, they are not intended to be comprehensive tests.

class KotlinDocsTest {
    @Test
    fun byteRangeSource() = runTest {
        val pmTilesBytes = loadPmTilesBytes()

        // --8<-- [start:byteRangeSource]
        fun ByteString.asByteRangeSource(): ByteRangeSource =
            object : ByteRangeSource {
                override suspend fun size(): ULong = this@asByteRangeSource.size.toULong()

                override suspend fun read(range: ByteRange): ByteString {
                    val start = range.offset.toInt()
                    val length = range.length.toInt()
                    return this@asByteRangeSource.substring(start, start + length)
                }
            }
        // --8<-- [end:byteRangeSource]

        val source = pmTilesBytes.asByteRangeSource()
        PmTiles.open(source).close()
    }

    @Test
    fun openArchive() = runTest {
        val source = loadPmTilesBytes().asByteRangeSource()

        // --8<-- [start:openArchive]
        PmTiles.open(source).use { archive ->
            val header = archive.header
            val metadata = archive.metadata()
            val tile = archive.readStoredTile(z = 0, x = 0, y = 0)
            val tileRange = archive.findTileRange(z = 0, x = 0, y = 0)
        }
        // --8<-- [end:openArchive]
    }

    @Test
    fun decompressedTiles() = runTest {
        val source = loadPmTilesBytes().asByteRangeSource()

        // --8<-- [start:decompressedTiles]
        PmTiles.open(source).use { archive ->
            val tile = archive.readDecompressedTile(z = 0, x = 0, y = 0)
        }
        // --8<-- [end:decompressedTiles]
    }

    @Test
    fun batchTiles() = runTest {
        val source = readFixture("go-pmtiles-unclustered.pmtiles").asByteRangeSource()

        // --8<-- [start:batchTiles]
        PmTiles.open(source).use { archive ->
            val coords =
                listOf(
                    TileCoord(z = 1, x = 0, y = 0),
                    TileCoord(z = 1, x = 0, y = 1),
                )
            val results = archive.readStoredTiles(coords)
        }
        // --8<-- [end:batchTiles]
    }

    @Test
    fun customDecompressor() = runTest {
        val source = loadPmTilesBytes().asByteRangeSource()

        // --8<-- [start:customDecompressor]
        val options = ArchiveOpenOptions.build {
            decompressor(CompressionCodes.Brotli) { bytes, limits ->
                val decoded = decodeBrotli(bytes)
                if (decoded.size.toULong() > limits.maxDecompressedBytes) {
                    throw PmTilesException(
                        PmTilesErrorCode.LimitExceeded,
                        "Decoded output exceeds ${limits.maxDecompressedBytes} bytes.",
                    )
                }
                decoded
            }
        }

        PmTiles.open(source, options).use { archive ->
            val tile = archive.readDecompressedTile(z = 0, x = 0, y = 0)
        }
        // --8<-- [end:customDecompressor]
    }

    @Test
    fun lenientWarnings() = runTest {
        val source = readFixture("pmtiles-js-test-fixture-mlt.pmtiles").asByteRangeSource()

        // --8<-- [start:lenientWarnings]
        val options = ArchiveOpenOptions.build { validationMode = ValidationMode.Lenient }

        PmTiles.open(source, options).use { archive ->
            val warnings = archive.warnings
        }
        // --8<-- [end:lenientWarnings]
    }
}

private fun loadPmTilesBytes(): ByteString = buildSingleTileArchive(tileBytes = ByteString(1, 2, 3))

private fun readFixture(path: String): ByteString = ByteString(readResourceBytes(path))

private fun decodeBrotli(bytes: ByteString): ByteString = bytes

private fun ByteString.asByteRangeSource(): ByteRangeSource =
    object : ByteRangeSource {
        override suspend fun size(): ULong = this@asByteRangeSource.size.toULong()

        override suspend fun read(range: ByteRange): ByteString {
            val start = range.offset.toInt()
            val length = range.length.toInt()
            return this@asByteRangeSource.substring(start, start + length)
        }
    }
