package org.maplibre.spatialk.pmtiles.internal

import java.io.ByteArrayInputStream
import java.io.IOException
import java.util.zip.GZIPInputStream
import org.maplibre.spatialk.pmtiles.PmTilesException

internal actual fun decodeGzip(bytes: ByteArray, limits: DecodeLimits): ByteArray {
    val sink = BoundedByteArraySink(limits)
    val buffer = ByteArray(GZIP_BUFFER_SIZE)

    try {
        GZIPInputStream(ByteArrayInputStream(bytes)).use { gzip ->
            while (true) {
                val read = gzip.read(buffer)
                if (read < 0) break
                sink.append(buffer, read)
            }
        }
    } catch (error: PmTilesException) {
        throw error
    } catch (error: IOException) {
        decompressionFailed("${limits.purpose.displayName} gzip decompression failed.", error)
    }

    return sink.toByteArray()
}

private const val GZIP_BUFFER_SIZE = 8 * 1024
