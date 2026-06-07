@file:OptIn(ExperimentalForeignApi::class)

package org.maplibre.spatialk.pmtiles.internal

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.usePinned
import kotlinx.io.bytestring.ByteString
import org.maplibre.spatialk.pmtiles.CompressionLimits
import platform.zlib.ZLIB_VERSION
import platform.zlib.Z_DEFAULT_COMPRESSION
import platform.zlib.Z_DEFAULT_STRATEGY
import platform.zlib.Z_DEFLATED
import platform.zlib.Z_FINISH
import platform.zlib.Z_OK
import platform.zlib.Z_STREAM_END
import platform.zlib.Z_STREAM_ERROR
import platform.zlib.Z_VERSION_ERROR
import platform.zlib.deflate
import platform.zlib.deflateEnd
import platform.zlib.deflateInit2_
import platform.zlib.z_stream

internal suspend fun encodeGzip(bytes: ByteString, limits: CompressionLimits): ByteString =
    memScoped {
        val stream = alloc<z_stream>()
        stream.zalloc = null
        stream.zfree = null
        stream.opaque = null

        val initStatus =
            deflateInit2_(
                stream.ptr,
                Z_DEFAULT_COMPRESSION,
                Z_DEFLATED,
                GZIP_WINDOW_BITS,
                DEFAULT_MEMORY_LEVEL,
                Z_DEFAULT_STRATEGY,
                ZLIB_VERSION,
                sizeOf<z_stream>().convert(),
            )
        if (initStatus != Z_OK) {
            compressionFailed("gzip initialization failed: $initStatus.")
        }

        try {
            val inputBytes = bytes.toByteArray()
            if (inputBytes.isEmpty()) {
                stream.next_in = null
                stream.avail_in = 0u
                finishGzipDeflate(stream.ptr, limits)
            } else {
                inputBytes.usePinned { input ->
                    stream.next_in = input.addressOf(0).reinterpret()
                    stream.avail_in = inputBytes.size.convert()
                    finishGzipDeflate(stream.ptr, limits)
                }
            }
        } finally {
            val endStatus = deflateEnd(stream.ptr)
            stream.next_in = null
            stream.next_out = null
            stream.avail_in = 0u
            stream.avail_out = 0u
            stream.zalloc = null
            stream.zfree = null
            stream.opaque = null
            if (endStatus == Z_STREAM_ERROR || endStatus == Z_VERSION_ERROR) {
                // zlib reports this only for a corrupted stream state; the primary error above
                // wins.
            }
        }
    }

private fun finishGzipDeflate(
    stream: kotlinx.cinterop.CPointer<z_stream>,
    limits: CompressionLimits,
): ByteString {
    val sink = BoundedCompressedByteArraySink(limits)
    val buffer = ByteArray(GZIP_BUFFER_SIZE)
    var finished = false
    while (!finished) {
        buffer.usePinned { output ->
            stream.pointed.next_out = output.addressOf(0).reinterpret()
            stream.pointed.avail_out = buffer.size.convert()

            val status = deflate(stream, Z_FINISH)
            val produced = buffer.size - stream.pointed.avail_out.toInt()
            sink.append(buffer, produced)

            when (status) {
                Z_STREAM_END -> finished = true
                Z_OK -> {
                    if (produced == 0) {
                        compressionFailed("gzip stream made no progress.")
                    }
                }
                else -> compressionFailed("gzip stream failed: $status.")
            }
        }
    }
    return sink.toByteString()
}

private const val GZIP_BUFFER_SIZE = 8 * 1024
private const val MAX_WBITS = 15
private const val GZIP_WINDOW_BITS = 16 + MAX_WBITS
private const val DEFAULT_MEMORY_LEVEL = 8
