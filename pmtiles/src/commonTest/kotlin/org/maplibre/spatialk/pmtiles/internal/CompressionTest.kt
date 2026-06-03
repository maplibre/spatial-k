package org.maplibre.spatialk.pmtiles.internal

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.maplibre.spatialk.pmtiles.Compression
import org.maplibre.spatialk.pmtiles.PmTilesErrorCode
import org.maplibre.spatialk.pmtiles.PmTilesException

class CompressionTest {
    @Test
    fun decodesNone() {
        val decoded = decodeCompression(Compression.None, helloBytes, testDecodeLimits())

        assertContentEquals(helloBytes, decoded)
    }

    @Test
    fun noneEnforcesCompressedLimit() {
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
    fun noneEnforcesDecompressedLimit() {
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
    fun rejectsUnsupportedCompressionWhenDecodeIsRequired() {
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
    fun rejectsNegativeDecodeLimits() {
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
