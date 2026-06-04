package org.maplibre.spatialk.pmtiles.internal

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest
import org.maplibre.spatialk.pmtiles.Compression
import org.maplibre.spatialk.pmtiles.PmTilesErrorCode
import org.maplibre.spatialk.pmtiles.PmTilesException

class CompressionTest {
    @Test
    fun decodesNone() = runTest {
        val decoded = decodeCompression(Compression.None, helloBytes, testDecodeLimits())

        assertContentEquals(helloBytes, decoded)
    }

    @Test
    fun noneEnforcesCompressedLimit() = runTest {
        val error =
            assertFailsWith<PmTilesException> {
                decodeCompression(
                    Compression.None,
                    helloBytes,
                    testDecodeLimits(maxCompressedBytes = helloBytes.size - 1),
                )
            }

        assertEquals(PmTilesErrorCode.LimitExceeded, error.code)
    }

    @Test
    fun noneEnforcesDecompressedLimit() = runTest {
        val error =
            assertFailsWith<PmTilesException> {
                decodeCompression(
                    Compression.None,
                    helloBytes,
                    testDecodeLimits(maxDecompressedBytes = helloBytes.size - 1),
                )
            }

        assertEquals(PmTilesErrorCode.LimitExceeded, error.code)
    }

    @Test
    fun decodesGzip() = runTest {
        val decoded = decodeCompression(Compression.Gzip, helloGzipBytes, testDecodeLimits())

        assertContentEquals(helloBytes, decoded)
    }

    @Test
    fun decodesEmptyGzip() = runTest {
        val decoded = decodeCompression(Compression.Gzip, emptyGzipBytes, testDecodeLimits())

        assertContentEquals(ByteArray(0), decoded)
    }

    @Test
    fun rejectsTruncatedGzip() = runTest {
        val error =
            assertFailsWith<PmTilesException> {
                decodeCompression(Compression.Gzip, helloGzipBytes.copyOf(12), testDecodeLimits())
            }

        assertEquals(PmTilesErrorCode.DecompressionFailed, error.code)
    }

    @Test
    fun rejectsInvalidGzip() = runTest {
        val error =
            assertFailsWith<PmTilesException> {
                decodeCompression(Compression.Gzip, byteArrayOf(1, 2, 3), testDecodeLimits())
            }

        assertEquals(PmTilesErrorCode.DecompressionFailed, error.code)
    }

    @Test
    fun gzipEnforcesDecompressedLimit() = runTest {
        val error =
            assertFailsWith<PmTilesException> {
                decodeCompression(
                    Compression.Gzip,
                    helloGzipBytes,
                    testDecodeLimits(maxDecompressedBytes = helloBytes.size - 1),
                )
            }

        assertEquals(PmTilesErrorCode.LimitExceeded, error.code)
    }

    @Test
    fun rejectsUnsupportedCompressionWhenDecodeIsRequired() = runTest {
        listOf(
                Compression.Unknown,
                Compression.Brotli,
                Compression.Zstd,
                Compression(99u),
            )
            .forEach { compression ->
                val error =
                    assertFailsWith<PmTilesException> {
                        decodeCompression(compression, helloBytes, testDecodeLimits())
                    }

                assertEquals(PmTilesErrorCode.UnsupportedCompression, error.code)
            }
    }

    @Test
    fun rejectsNegativeDecodeLimits() = runTest {
        val error =
            assertFailsWith<PmTilesException> {
                decodeCompression(
                    Compression.None,
                    helloBytes,
                    testDecodeLimits(maxCompressedBytes = -1),
                )
            }

        assertEquals(PmTilesErrorCode.LimitExceeded, error.code)
    }
}
