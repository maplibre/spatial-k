package org.maplibre.spatialk.pmtiles.internal

import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.append
import kotlinx.io.bytestring.buildByteString
import kotlinx.io.bytestring.encodeToByteString
import kotlinx.io.bytestring.hexToByteString

internal val helloBytes: ByteString = "hello pmtiles".encodeToByteString()

internal val helloGzipBytes: ByteString =
    "1f8b0800000000000003cb48cdc9c95728c82dc9cc492d06008227f9820d000000".hexToByteString()

internal val emptyGzipBytes: ByteString =
    "1f8b080000000000000303000000000000000000".hexToByteString()

internal fun truncatedGzipBombBytes(decompressedBytes: Int): ByteString {
    require(decompressedBytes > 1)

    val bits = DeflateBitWriter()
    bits.writeBits(value = 1, count = 1) // Final block.
    bits.writeBits(value = 1, count = 2) // Fixed Huffman block.
    bits.writeFixedHuffmanSymbol(0)

    var remaining = decompressedBytes - 1
    while (remaining > 0) {
        val length = minOf(remaining, MAX_DEFLATE_LENGTH)
        bits.writeLength(length)
        bits.writeDistanceOne()
        remaining -= length
    }
    bits.writeFixedHuffmanSymbol(END_OF_BLOCK)

    return buildByteString {
        append(gzipHeader)
        append(bits.toByteString())
        append(gzipTrailerForZeroBytes(decompressedBytes))
    }
}

private fun gzipTrailerForZeroBytes(decompressedBytes: Int): ByteString {
    val checksum = crc32OfZeros(decompressedBytes)
    val size = decompressedBytes.toUInt()
    return buildByteString {
        append(littleEndian(checksum))
        append(littleEndian(size))
    }
}

private fun littleEndian(value: UInt): ByteString =
    ByteString(
        (value and 0xffu).toByte(),
        ((value shr 8) and 0xffu).toByte(),
        ((value shr 16) and 0xffu).toByte(),
        ((value shr 24) and 0xffu).toByte(),
    )

private fun crc32OfZeros(length: Int): UInt {
    var crc = 0xffffffffu
    repeat(length) {
        repeat(Byte.SIZE_BITS) {
            crc = if ((crc and 1u) == 0u) crc shr 1 else (crc shr 1) xor CRC32_POLYNOMIAL
        }
    }
    return crc xor 0xffffffffu
}

internal fun testDecodeLimits(
    maxCompressedBytes: Int = 1024,
    maxDecompressedBytes: Int = 1024,
): DecodeLimits =
    DecodeLimits(
        maxCompressedBytes = maxCompressedBytes.toULong(),
        maxDecompressedBytes = maxDecompressedBytes.toULong(),
        purpose = DecodePurpose.Metadata,
    )

private class DeflateBitWriter {
    private val bytes = mutableListOf<Byte>()
    private var currentByte = 0
    private var bitCount = 0

    fun writeBits(value: Int, count: Int) {
        repeat(count) { index ->
            currentByte = currentByte or (((value shr index) and 1) shl bitCount)
            bitCount += 1
            if (bitCount == Byte.SIZE_BITS) flushByte()
        }
    }

    fun writeFixedHuffmanSymbol(symbol: Int) {
        val (code, length) =
            when (symbol) {
                in 0..143 -> (0x30 + symbol) to 8
                in 144..255 -> (0x190 + symbol - 144) to 9
                in 256..279 -> (symbol - 256) to 7
                in 280..287 -> (0xc0 + symbol - 280) to 8
                else -> error("Unsupported fixed Huffman symbol $symbol.")
            }
        writeBits(code.reverseBits(length), length)
    }

    fun writeLength(length: Int) {
        val code = LENGTH_CODES.first { length in it.base until it.base + (1 shl it.extraBits) }
        writeFixedHuffmanSymbol(code.symbol)
        if (code.extraBits > 0) writeBits(length - code.base, code.extraBits)
    }

    fun writeDistanceOne() {
        writeBits(value = 0, count = 5)
    }

    fun toByteString(): ByteString {
        if (bitCount > 0) flushByte()
        return buildByteString(bytes.size) {
            bytes.forEach(::append)
        }
    }

    private fun flushByte() {
        bytes += currentByte.toByte()
        currentByte = 0
        bitCount = 0
    }
}

private data class LengthCode(
    val symbol: Int,
    val base: Int,
    val extraBits: Int,
)

private fun Int.reverseBits(count: Int): Int {
    var reversed = 0
    repeat(count) { index ->
        reversed = (reversed shl 1) or ((this shr index) and 1)
    }
    return reversed
}

private val gzipHeader = "1f8b0800000000000003".hexToByteString()

private val LENGTH_CODES =
    listOf(
        LengthCode(257, 3, 0),
        LengthCode(258, 4, 0),
        LengthCode(259, 5, 0),
        LengthCode(260, 6, 0),
        LengthCode(261, 7, 0),
        LengthCode(262, 8, 0),
        LengthCode(263, 9, 0),
        LengthCode(264, 10, 0),
        LengthCode(265, 11, 1),
        LengthCode(266, 13, 1),
        LengthCode(267, 15, 1),
        LengthCode(268, 17, 1),
        LengthCode(269, 19, 2),
        LengthCode(270, 23, 2),
        LengthCode(271, 27, 2),
        LengthCode(272, 31, 2),
        LengthCode(273, 35, 3),
        LengthCode(274, 43, 3),
        LengthCode(275, 51, 3),
        LengthCode(276, 59, 3),
        LengthCode(277, 67, 4),
        LengthCode(278, 83, 4),
        LengthCode(279, 99, 4),
        LengthCode(280, 115, 4),
        LengthCode(281, 131, 5),
        LengthCode(282, 163, 5),
        LengthCode(283, 195, 5),
        LengthCode(284, 227, 5),
        LengthCode(285, 258, 0),
    )

private const val END_OF_BLOCK = 256
private const val MAX_DEFLATE_LENGTH = 258
private val CRC32_POLYNOMIAL: UInt = 0xedb88320u
