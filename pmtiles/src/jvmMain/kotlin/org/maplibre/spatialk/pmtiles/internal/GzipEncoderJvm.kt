package org.maplibre.spatialk.pmtiles.internal

import java.io.IOException
import java.io.OutputStream
import java.util.zip.GZIPOutputStream
import kotlinx.io.bytestring.ByteString
import org.maplibre.spatialk.pmtiles.CompressionLimits
import org.maplibre.spatialk.pmtiles.PmTilesException

internal suspend fun encodeGzip(bytes: ByteString, limits: CompressionLimits): ByteString =
    try {
        val output = BoundedCompressedOutputStream(limits)
        GZIPOutputStream(output).use { gzip -> gzip.write(bytes.toByteArray()) }
        output.toByteString()
    } catch (error: PmTilesException) {
        throw error
    } catch (error: IOException) {
        compressionFailed("gzip compression failed.", error)
    }

private class BoundedCompressedOutputStream(limits: CompressionLimits) : OutputStream() {
    private val sink = BoundedCompressedByteArraySink(limits)

    override fun write(value: Int) {
        sink.append(byteArrayOf(value.toByte()), 1)
    }

    override fun write(bytes: ByteArray, offset: Int, length: Int) {
        if (length <= 0) return
        sink.append(bytes.copyOfRange(offset, offset + length), length)
    }

    fun toByteString(): ByteString = sink.toByteString()
}
