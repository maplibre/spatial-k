@file:OptIn(
    kotlin.experimental.ExperimentalObjCName::class,
    kotlin.experimental.ExperimentalObjCRefinement::class,
)

package org.maplibre.spatialk.pmtiles

import kotlin.coroutines.cancellation.CancellationException
import kotlin.native.HiddenFromObjC
import kotlin.native.ObjCName
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.maplibre.spatialk.pmtiles.internal.ArchiveReadState
import org.maplibre.spatialk.pmtiles.internal.DecodeLimits
import org.maplibre.spatialk.pmtiles.internal.DecodePurpose
import org.maplibre.spatialk.pmtiles.internal.DirectoryEntry
import org.maplibre.spatialk.pmtiles.internal.DirectoryResolver
import org.maplibre.spatialk.pmtiles.internal.FIRST_READ_BYTES
import org.maplibre.spatialk.pmtiles.internal.allocationLength
import org.maplibre.spatialk.pmtiles.internal.decodeDirectory
import org.maplibre.spatialk.pmtiles.internal.parseHeaderForOpen
import org.maplibre.spatialk.pmtiles.internal.pmTilesException
import org.maplibre.spatialk.pmtiles.internal.readSourceRange
import org.maplibre.spatialk.pmtiles.internal.sourceSize
import org.maplibre.spatialk.pmtiles.internal.toByteRange

/**
 * Open PMTiles archive reader.
 *
 * @property header Parsed PMTiles header.
 * @property tileType Tile payload type from the header.
 * @property internalCompression Compression used for directories and metadata.
 * @property tileCompression Compression used for tile payloads.
 */
public class PmTilesArchive
private constructor(
    public val header: ArchiveHeader,
    private val source: ByteRangeSource,
    private val options: ArchiveOpenOptions,
    private val archiveSize: ULong,
    rootDirectory: List<DirectoryEntry>,
    initialWarnings: List<ArchiveWarning>,
) : AutoCloseable {
    private val state = ArchiveReadState(initialWarnings)
    private val decompressors = options.effectiveDecompressors()
    private val directoryResolver =
        DirectoryResolver(
            header = header,
            source = source,
            options = options,
            decompressors = decompressors,
            archiveSize = archiveSize,
            rootDirectory = rootDirectory,
            state = state,
        )

    /** Tile payload type from the header. */
    public val tileType: TileType = header.tileType

    /** Compression used for directories and metadata. */
    public val internalCompression: Compression = header.internalCompression

    /** Compression used for tile payloads. */
    public val tileCompression: Compression = header.tileCompression

    /** Returns the raw metadata JSON string. */
    @Throws(PmTilesException::class, CancellationException::class)
    public suspend fun rawMetadataJson(): String {
        state.cachedRawMetadataJson()?.let {
            return it
        }

        val json =
            if (header.metadata.length == 0uL) {
                ""
            } else {
                val compressedBytes =
                    state.readSourceRangeDeduplicated(
                        source = source,
                        archiveSize = archiveSize,
                        range =
                            header.metadata.toByteRange(
                                options.limits.maxMetadataBytes,
                                "Metadata",
                            ),
                        maxBytes = options.limits.maxMetadataBytes,
                    )
                val metadataBytes =
                    decompressors.decompress(
                        header.internalCompression,
                        compressedBytes,
                        DecodeLimits(
                            maxCompressedBytes = options.limits.maxMetadataBytes,
                            maxDecompressedBytes = options.limits.maxMetadataBytes,
                            purpose = DecodePurpose.Metadata,
                        ),
                    )
                metadataBytes.decodeMetadataUtf8()
            }

        return state.cacheRawMetadataJson(json)
    }

    /** Returns typed PMTiles metadata fields. */
    @Throws(PmTilesException::class, CancellationException::class)
    public suspend fun metadata(): ArchiveMetadata {
        state.cachedMetadata()?.let {
            return it
        }

        val rawJson = rawMetadataJson()
        return state.cacheMetadata(parseMetadata(rawJson))
    }

    /** Returns the tile at [z], [x], and [y], or null when absent. */
    @Throws(PmTilesException::class, CancellationException::class)
    public suspend fun getTile(z: Int, x: Int, y: Int): ArchiveTile? {
        TileIds.validateZxy(z, x, y)
        return readTile(
            tileId = TileIds.fromZxy(z, x, y),
            coord = TileCoord(z = z, x = x, y = y),
        )
    }

    /** Returns the tile at [coord], or null when absent. */
    @Throws(PmTilesException::class, CancellationException::class)
    public suspend fun getTile(coord: TileCoord): ArchiveTile? = getTile(coord.z, coord.x, coord.y)

    /** Returns the tile with [tileId], or null when absent. */
    @Throws(PmTilesException::class, CancellationException::class)
    public suspend fun getTileById(tileId: Long): ArchiveTile? {
        return readTile(
            tileId = tileId,
            coord = TileIds.toZxy(tileId),
        )
    }

    /** Returns the archive byte range for the tile at [z], [x], and [y]. */
    @Throws(PmTilesException::class, CancellationException::class)
    public suspend fun getTileRange(z: Int, x: Int, y: Int): TileRange? {
        TileIds.validateZxy(z, x, y)
        return directoryResolver.findTileRange(
            tileId = TileIds.fromZxy(z, x, y),
            coord = TileCoord(z = z, x = x, y = y),
        )
    }

    /** Returns compressed tile bytes for the tile at [z], [x], and [y]. */
    @Throws(PmTilesException::class, CancellationException::class)
    public suspend fun getTileCompressed(z: Int, x: Int, y: Int): ArchiveTile? {
        TileIds.validateZxy(z, x, y)
        return readTile(
            tileId = TileIds.fromZxy(z, x, y),
            coord = TileCoord(z = z, x = x, y = y),
        )
    }

    /** Returns decompressed tile bytes for the tile at [z], [x], and [y]. */
    @Throws(PmTilesException::class, CancellationException::class)
    public suspend fun getTileDecompressed(z: Int, x: Int, y: Int): ArchiveTile? {
        TileIds.validateZxy(z, x, y)
        return readTile(
                tileId = TileIds.fromZxy(z, x, y),
                coord = TileCoord(z = z, x = x, y = y),
            )
            ?.decompressed()
    }

    /** Returns decompressed tile bytes for [coord], or null when absent. */
    @Throws(PmTilesException::class, CancellationException::class)
    public suspend fun getTileDecompressed(coord: TileCoord): ArchiveTile? =
        getTileDecompressed(coord.z, coord.x, coord.y)

    /** Returns decompressed tile bytes for [tileId], or null when absent. */
    @Throws(PmTilesException::class, CancellationException::class)
    public suspend fun getTileDecompressedById(tileId: Long): ArchiveTile? =
        readTile(
                tileId = tileId,
                coord = TileIds.toZxy(tileId),
            )
            ?.decompressed()

    /** Returns true when the archive contains a tile at [z], [x], and [y]. */
    @Throws(PmTilesException::class, CancellationException::class)
    public suspend fun containsTile(z: Int, x: Int, y: Int): Boolean {
        return getTileRange(z, x, y) != null
    }

    /** Number of warnings recorded by this archive. */
    public val warningCount: Int
        get() = state.warningCount

    /** Returns the warning at [index], or null when [index] is outside the warning list. */
    @ObjCName(name = "warningAt", swiftName = "warning")
    public fun warningAt(@ObjCName(name = "at", swiftName = "at") index: Int): ArchiveWarning? =
        state.warningAt(index)

    /** Returns a snapshot of warnings recorded by this archive. */
    @HiddenFromObjC public fun warnings(): List<ArchiveWarning> = state.warnings()

    /** Releases archive-owned caches and in-flight work. */
    override public fun close(): Unit = state.close()

    private suspend fun readTile(
        tileId: Long,
        coord: TileCoord,
    ): ArchiveTile? {
        val range = directoryResolver.findTileRange(tileId, coord) ?: return null
        return readCompressedTile(range)
    }

    private suspend fun readCompressedTile(range: TileRange): ArchiveTile {
        val bytes =
            state.readSourceRangeDeduplicated(
                source = source,
                archiveSize = archiveSize,
                range = range.archiveRange,
                maxBytes = options.limits.maxTileCompressedBytes,
            )
        return ArchiveTile(
            tileId = range.tileId,
            coord = range.coord,
            bytes = bytes,
            tileType = range.tileType,
            compression = range.compression,
            wasDecompressed = false,
            range = range,
        )
    }

    private suspend fun ArchiveTile.decompressed(): ArchiveTile {
        if (compression == Compression.None) return this

        val decompressedBytes =
            decompressors.decompress(
                compression,
                bytes,
                DecodeLimits(
                    maxCompressedBytes = options.limits.maxTileCompressedBytes,
                    maxDecompressedBytes = options.limits.maxTileDecompressedBytes,
                    purpose = DecodePurpose.Tile,
                ),
            )
        return ArchiveTile(
            tileId = tileId,
            coord = coord,
            bytes = decompressedBytes,
            tileType = tileType,
            compression = Compression.None,
            wasDecompressed = true,
            range = range,
        )
    }

    private fun parseMetadata(rawJson: String): ArchiveMetadata {
        if (rawJson.isEmpty()) {
            return emptyMetadata().also { validateMvtVectorLayers(hasVectorLayers = false) }
        }

        val jsonObject =
            try {
                Json.parseToJsonElement(rawJson) as? JsonObject
            } catch (error: SerializationException) {
                recoverMetadata("Metadata JSON is malformed.", error)
                return emptyMetadata()
            }

        if (jsonObject == null) {
            recoverMetadata("Metadata JSON is not an object.")
            return emptyMetadata()
        }

        val metadata =
            ArchiveMetadata(
                name = jsonObject.optionalString("name"),
                description = jsonObject.optionalString("description"),
                attribution = jsonObject.optionalString("attribution"),
                type = jsonObject.optionalString("type")?.let(::TilesetKind),
                version = jsonObject.optionalString("version"),
                encoding = jsonObject.optionalString("encoding"),
                vectorLayersJson = jsonObject.optionalVectorLayersJson(),
            )

        validateMvtVectorLayers(hasVectorLayers = metadata.vectorLayersJson != null)
        return metadata
    }

    private fun JsonObject.optionalString(key: String): String? {
        val value = this[key] ?: return null
        if (value is JsonPrimitive && value.isString) return value.content
        recoverMetadata("Metadata key `$key` is not a string.")
        return null
    }

    private fun JsonObject.optionalVectorLayersJson(): String? {
        val value = this["vector_layers"] ?: return null
        if (value is JsonArray) return value.toString()
        recoverMetadata("Metadata key `vector_layers` is not an array.")
        return null
    }

    private fun validateMvtVectorLayers(hasVectorLayers: Boolean) {
        if (header.tileType != TileType.Mvt || hasVectorLayers) return
        if (options.validationMode == ValidationMode.Strict) {
            throw pmTilesException(
                PmTilesErrorCode.InvalidMetadata,
                "MVT metadata must contain `vector_layers`.",
            )
        }
        state.appendWarning(
            ArchiveWarning(
                code = ArchiveWarningCode.MissingVectorLayers,
                message = "MVT metadata does not contain `vector_layers`.",
            )
        )
    }

    private fun recoverMetadata(message: String, cause: Throwable? = null) {
        if (options.validationMode == ValidationMode.Strict) {
            throw pmTilesException(PmTilesErrorCode.InvalidMetadata, message, cause)
        }
        state.appendWarning(
            ArchiveWarning(
                code = ArchiveWarningCode.InvalidMetadataRecovered,
                message = message,
            )
        )
    }

    /** Factory methods for PMTiles archives. */
    public companion object {
        /** Opens a PMTiles archive from [source] with [options]. */
        @HiddenFromObjC
        @Throws(PmTilesException::class, CancellationException::class)
        public suspend fun open(
            source: ByteRangeSource,
            options: ArchiveOpenOptions = ArchiveOpenOptions.Default,
        ): PmTilesArchive {
            val sourceSize = source.sourceSize()
            val initialReadLength =
                allocationLength(
                    minOf(sourceSize, FIRST_READ_BYTES.toULong()),
                    options.limits.maxInitialReadBytes,
                    "Initial read",
                )
            val initialBytes =
                source.readSourceRange(
                    ByteRange(offset = 0uL, length = initialReadLength),
                    archiveSize = sourceSize,
                    maxBytes = options.limits.maxInitialReadBytes,
                )
            val parsedHeader = parseHeaderForOpen(initialBytes, sourceSize, options.validationMode)
            val header = parsedHeader.header
            val decompressors = options.effectiveDecompressors()
            val compressedRoot = initialBytes.slice(header.rootDirectory, options.limits)
            val rootDirectoryBytes =
                decompressors.decompress(
                    header.internalCompression,
                    compressedRoot,
                    DecodeLimits(
                        maxCompressedBytes = options.limits.maxDirectoryCompressedBytes,
                        maxDecompressedBytes = options.limits.maxDirectoryDecompressedBytes,
                        purpose = DecodePurpose.RootDirectory,
                    ),
                )
            val rootDirectory =
                decodeDirectory(
                    rootDirectoryBytes,
                    header,
                    options.limits,
                    allowEmpty = options.validationMode == ValidationMode.Lenient,
                )
            val warnings =
                if (rootDirectory.isEmpty()) {
                    parsedHeader.warnings +
                        ArchiveWarning(
                            code = ArchiveWarningCode.EmptyRootDirectory,
                            message =
                                "Root directory has zero entries and was accepted in lenient mode.",
                        )
                } else {
                    parsedHeader.warnings
                }

            return PmTilesArchive(
                header = header,
                source = source,
                options = options,
                archiveSize = sourceSize,
                rootDirectory = rootDirectory,
                initialWarnings = warnings,
            )
        }
    }
}

private fun ByteArray.decodeMetadataUtf8(): String =
    try {
        decodeToString(throwOnInvalidSequence = true)
    } catch (error: Throwable) {
        throw pmTilesException(
            PmTilesErrorCode.InvalidMetadata,
            "Metadata is not valid UTF-8.",
            error,
        )
    }

private fun emptyMetadata(): ArchiveMetadata =
    ArchiveMetadata(
        name = null,
        description = null,
        attribution = null,
        type = null,
        version = null,
        encoding = null,
        vectorLayersJson = null,
    )

private fun ByteArray.slice(section: ArchiveSection, limits: ArchiveLimits): ByteArray {
    val length =
        allocationLength(
            section.length,
            limits.maxDirectoryCompressedBytes,
            "Root directory",
        )
    val offset = section.offset.toInt()
    return copyOfRange(offset, offset + length)
}
