@file:OptIn(ExperimentalForeignApi::class)

package org.maplibre.spatialk.pmtiles.internal

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.usePinned
import org.maplibre.spatialk.pmtiles.DecompressionLimits
import platform.zlib.ZLIB_VERSION
import platform.zlib.Z_NO_FLUSH
import platform.zlib.Z_OK
import platform.zlib.Z_STREAM_END
import platform.zlib.Z_STREAM_ERROR
import platform.zlib.Z_VERSION_ERROR
import platform.zlib.inflate
import platform.zlib.inflateEnd
import platform.zlib.inflateInit2_
import platform.zlib.z_stream

internal actual suspend fun decodeGzip(bytes: ByteArray, limits: DecompressionLimits): ByteArray =
    memScoped {
        if (bytes.isEmpty()) {
            decompressionFailed("gzip input is empty.")
        }

        val stream = alloc<z_stream>()
        stream.zalloc = null
        stream.zfree = null
        stream.opaque = null

        val sink = BoundedByteArraySink(limits)
        val buffer = ByteArray(GZIP_BUFFER_SIZE)

        bytes.usePinned { input ->
            stream.next_in = input.addressOf(0).reinterpret()
            stream.avail_in = bytes.size.convert()

            val initStatus =
                inflateInit2_(
                    stream.ptr,
                    GZIP_WINDOW_BITS,
                    ZLIB_VERSION,
                    sizeOf<z_stream>().convert(),
                )
            if (initStatus != Z_OK) {
                decompressionFailed("gzip initialization failed: $initStatus.")
            }

            try {
                var finished = false
                while (!finished) {
                    buffer.usePinned { output ->
                        stream.next_out = output.addressOf(0).reinterpret()
                        stream.avail_out = buffer.size.convert()

                        val status = inflate(stream.ptr, Z_NO_FLUSH)
                        val produced = buffer.size - stream.avail_out.toInt()
                        sink.append(buffer, produced)

                        when (status) {
                            Z_STREAM_END -> finished = true
                            Z_OK -> {
                                if (produced == 0 && stream.avail_in == 0u) {
                                    decompressionFailed("gzip stream ended before trailer.")
                                }
                            }
                            else -> decompressionFailed("gzip stream failed: $status.")
                        }
                    }
                }
                sink.toByteArray()
            } finally {
                val endStatus = inflateEnd(stream.ptr)
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
    }

private const val GZIP_BUFFER_SIZE = 8 * 1024
private const val MAX_WBITS = 15
private const val GZIP_WINDOW_BITS = 16 + MAX_WBITS
