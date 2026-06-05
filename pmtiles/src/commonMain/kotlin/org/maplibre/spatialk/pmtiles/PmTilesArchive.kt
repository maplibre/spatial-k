@file:OptIn(kotlin.experimental.ExperimentalObjCRefinement::class)

package org.maplibre.spatialk.pmtiles

import kotlin.coroutines.cancellation.CancellationException
import kotlin.native.HiddenFromObjC
import kotlinx.io.bytestring.ByteString
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
import org.maplibre.spatialk.pmtiles.internal.checkedAdd
import org.maplibre.spatialk.pmtiles.internal.decodeDirectory
import org.maplibre.spatialk.pmtiles.internal.decompress
import org.maplibre.spatialk.pmtiles.internal.effectiveDecompressors
import org.maplibre.spatialk.pmtiles.internal.parseHeaderForOpen
import org.maplibre.spatialk.pmtiles.internal.pmTilesException
import org.maplibre.spatialk.pmtiles.internal.readSourceRange
import org.maplibre.spatialk.pmtiles.internal.sourceSize
import org.maplibre.spatialk.pmtiles.internal.toByteRange

/** Factory methods for PMTiles archives. */
public object PmTiles {
    /** Opens a PMTiles archive from [source] with [options]. */
    @HiddenFromObjC
    @Throws(PmTilesException::class, CancellationException::class)
    public suspend fun open(
        source: ByteRangeSource,
        options: ArchiveOpenOptions = ArchiveOpenOptions(),
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

    /** Opens a PMTiles archive from [source] with strict validation. */
    @HiddenFromObjC
    @Throws(PmTilesException::class, CancellationException::class)
    public suspend fun open(source: ByteRangeSource): PmTilesArchive =
        open(source = source, options = ArchiveOpenOptions())
}

/**
 * Open PMTiles archive reader.
 *
 * @property header Parsed PMTiles header.
 * @property tileType Tile payload type from the header.
 * @property internalCompression CompressionCode used for directories and metadata.
 * @property tileCompression CompressionCode used for tile payloads.
 */
public class PmTilesArchive
internal constructor(
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
    public val tileType: TileTypeCode = header.tileType

    /** CompressionCode used for directories and metadata. */
    public val internalCompression: CompressionCode = header.internalCompression

    /** CompressionCode used for tile payloads. */
    public val tileCompression: CompressionCode = header.tileCompression

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

    /** Returns stored tile bytes for the tile at [z], [x], and [y], or null when absent. */
    @Throws(PmTilesException::class, CancellationException::class)
    public suspend fun readStoredTile(z: Int, x: Int, y: Int): ArchiveTile? {
        return readTile(
            tileId = TileIds.fromZxy(z, x, y),
            coord = TileCoord(z = z, x = x, y = y),
        )
    }

    /** Returns stored tile bytes for [coord], or null when absent. */
    @Throws(PmTilesException::class, CancellationException::class)
    public suspend fun readStoredTile(coord: TileCoord): ArchiveTile? =
        readStoredTile(coord.z, coord.x, coord.y)

    /** Returns stored tile bytes for [tileId], or null when absent. */
    @Throws(PmTilesException::class, CancellationException::class)
    public suspend fun readStoredTile(tileId: Long): ArchiveTile? {
        return readTile(
            tileId = tileId,
            coord = TileIds.toZxy(tileId),
        )
    }

    /**
     * Returns stored tile read results for [coords], preserving input order.
     *
     * Tile payload source reads may be coalesced according to [coalescing].
     */
    @Throws(PmTilesException::class, CancellationException::class)
    public suspend fun readStoredTiles(
        coords: List<TileCoord>,
        coalescing: TileReadCoalescing = TileReadCoalescing(),
    ): List<TileReadResult> =
        readTiles(
            coords = coords,
            coalescing = coalescing,
            decompress = false,
        )

    /** Returns stored tiles for [coords] using default read coalescing. */
    @Throws(PmTilesException::class, CancellationException::class)
    public suspend fun readStoredTiles(coords: List<TileCoord>): List<TileReadResult> =
        readStoredTiles(coords = coords, coalescing = TileReadCoalescing())

    /**
     * Returns decompressed tile read results for [coords], preserving input order.
     *
     * Tile payload source reads may be coalesced according to [coalescing].
     */
    @Throws(PmTilesException::class, CancellationException::class)
    public suspend fun readDecompressedTiles(
        coords: List<TileCoord>,
        coalescing: TileReadCoalescing = TileReadCoalescing(),
    ): List<TileReadResult> =
        readTiles(
            coords = coords,
            coalescing = coalescing,
            decompress = true,
        )

    /** Returns decompressed tiles for [coords] using default read coalescing. */
    @Throws(PmTilesException::class, CancellationException::class)
    public suspend fun readDecompressedTiles(coords: List<TileCoord>): List<TileReadResult> =
        readDecompressedTiles(coords = coords, coalescing = TileReadCoalescing())

    /** Returns the archive byte range for the tile at [z], [x], and [y]. */
    @Throws(PmTilesException::class, CancellationException::class)
    public suspend fun findTileRange(z: Int, x: Int, y: Int): TileRange? {
        return directoryResolver.findTileRange(
            tileId = TileIds.fromZxy(z, x, y),
            coord = TileCoord(z = z, x = x, y = y),
        )
    }

    /** Returns the archive byte range for [coord]. */
    @Throws(PmTilesException::class, CancellationException::class)
    public suspend fun findTileRange(coord: TileCoord): TileRange? =
        findTileRange(coord.z, coord.x, coord.y)

    /** Returns the archive byte range for [tileId]. */
    @Throws(PmTilesException::class, CancellationException::class)
    public suspend fun findTileRange(tileId: Long): TileRange? =
        directoryResolver.findTileRange(tileId = tileId, coord = TileIds.toZxy(tileId))

    /** Returns decompressed tile bytes for the tile at [z], [x], and [y]. */
    @Throws(PmTilesException::class, CancellationException::class)
    public suspend fun readDecompressedTile(z: Int, x: Int, y: Int): ArchiveTile? {
        return readTile(
            tileId = TileIds.fromZxy(z, x, y),
            coord = TileCoord(z = z, x = x, y = y),
            decompress = true,
        )
    }

    /** Returns decompressed tile bytes for [coord], or null when absent. */
    @Throws(PmTilesException::class, CancellationException::class)
    public suspend fun readDecompressedTile(coord: TileCoord): ArchiveTile? =
        readDecompressedTile(coord.z, coord.x, coord.y)

    /** Returns decompressed tile bytes for [tileId], or null when absent. */
    @Throws(PmTilesException::class, CancellationException::class)
    public suspend fun readDecompressedTile(tileId: Long): ArchiveTile? =
        readTile(
            tileId = tileId,
            coord = TileIds.toZxy(tileId),
            decompress = true,
        )

    /** Returns true when the archive contains a tile at [z], [x], and [y]. */
    @HiddenFromObjC
    @Throws(PmTilesException::class, CancellationException::class)
    public suspend fun containsTile(z: Int, x: Int, y: Int): Boolean {
        return findTileRange(z, x, y) != null
    }

    /** Returns true when the archive contains [coord]. */
    @HiddenFromObjC
    @Throws(PmTilesException::class, CancellationException::class)
    public suspend fun containsTile(coord: TileCoord): Boolean =
        containsTile(coord.z, coord.x, coord.y)

    /** Returns true when the archive contains [tileId]. */
    @HiddenFromObjC
    @Throws(PmTilesException::class, CancellationException::class)
    public suspend fun containsTile(tileId: Long): Boolean =
        directoryResolver.findTileRange(tileId = tileId, coord = TileIds.toZxy(tileId)) != null

    /** Snapshot of warnings recorded by this archive. */
    public val warnings: List<ArchiveWarning>
        get() = state.warnings()

    /** Releases archive-owned caches and in-flight work. */
    override public fun close(): Unit = state.close()

    private suspend fun readTile(
        tileId: Long,
        coord: TileCoord,
        decompress: Boolean = false,
    ): ArchiveTile? {
        val range = directoryResolver.findTileRange(tileId, coord) ?: return null
        return readLocatedTiles(
            locatedTiles = listOf(LocatedTile(index = 0, range = range)),
            outputSize = 1,
            coalescing = TileReadCoalescing(maxCoalescedBytes = 0uL),
            decompress = decompress,
        )[0]
    }

    private suspend fun readTiles(
        coords: List<TileCoord>,
        coalescing: TileReadCoalescing,
        decompress: Boolean,
    ): List<TileReadResult> {
        val tiles =
            readLocatedTiles(
                locatedTiles = locateTiles(coords),
                outputSize = coords.size,
                coalescing = coalescing,
                decompress = decompress,
            )
        return coords.mapIndexed { index, coord ->
            TileReadResult(coord = coord, tile = tiles[index])
        }
    }

    private suspend fun readLocatedTiles(
        locatedTiles: List<LocatedTile>,
        outputSize: Int,
        coalescing: TileReadCoalescing,
        decompress: Boolean,
    ): List<ArchiveTile?> {
        val tiles = MutableList<ArchiveTile?>(outputSize) { null }

        locatedTiles.coalescedReads(coalescing).forEach { read ->
            val bytes =
                state.readSourceRangeDeduplicated(
                    source = source,
                    archiveSize = archiveSize,
                    range = read.range,
                    maxBytes = read.range.length,
                )
            read.tiles.forEach { located ->
                val compressedTile =
                    located.range.toArchiveTile(
                        payload = bytes.sliceTile(read.range, located.range)
                    )
                tiles[located.index] =
                    if (decompress) compressedTile.decompressed() else compressedTile
            }
        }

        return tiles
    }

    private suspend fun locateTiles(coords: List<TileCoord>): List<LocatedTile> {
        val located = mutableListOf<LocatedTile>()
        coords.forEachIndexed { index, coord ->
            val tileId = TileIds.fromZxy(coord.z, coord.x, coord.y)
            val range = directoryResolver.findTileRange(tileId, coord)
            if (range != null) {
                located += LocatedTile(index = index, range = range)
            }
        }
        return located
    }

    private suspend fun ArchiveTile.decompressed(): ArchiveTile {
        if (compression == CompressionCodes.None) return this

        val decompressedBytes =
            decompressors.decompress(
                compression,
                payload,
                DecodeLimits(
                    maxCompressedBytes = options.limits.maxTileCompressedBytes,
                    maxDecompressedBytes = options.limits.maxTileDecompressedBytes,
                    purpose = DecodePurpose.Tile,
                ),
            )
        return ArchiveTile(
            tileId = tileId,
            coord = coord,
            payload = decompressedBytes,
            tileType = tileType,
            compression = CompressionCodes.None,
            wasDecompressed = true,
            range = range,
        )
    }

    private fun TileRange.toArchiveTile(payload: ByteString): ArchiveTile =
        ArchiveTile(
            tileId = tileId,
            coord = coord,
            payload = payload,
            tileType = tileType,
            compression = compression,
            wasDecompressed = false,
            range = this,
        )

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
        if (header.tileType != TileTypeCodes.Mvt || hasVectorLayers) return
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
}

private fun ByteString.decodeMetadataUtf8(): String =
    try {
        toByteArray().decodeToString(throwOnInvalidSequence = true)
    } catch (error: Throwable) {
        throw pmTilesException(
            PmTilesErrorCode.InvalidMetadata,
            "Metadata is not valid UTF-8.",
            error,
        )
    }

private data class LocatedTile(
    val index: Int,
    val range: TileRange,
)

private data class CoalescedTileRead(
    val range: ByteRange,
    val tiles: List<LocatedTile>,
)

private fun List<LocatedTile>.coalescedReads(
    coalescing: TileReadCoalescing
): List<CoalescedTileRead> {
    if (isEmpty()) return emptyList()

    val sortedTiles =
        sortedWith(
            compareBy<LocatedTile> { it.range.archiveRange.offset }
                .thenBy { it.range.archiveRange.length }
                .thenBy { it.index }
        )
    val reads = mutableListOf<CoalescedTileRead>()
    var readStart = sortedTiles.first().range.archiveRange.offset
    var readEnd = sortedTiles.first().range.archiveRange.endOffset()
    val readTiles = mutableListOf<LocatedTile>()

    fun flushRead() {
        reads +=
            CoalescedTileRead(
                range =
                    ByteRange(
                        offset = readStart,
                        length = readEnd - readStart,
                    ),
                tiles = readTiles.toList(),
            )
        readTiles.clear()
    }

    sortedTiles.forEach { tile ->
        val range = tile.range.archiveRange
        if (readTiles.isEmpty()) {
            readStart = range.offset
            readEnd = range.endOffset()
            readTiles += tile
            return@forEach
        }

        if (!canCoalesce(readStart, readEnd, range, coalescing)) {
            flushRead()
            readStart = range.offset
            readEnd = range.endOffset()
        } else {
            readEnd = maxOf(readEnd, range.endOffset())
        }
        readTiles += tile
    }
    if (readTiles.isNotEmpty()) flushRead()

    return reads
}

private fun canCoalesce(
    currentStart: ULong,
    currentEnd: ULong,
    nextRange: ByteRange,
    coalescing: TileReadCoalescing,
): Boolean {
    if (coalescing.maxCoalescedBytes == 0uL) return false

    val nextStart = nextRange.offset
    val nextEnd = nextRange.endOffset()
    val gap = if (nextStart > currentEnd) nextStart - currentEnd else 0uL
    if (gap > coalescing.maxGapBytes) return false

    val mergedLength = maxOf(currentEnd, nextEnd) - currentStart
    return mergedLength <= coalescing.maxCoalescedBytes
}

private fun ByteRange.endOffset(): ULong =
    checkedAdd(offset, length, PmTilesErrorCode.RangeOutOfBounds)

private fun ByteString.sliceTile(readRange: ByteRange, tileRange: TileRange): ByteString {
    val offset = (tileRange.archiveRange.offset - readRange.offset).toInt()
    return substring(offset, offset + tileRange.archiveRange.lengthAsInt("Tile payload"))
}

private fun ByteRange.lengthAsInt(purpose: String): Int =
    allocationLength(length, Int.MAX_VALUE.toULong(), purpose)

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

private fun ByteString.slice(section: ArchiveSection, limits: ArchiveLimits): ByteString {
    val length =
        allocationLength(
            section.length,
            limits.maxDirectoryCompressedBytes,
            "Root directory",
        )
    val offset = section.offset.toInt()
    return substring(offset, offset + length)
}
