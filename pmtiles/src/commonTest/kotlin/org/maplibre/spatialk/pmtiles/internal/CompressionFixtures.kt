package org.maplibre.spatialk.pmtiles.internal

internal val helloBytes: ByteArray = "hello pmtiles".encodeToByteArray()

internal val helloGzipBytes: ByteArray =
    intArrayOf(
            0x1f,
            0x8b,
            0x08,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x03,
            0xcb,
            0x48,
            0xcd,
            0xc9,
            0xc9,
            0x57,
            0x28,
            0xc8,
            0x2d,
            0xc9,
            0xcc,
            0x49,
            0x2d,
            0x06,
            0x00,
            0x82,
            0x27,
            0xf9,
            0x82,
            0x0d,
            0x00,
            0x00,
            0x00,
        )
        .map { it.toByte() }
        .toByteArray()

internal val emptyGzipBytes: ByteArray =
    intArrayOf(
            0x1f,
            0x8b,
            0x08,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x03,
            0x03,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
        )
        .map { it.toByte() }
        .toByteArray()

internal fun testDecodeLimits(
    maxCompressedBytes: Int = 1024,
    maxDecompressedBytes: Int = 1024,
): DecodeLimits =
    DecodeLimits(
        maxCompressedBytes = maxCompressedBytes,
        maxDecompressedBytes = maxDecompressedBytes,
        purpose = DecodePurpose.Metadata,
    )
