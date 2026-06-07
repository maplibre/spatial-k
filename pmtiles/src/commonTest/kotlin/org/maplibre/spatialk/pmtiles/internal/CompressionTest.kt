package org.maplibre.spatialk.pmtiles.internal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest
import kotlinx.io.bytestring.ByteString
import org.maplibre.spatialk.pmtiles.CompressionCode
import org.maplibre.spatialk.pmtiles.CompressionCodes
import org.maplibre.spatialk.pmtiles.CompressionLimits
import org.maplibre.spatialk.pmtiles.Compressor
import org.maplibre.spatialk.pmtiles.DecompressionLimits
import org.maplibre.spatialk.pmtiles.Decompressor
import org.maplibre.spatialk.pmtiles.PmTilesErrorCodes
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

        assertEquals(helloBytes, decoded)
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

        assertEquals(PmTilesErrorCodes.LimitExceeded, error.code)
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

        assertEquals(PmTilesErrorCodes.LimitExceeded, error.code)
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

        assertEquals(helloBytes, decoded)
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

        assertEquals(ByteString(), decoded)
    }

    @Test
    fun rejectsTruncatedGzip() = runTest {
        val error =
            assertFailsWith<PmTilesException> {
                platformDefaultDecompressors()
                    .decompress(
                        CompressionCodes.Gzip,
                        helloGzipBytes.substring(0, 12),
                        testDecodeLimits(),
                    )
            }

        assertEquals(PmTilesErrorCodes.DecompressionFailed, error.code)
    }

    @Test
    fun rejectsInvalidGzip() = runTest {
        val error =
            assertFailsWith<PmTilesException> {
                platformDefaultDecompressors()
                    .decompress(
                        CompressionCodes.Gzip,
                        ByteString(1, 2, 3),
                        testDecodeLimits(),
                    )
            }

        assertEquals(PmTilesErrorCodes.DecompressionFailed, error.code)
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

        assertEquals(PmTilesErrorCodes.LimitExceeded, error.code)
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

        assertEquals(PmTilesErrorCodes.LimitExceeded, error.code)
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

                assertEquals(PmTilesErrorCodes.UnsupportedCompression, error.code)
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

        assertEquals(PmTilesErrorCodes.LimitExceeded, error.code)
    }

    @Test
    fun customDecompressorsOverrideDefaultEntriesByCompressionCode() = runTest {
        val decodedBytes = ByteString(4, 5, 6)
        val decompressors =
            platformDefaultDecompressors() +
                mapOf(CompressionCodes.None to Decompressor { _, _ -> decodedBytes })

        val decoded =
            decompressors.decompress(
                CompressionCodes.None,
                ByteString(1, 2, 3),
                DecompressionLimits(maxCompressedBytes = 3uL, maxDecompressedBytes = 3uL),
            )

        assertEquals(decodedBytes, decoded)
        assertEquals(true, CompressionCodes.Gzip in decompressors)
    }

    @Test
    fun customDecompressorsCanAddCompressionCodes() = runTest {
        val decodedBytes = ByteString(4, 5, 6)
        val decompressors =
            platformDefaultDecompressors() +
                mapOf(CompressionCodes.Brotli to Decompressor { _, _ -> decodedBytes })

        val decoded =
            decompressors.decompress(
                CompressionCodes.Brotli,
                ByteString(1, 2, 3),
                DecompressionLimits(maxCompressedBytes = 3uL, maxDecompressedBytes = 3uL),
            )

        assertEquals(decodedBytes, decoded)
        assertEquals(true, CompressionCodes.Brotli in decompressors)
    }

    @Test
    fun registryEnforcesCustomDecompressedLimit() = runTest {
        val decompressors =
            mapOf(CompressionCodes.Brotli to Decompressor { _, _ -> ByteString(1, 2) })

        val error =
            assertFailsWith<PmTilesException> {
                decompressors.decompress(
                    CompressionCodes.Brotli,
                    ByteString(1),
                    DecompressionLimits(maxCompressedBytes = 1uL, maxDecompressedBytes = 1uL),
                )
            }

        assertEquals(PmTilesErrorCodes.LimitExceeded, error.code)
    }

    @Test
    fun encodesNone() = runTest {
        val encoded =
            platformDefaultCompressors()
                .compress(
                    CompressionCodes.None,
                    helloBytes,
                    testCompressionLimits(),
                    EncodePurpose.Metadata,
                )

        assertEquals(helloBytes, encoded)
    }

    @Test
    fun encodesGzip() = runTest {
        val encoded =
            platformDefaultCompressors()
                .compress(
                    CompressionCodes.Gzip,
                    helloBytes,
                    testCompressionLimits(),
                    EncodePurpose.Metadata,
                )
        val decoded =
            platformDefaultDecompressors()
                .decompress(
                    CompressionCodes.Gzip,
                    encoded,
                    testDecodeLimits(),
                )

        assertEquals(helloBytes, decoded)
    }

    @Test
    fun noneCompressionEnforcesInputLimit() = runTest {
        val error =
            assertFailsWith<PmTilesException> {
                platformDefaultCompressors()
                    .compress(
                        CompressionCodes.None,
                        helloBytes,
                        testCompressionLimits(maxUncompressedBytes = helloBytes.size - 1),
                        EncodePurpose.Metadata,
                    )
            }

        assertEquals(PmTilesErrorCodes.LimitExceeded, error.code)
    }

    @Test
    fun gzipCompressionEnforcesOutputLimit() = runTest {
        val error =
            assertFailsWith<PmTilesException> {
                platformDefaultCompressors()
                    .compress(
                        CompressionCodes.Gzip,
                        helloBytes,
                        testCompressionLimits(maxCompressedBytes = 1),
                        EncodePurpose.Metadata,
                    )
            }

        assertEquals(PmTilesErrorCodes.LimitExceeded, error.code)
    }

    @Test
    fun compressionRegistryEnforcesOutputLimit() = runTest {
        val compressors = mapOf(CompressionCodes.Brotli to Compressor { _, _ -> ByteString(1, 2) })

        val error =
            assertFailsWith<PmTilesException> {
                compressors.compress(
                    CompressionCodes.Brotli,
                    ByteString(1),
                    testCompressionLimits(maxCompressedBytes = 1),
                    EncodePurpose.Tile,
                )
            }

        assertEquals(PmTilesErrorCodes.LimitExceeded, error.code)
    }

    @Test
    fun rejectsUnsupportedCompressionWhenEncodeIsRequired() = runTest {
        listOf(
                CompressionCodes.Unknown,
                CompressionCodes.Brotli,
                CompressionCodes.Zstd,
                CompressionCode(99u),
            )
            .forEach { compression ->
                val error =
                    assertFailsWith<PmTilesException> {
                        platformDefaultCompressors()
                            .compress(
                                compression,
                                helloBytes,
                                testCompressionLimits(),
                                EncodePurpose.RootDirectory,
                            )
                    }

                assertEquals(PmTilesErrorCodes.UnsupportedCompression, error.code)
            }
    }

    @Test
    fun customCompressorsOverrideDefaultEntriesByCompressionCode() = runTest {
        val encodedBytes = ByteString(4, 5, 6)
        val compressors =
            platformDefaultCompressors() +
                mapOf(CompressionCodes.None to Compressor { _, _ -> encodedBytes })

        val encoded =
            compressors.compress(
                CompressionCodes.None,
                ByteString(1, 2, 3),
                testCompressionLimits(maxUncompressedBytes = 3, maxCompressedBytes = 3),
                EncodePurpose.Tile,
            )

        assertEquals(encodedBytes, encoded)
    }

    @Test
    fun wrapsUnexpectedCompressorFailures() = runTest {
        val compressors = mapOf(CompressionCodes.Brotli to Compressor { _, _ -> error("boom") })

        val error =
            assertFailsWith<PmTilesException> {
                compressors.compress(
                    CompressionCodes.Brotli,
                    ByteString(1),
                    testCompressionLimits(),
                    EncodePurpose.Tile,
                )
            }

        assertEquals(PmTilesErrorCodes.CompressionFailed, error.code)
    }
}

private fun testCompressionLimits(
    maxUncompressedBytes: Int = 1024,
    maxCompressedBytes: Int = 1024,
): CompressionLimits =
    CompressionLimits(
        maxUncompressedBytes = maxUncompressedBytes.toULong(),
        maxCompressedBytes = maxCompressedBytes.toULong(),
    )
