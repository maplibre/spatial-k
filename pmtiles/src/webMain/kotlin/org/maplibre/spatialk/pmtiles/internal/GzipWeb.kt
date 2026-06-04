@file:OptIn(ExperimentalWasmJsInterop::class)

package org.maplibre.spatialk.pmtiles.internal

import js.buffer.ArrayBuffer
import js.reflect.unsafeCast
import js.typedarrays.Uint8Array
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
import web.http.byteArray
import web.streams.ReadableWritablePair

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

            Response(
                    compressedStream.pipeThrough<Uint8Array<ArrayBuffer>>(
                        DecompressionStream(CompressionFormat.gzip).asReadableWritablePair()
                    )
                )
                .byteArray()
        } catch (error: PmTilesException) {
            throw error
        } catch (error: Throwable) {
            decompressionFailed("${limits.purpose.displayName} gzip decompression failed.", error)
        }

    validateDecompressedSize(decoded.size, limits)
    return decoded
}

private fun hasDecompressionStream(): Boolean =
    js("typeof globalThis.DecompressionStream === 'function'")

private fun DecompressionStream.asReadableWritablePair():
    ReadableWritablePair<Uint8Array<ArrayBuffer>, Uint8Array<ArrayBuffer>> = unsafeCast(this)
