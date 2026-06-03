package org.maplibre.spatialk.pmtiles.internal

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.maplibre.spatialk.pmtiles.Compression
import org.maplibre.spatialk.pmtiles.PmTilesErrorCode
import org.maplibre.spatialk.pmtiles.PmTilesException

class GzipNativeTest {
    @Test
    fun decodesGzip() {
        val decoded = decodeCompression(Compression.Gzip, helloGzipBytes, testDecodeLimits())

        assertContentEquals(helloBytes, decoded)
    }

    @Test
    fun decodesEmptyGzip() {
        val decoded = decodeCompression(Compression.Gzip, emptyGzipBytes, testDecodeLimits())

        assertContentEquals(ByteArray(0), decoded)
    }

    @Test
    fun rejectsTruncatedGzip() {
        val error =
            assertFailsWith<PmTilesException> {
                decodeCompression(Compression.Gzip, helloGzipBytes.copyOf(12), testDecodeLimits())
            }

        assertEquals(PmTilesErrorCode.DecompressionFailed, error.code)
    }

    @Test
    fun rejectsInvalidGzip() {
        val error =
            assertFailsWith<PmTilesException> {
                decodeCompression(Compression.Gzip, byteArrayOf(1, 2, 3), testDecodeLimits())
            }

        assertEquals(PmTilesErrorCode.DecompressionFailed, error.code)
    }

    @Test
    fun enforcesDecompressedLimit() {
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
}
