package org.maplibre.spatialk.pmtiles.internal

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest
import org.maplibre.spatialk.pmtiles.CompressionCode
import org.maplibre.spatialk.pmtiles.CompressionCodes
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
                    CompressionCodes.None,
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
                        CompressionCodes.None,
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
                        CompressionCodes.None,
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
                    CompressionCodes.Gzip,
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
                    CompressionCodes.Gzip,
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
                        CompressionCodes.Gzip,
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
                        CompressionCodes.Gzip,
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
                        CompressionCodes.Gzip,
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
                        CompressionCodes.Gzip,
                        truncatedGzipBombBytes(decompressedBytes = 4096),
                        testDecodeLimits(maxDecompressedBytes = 1024),
                    )
            }

        assertEquals(PmTilesErrorCode.LimitExceeded, error.code)
    }

    @Test
    fun rejectsUnsupportedCompressionWhenDecodeIsRequired() = runTest {
        listOf(
                CompressionCodes.Unknown,
                CompressionCodes.Brotli,
                CompressionCodes.Zstd,
                CompressionCode(99u),
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
    fun rejectsDecodeLimitsAboveSupportedAllocationRange() = runTest {
        val error =
            assertFailsWith<PmTilesException> {
                platformDefaultDecompressors()
                    .decompress(
                        CompressionCodes.None,
                        helloBytes,
                        DecodeLimits(
                            maxCompressedBytes = Int.MAX_VALUE.toULong() + 1uL,
                            maxDecompressedBytes = 1024uL,
                            purpose = DecodePurpose.Metadata,
                        ),
                    )
            }

        assertEquals(PmTilesErrorCode.LimitExceeded, error.code)
    }

    @Test
    fun customDecompressorsOverrideDefaultEntriesByCompressionCode() = runTest {
        val decodedBytes = byteArrayOf(4, 5, 6)
        val decompressors =
            platformDefaultDecompressors() +
                mapOf(CompressionCodes.None to Decompressor { _, _ -> decodedBytes })

        val decoded =
            decompressors.decompress(
                CompressionCodes.None,
                byteArrayOf(1, 2, 3),
                DecompressionLimits(maxCompressedBytes = 3, maxDecompressedBytes = 3),
            )

        assertContentEquals(decodedBytes, decoded)
        assertEquals(true, CompressionCodes.Gzip in decompressors)
    }

    @Test
    fun customDecompressorsCanAddCompressionCodes() = runTest {
        val decodedBytes = byteArrayOf(4, 5, 6)
        val decompressors =
            platformDefaultDecompressors() +
                mapOf(CompressionCodes.Brotli to Decompressor { _, _ -> decodedBytes })

        val decoded =
            decompressors.decompress(
                CompressionCodes.Brotli,
                byteArrayOf(1, 2, 3),
                DecompressionLimits(maxCompressedBytes = 3, maxDecompressedBytes = 3),
            )

        assertContentEquals(decodedBytes, decoded)
        assertEquals(true, CompressionCodes.Brotli in decompressors)
    }

    @Test
    fun registryEnforcesCustomDecompressedLimit() = runTest {
        val decompressors =
            mapOf(CompressionCodes.Brotli to Decompressor { _, _ -> byteArrayOf(1, 2) })

        val error =
            assertFailsWith<PmTilesException> {
                decompressors.decompress(
                    CompressionCodes.Brotli,
                    byteArrayOf(1),
                    DecompressionLimits(maxCompressedBytes = 1, maxDecompressedBytes = 1),
                )
            }

        assertEquals(PmTilesErrorCode.LimitExceeded, error.code)
    }
}
