package org.maplibre.spatialk.pmtiles.internal

import java.io.ByteArrayInputStream
import java.io.IOException
import java.util.zip.GZIPInputStream
import kotlinx.io.bytestring.ByteString
import org.maplibre.spatialk.pmtiles.DecompressionLimits
import org.maplibre.spatialk.pmtiles.PmTilesException

internal actual suspend fun decodeGzip(bytes: ByteString, limits: DecompressionLimits): ByteString {
    val sink = BoundedByteArraySink(limits)
    val buffer = ByteArray(GZIP_BUFFER_SIZE)

    try {
        GZIPInputStream(ByteArrayInputStream(bytes.toByteArray())).use { gzip ->
            while (true) {
                val read = gzip.read(buffer)
                if (read < 0) break
                sink.append(buffer, read)
            }
        }
    } catch (error: PmTilesException) {
        throw error
    } catch (error: IOException) {
        decompressionFailed("gzip decompression failed.", error)
    }

    return sink.toByteString()
}

private const val GZIP_BUFFER_SIZE = 8 * 1024
