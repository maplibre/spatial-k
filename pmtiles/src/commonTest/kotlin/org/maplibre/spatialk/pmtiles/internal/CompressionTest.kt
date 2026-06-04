package org.maplibre.spatialk.pmtiles.internal

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest
import org.maplibre.spatialk.pmtiles.Compression
import org.maplibre.spatialk.pmtiles.DecompressionLimits
import org.maplibre.spatialk.pmtiles.Decompressor
import org.maplibre.spatialk.pmtiles.PmTilesErrorCode
import org.maplibre.spatialk.pmtiles.PmTilesException

class CompressionTest {
    @Test
    fun decodesNone() = runTest {
        val decoded =
            platformDefaultDecompressors()
                .decompress(
                    Compression.None,
                    helloBytes,
                    testDecodeLimits(),
                )

        assertContentEquals(helloBytes, decoded)
    }

    @Test
    fun noneEnforcesCompressedLimit() = runTest {
        val error =
            assertFailsWith<PmTilesException> {
                platformDefaultDecompressors()
                    .decompress(
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
                platformDefaultDecompressors()
                    .decompress(
                        Compression.None,
                        helloBytes,
                        testDecodeLimits(maxDecompressedBytes = helloBytes.size - 1),
                    )
            }

        assertEquals(PmTilesErrorCode.LimitExceeded, error.code)
    }

    @Test
    fun decodesGzip() = runTest {
        val decoded =
            platformDefaultDecompressors()
                .decompress(
                    Compression.Gzip,
                    helloGzipBytes,
                    testDecodeLimits(),
                )

        assertContentEquals(helloBytes, decoded)
    }

    @Test
    fun decodesEmptyGzip() = runTest {
        val decoded =
            platformDefaultDecompressors()
                .decompress(
                    Compression.Gzip,
                    emptyGzipBytes,
                    testDecodeLimits(),
                )

        assertContentEquals(ByteArray(0), decoded)
    }

    @Test
    fun rejectsTruncatedGzip() = runTest {
        val error =
            assertFailsWith<PmTilesException> {
                platformDefaultDecompressors()
                    .decompress(
                        Compression.Gzip,
                        helloGzipBytes.copyOf(12),
                        testDecodeLimits(),
                    )
            }

        assertEquals(PmTilesErrorCode.DecompressionFailed, error.code)
    }

    @Test
    fun rejectsInvalidGzip() = runTest {
        val error =
            assertFailsWith<PmTilesException> {
                platformDefaultDecompressors()
                    .decompress(
                        Compression.Gzip,
                        byteArrayOf(1, 2, 3),
                        testDecodeLimits(),
                    )
            }

        assertEquals(PmTilesErrorCode.DecompressionFailed, error.code)
    }

    @Test
    fun gzipEnforcesDecompressedLimit() = runTest {
        val error =
            assertFailsWith<PmTilesException> {
                platformDefaultDecompressors()
                    .decompress(
                        Compression.Gzip,
                        helloGzipBytes,
                        testDecodeLimits(maxDecompressedBytes = helloBytes.size - 1),
                    )
            }

        assertEquals(PmTilesErrorCode.LimitExceeded, error.code)
    }

    @Test
    fun gzipBombFailsOnLimitBeforeTrailingCorruption() = runTest {
        val error =
            assertFailsWith<PmTilesException> {
                platformDefaultDecompressors()
                    .decompress(
                        Compression.Gzip,
                        truncatedGzipBombBytes(decompressedBytes = 4096),
                        testDecodeLimits(maxDecompressedBytes = 1024),
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
                        platformDefaultDecompressors()
                            .decompress(
                                compression,
                                helloBytes,
                                testDecodeLimits(),
                            )
                    }

                assertEquals(PmTilesErrorCode.UnsupportedCompression, error.code)
            }
    }

    @Test
    fun rejectsNegativeDecodeLimits() = runTest {
        val error =
            assertFailsWith<PmTilesException> {
                platformDefaultDecompressors()
                    .decompress(
                        Compression.None,
                        helloBytes,
                        testDecodeLimits(maxCompressedBytes = -1),
                    )
            }

        assertEquals(PmTilesErrorCode.LimitExceeded, error.code)
    }

    @Test
    fun customDecompressorsOverrideDefaultEntriesByCompressionCode() = runTest {
        val decodedBytes = byteArrayOf(4, 5, 6)
        val decompressors =
            platformDefaultDecompressors() +
                mapOf(Compression.None to Decompressor { _, _ -> decodedBytes })

        val decoded =
            decompressors.decompress(
                Compression.None,
                byteArrayOf(1, 2, 3),
                DecompressionLimits(maxCompressedBytes = 3, maxDecompressedBytes = 3),
            )

        assertContentEquals(decodedBytes, decoded)
        assertEquals(true, Compression.Gzip in decompressors)
    }

    @Test
    fun customDecompressorsCanAddCompressionCodes() = runTest {
        val decodedBytes = byteArrayOf(4, 5, 6)
        val decompressors =
            platformDefaultDecompressors() +
                mapOf(Compression.Brotli to Decompressor { _, _ -> decodedBytes })

        val decoded =
            decompressors.decompress(
                Compression.Brotli,
                byteArrayOf(1, 2, 3),
                DecompressionLimits(maxCompressedBytes = 3, maxDecompressedBytes = 3),
            )

        assertContentEquals(decodedBytes, decoded)
        assertEquals(true, Compression.Brotli in decompressors)
    }

    @Test
    fun registryEnforcesCustomDecompressedLimit() = runTest {
        val decompressors = mapOf(Compression.Brotli to Decompressor { _, _ -> byteArrayOf(1, 2) })

        val error =
            assertFailsWith<PmTilesException> {
                decompressors.decompress(
                    Compression.Brotli,
                    byteArrayOf(1),
                    DecompressionLimits(maxCompressedBytes = 1, maxDecompressedBytes = 1),
                )
            }

        assertEquals(PmTilesErrorCode.LimitExceeded, error.code)
    }
}
