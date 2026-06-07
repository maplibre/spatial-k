package org.maplibre.spatialk.pmtiles.internal

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.zip.GZIPOutputStream
import kotlinx.io.bytestring.ByteString
import org.maplibre.spatialk.pmtiles.CompressionLimits
import org.maplibre.spatialk.pmtiles.PmTilesException

internal suspend fun encodeGzip(bytes: ByteString, limits: CompressionLimits): ByteString =
    try {
        val output = ByteArrayOutputStream()
        GZIPOutputStream(output).use { gzip -> gzip.write(bytes.toByteArray()) }
        val encoded = output.toByteArray()
        checkedCompressedSize(current = 0, nextChunk = encoded.size, limits = limits)
        ByteString(encoded)
    } catch (error: PmTilesException) {
        throw error
    } catch (error: IOException) {
        compressionFailed("gzip compression failed.", error)
    }
