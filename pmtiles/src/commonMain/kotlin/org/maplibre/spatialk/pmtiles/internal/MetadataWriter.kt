package org.maplibre.spatialk.pmtiles.internal

import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.encodeToByteString
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import org.maplibre.spatialk.pmtiles.ArchiveWriteConfig
import org.maplibre.spatialk.pmtiles.ArchiveWriteOptions
import org.maplibre.spatialk.pmtiles.CompressionLimits
import org.maplibre.spatialk.pmtiles.PmTilesErrorCodes
import org.maplibre.spatialk.pmtiles.TileTypeCodes

internal data class EncodedMetadata(
    val rawBytes: ByteString,
    val compressedBytes: ByteString,
)

internal suspend fun encodeMetadata(
    config: ArchiveWriteConfig,
    options: ArchiveWriteOptions,
): EncodedMetadata {
    val jsonObject = parseMetadataObject(config.metadataJson)
    validateMetadataForTileType(jsonObject, config)

    val rawBytes = config.metadataJson.encodeToByteString()
    val compressedBytes =
        options
            .effectiveCompressors()
            .compress(
                compression = options.internalCompression,
                bytes = rawBytes,
                limits =
                    CompressionLimits(
                        maxUncompressedBytes = options.limits.maxMetadataBytes,
                        maxCompressedBytes = options.limits.maxMetadataBytes,
                    ),
                purpose = EncodePurpose.Metadata,
            )
    return EncodedMetadata(rawBytes = rawBytes, compressedBytes = compressedBytes)
}

private fun parseMetadataObject(rawJson: String): JsonObject {
    val element =
        try {
            Json.parseToJsonElement(rawJson)
        } catch (error: SerializationException) {
            throw invalidMetadata("Metadata JSON is malformed.", error)
        }

    return element as? JsonObject ?: throw invalidMetadata("Metadata JSON is not an object.")
}

private fun validateMetadataForTileType(jsonObject: JsonObject, config: ArchiveWriteConfig) {
    if (config.tileType != TileTypeCodes.Mvt) return
    if (jsonObject["vector_layers"] is JsonArray) return

    throw invalidMetadata("MVT metadata must contain `vector_layers` as an array.")
}

private fun invalidMetadata(message: String, cause: Throwable? = null) =
    pmTilesException(PmTilesErrorCodes.InvalidMetadata, message, cause)
