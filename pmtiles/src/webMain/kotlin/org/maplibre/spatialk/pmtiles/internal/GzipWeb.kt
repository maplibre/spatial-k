@file:OptIn(ExperimentalWasmJsInterop::class)

package org.maplibre.spatialk.pmtiles.internal

import js.buffer.ArrayBuffer
import js.reflect.unsafeCast
import js.typedarrays.Uint8Array
import js.typedarrays.toByteArray
import js.typedarrays.toUint8Array
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.js
import org.maplibre.spatialk.pmtiles.PmTilesErrorCode
import org.maplibre.spatialk.pmtiles.PmTilesException
import web.compression.CompressionFormat
import web.compression.DecompressionStream
import web.compression.gzip
import web.http.BodyInit
import web.http.Response
import web.streams.ReadableStream
import web.streams.ReadableWritablePair
import web.streams.read

internal actual suspend fun decodeGzip(bytes: ByteArray, limits: DecodeLimits): ByteArray {
    if (!hasDecompressionStream()) {
        throw pmTilesException(
            PmTilesErrorCode.UnsupportedCompression,
            "${limits.purpose.displayName} gzip decompression requires DecompressionStream.",
        )
    }

    val decoded =
        try {
            val compressedStream =
                Response(BodyInit(bytes.toUint8Array())).body
                    ?: error("Response created from bytes should have a body.")

            compressedStream
                .pipeThrough<Uint8Array<ArrayBuffer>>(
                    DecompressionStream(CompressionFormat.gzip).asReadableWritablePair()
                )
                .readBoundedBytes(limits)
        } catch (error: PmTilesException) {
            throw error
        } catch (error: Throwable) {
            decompressionFailed("${limits.purpose.displayName} gzip decompression failed.", error)
        }

    return decoded
}

private fun hasDecompressionStream(): Boolean =
    js("typeof globalThis.DecompressionStream === 'function'")

private fun DecompressionStream.asReadableWritablePair():
    ReadableWritablePair<Uint8Array<ArrayBuffer>, Uint8Array<ArrayBuffer>> = unsafeCast(this)

private suspend fun ReadableStream<Uint8Array<ArrayBuffer>>.readBoundedBytes(
    limits: DecodeLimits
): ByteArray {
    val reader = getReader()
    val sink = BoundedByteArraySink(limits)
    try {
        while (true) {
            val result = reader.read()
            if (result.done) break
            val chunk = result.value ?: continue
            val bytes = chunk.toByteArray()
            sink.append(bytes, bytes.size)
        }
    } finally {
        reader.releaseLock()
    }
    return sink.toByteArray()
}
