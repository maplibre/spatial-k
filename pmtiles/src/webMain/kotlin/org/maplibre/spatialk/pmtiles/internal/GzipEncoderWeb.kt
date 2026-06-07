@file:OptIn(ExperimentalWasmJsInterop::class)

package org.maplibre.spatialk.pmtiles.internal

import js.buffer.ArrayBuffer
import js.reflect.unsafeCast
import js.typedarrays.Uint8Array
import js.typedarrays.toByteArray
import js.typedarrays.toUint8Array
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.js
import kotlinx.io.bytestring.ByteString
import org.maplibre.spatialk.pmtiles.CompressionLimits
import org.maplibre.spatialk.pmtiles.PmTilesException
import web.compression.CompressionFormat
import web.compression.CompressionStream
import web.compression.gzip
import web.http.BodyInit
import web.http.Response
import web.streams.ReadableStream
import web.streams.ReadableWritablePair
import web.streams.read

internal suspend fun encodeGzip(bytes: ByteString, limits: CompressionLimits): ByteString {
    val encoded =
        try {
            val uncompressedStream =
                Response(BodyInit(bytes.toByteArray().toUint8Array())).body
                    ?: error("Response created from bytes should have a body.")

            uncompressedStream
                .pipeThrough<Uint8Array<ArrayBuffer>>(
                    CompressionStream(CompressionFormat.gzip).asReadableWritablePair()
                )
                .readBoundedCompressedBytes(limits)
        } catch (error: PmTilesException) {
            throw error
        } catch (error: Throwable) {
            compressionFailed("gzip compression failed.", error)
        }

    return encoded
}

internal fun hasCompressionStream(): Boolean =
    js("typeof globalThis.CompressionStream === 'function'")

private fun CompressionStream.asReadableWritablePair():
    ReadableWritablePair<Uint8Array<ArrayBuffer>, Uint8Array<ArrayBuffer>> = unsafeCast(this)

private suspend fun ReadableStream<Uint8Array<ArrayBuffer>>.readBoundedCompressedBytes(
    limits: CompressionLimits
): ByteString {
    val reader = getReader()
    val sink = BoundedCompressedByteArraySink(limits)
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
    return sink.toByteString()
}
