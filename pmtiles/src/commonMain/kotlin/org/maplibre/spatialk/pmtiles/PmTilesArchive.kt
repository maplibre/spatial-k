@file:OptIn(
    kotlin.concurrent.atomics.ExperimentalAtomicApi::class,
    kotlin.experimental.ExperimentalObjCName::class,
    kotlin.experimental.ExperimentalObjCRefinement::class,
)

package org.maplibre.spatialk.pmtiles

import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.AtomicReference
import kotlin.coroutines.cancellation.CancellationException
import kotlin.native.HiddenFromObjC
import kotlin.native.ObjCName
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.maplibre.spatialk.pmtiles.internal.DecodeLimits
import org.maplibre.spatialk.pmtiles.internal.DecodePurpose
import org.maplibre.spatialk.pmtiles.internal.DirectoryEntry
import org.maplibre.spatialk.pmtiles.internal.FIRST_READ_BYTES
import org.maplibre.spatialk.pmtiles.internal.allocationLength
import org.maplibre.spatialk.pmtiles.internal.checkedAdd
import org.maplibre.spatialk.pmtiles.internal.coversTile
import org.maplibre.spatialk.pmtiles.internal.decodeCompression
import org.maplibre.spatialk.pmtiles.internal.decodeDirectory
import org.maplibre.spatialk.pmtiles.internal.findPredecessor
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
    private val rootDirectory: List<DirectoryEntry>,
    initialWarnings: List<ArchiveWarning>,
) : AutoCloseable {
    private val stateMutex = Mutex()
    private val closed = AtomicBoolean(false)
    private val archiveWarnings = AtomicReference(initialWarnings)
    private val leafDirectoryCache = LinkedHashMap<ByteRange, List<DirectoryEntry>>()
    private val inFlightSourceReads = mutableMapOf<SourceReadKey, CompletableDeferred<ByteArray>>()
    private var rawMetadataJsonCache: String? = null
    private var metadataCache: ArchiveMetadata? = null

    /** Tile payload type from the header. */
    public val tileType: TileType = header.tileType

    /** Compression used for directories and metadata. */
    public val internalCompression: Compression = header.internalCompression

    /** Compression used for tile payloads. */
    public val tileCompression: Compression = header.tileCompression

    /** Returns the raw metadata JSON string. */
    @Throws(PmTilesException::class, CancellationException::class)
    public suspend fun rawMetadataJson(): String {
        cachedRawMetadataJson()?.let {
            return it
        }

        val json =
            if (header.metadata.length == 0uL) {
                ""
            } else {
                val compressedBytes =
                    readSourceRangeDeduplicated(
                        header.metadata.toByteRange(
                            options.limits.maxMetadataBytes,
                            "Metadata",
                        ),
                        maxBytes = options.limits.maxMetadataBytes,
                    )
                val metadataBytes =
                    decodeCompression(
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

        return cacheRawMetadataJson(json)
    }

    /** Returns typed PMTiles metadata fields. */
    @Throws(PmTilesException::class, CancellationException::class)
    public suspend fun metadata(): ArchiveMetadata {
        cachedMetadata()?.let {
            return it
        }

        val rawJson = rawMetadataJson()
        return parseAndCacheMetadata(rawJson)
    }

    /** Returns the tile at [z], [x], and [y], or null when absent. */
    @Throws(PmTilesException::class, CancellationException::class)
    public suspend fun getTile(z: Int, x: Int, y: Int): ArchiveTile? {
        TileIds.validateZxy(z, x, y)
        return readTile(
            tileId = TileIds.fromZxy(z, x, y),
            coord = TileCoord(z = z, x = x, y = y),
            readMode = options.tileReadMode,
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
            readMode = options.tileReadMode,
        )
    }

    /** Returns the archive byte range for the tile at [z], [x], and [y]. */
    @Throws(PmTilesException::class, CancellationException::class)
    public suspend fun getTileRange(z: Int, x: Int, y: Int): TileRange? {
        TileIds.validateZxy(z, x, y)
        return findTileRange(
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
            readMode = TileReadMode.CompressedBytes,
        )
    }

    /** Returns true when the archive contains a tile at [z], [x], and [y]. */
    @Throws(PmTilesException::class, CancellationException::class)
    public suspend fun containsTile(z: Int, x: Int, y: Int): Boolean {
        return getTileRange(z, x, y) != null
    }

    /** Number of warnings recorded by this archive. */
    public val warningCount: Int
        get() = archiveWarnings.load().size

    /** Returns the warning at [index], or null when [index] is outside the warning list. */
    @ObjCName(name = "warningAt", swiftName = "warning")
    public fun warningAt(@ObjCName(name = "at", swiftName = "at") index: Int): ArchiveWarning? =
        archiveWarnings.load().getOrNull(index)

    /** Returns a snapshot of warnings recorded by this archive. */
    @HiddenFromObjC public fun warnings(): List<ArchiveWarning> = archiveWarnings.load()

    /** Releases archive-owned caches and in-flight work. */
    override public fun close() {
        if (!closed.compareAndSet(expectedValue = false, newValue = true)) return
        if (stateMutex.tryLock()) {
            try {
                clearStateForCloseLocked()
            } finally {
                stateMutex.unlock()
            }
        }
    }

    private suspend fun findTileRange(tileId: Long, coord: TileCoord): TileRange? {
        checkOpen()
        return findTileRange(
            directory = rootDirectory,
            tileId = tileId,
            coord = coord,
            depth = 0,
            visitedLeafRanges = mutableSetOf(),
        )
    }

    private suspend fun readTile(
        tileId: Long,
        coord: TileCoord,
        readMode: TileReadMode,
    ): ArchiveTile? {
        val range = findTileRange(tileId, coord) ?: return null
        val compressedTile = readCompressedTile(range)
        return when (readMode) {
            TileReadMode.CompressedBytes -> compressedTile
            TileReadMode.DecompressedBytes -> compressedTile.decompressed()
        }
    }

    private suspend fun readCompressedTile(range: TileRange): ArchiveTile {
        val bytes =
            readSourceRangeDeduplicated(
                range.archiveRange,
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

    private fun ArchiveTile.decompressed(): ArchiveTile {
        if (compression == Compression.None) return this

        val decompressedBytes =
            decodeCompression(
                compression,
                bytes,
                DecodeLimits(
                    maxCompressedBytes = options.limits.maxTileCompressedBytes,
                    maxDecompressedBytes = options.limits.maxTileDecompressedBytes,
                    purpose = DecodePurpose.Tile,
                ),
            )
        return copy(
            bytes = decompressedBytes,
            compression = Compression.None,
            wasDecompressed = true,
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
        appendWarningLocked(
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
        appendWarningLocked(
            ArchiveWarning(
                code = ArchiveWarningCode.InvalidMetadataRecovered,
                message = message,
            )
        )
    }

    private suspend fun findTileRange(
        directory: List<DirectoryEntry>,
        tileId: Long,
        coord: TileCoord,
        depth: Int,
        visitedLeafRanges: MutableSet<ByteRange>,
    ): TileRange? {
        val entry = directory.findPredecessor(tileId) ?: return null

        if (entry.isTile) {
            return if (entry.coversTile(tileId)) entry.toTileRange(tileId, coord, depth) else null
        }

        val leafDepth = depth + 1
        val leaf = loadLeafDirectory(entry, leafDepth, visitedLeafRanges)
        return findTileRange(leaf, tileId, coord, leafDepth, visitedLeafRanges)
    }

    private suspend fun loadLeafDirectory(
        entry: DirectoryEntry,
        depth: Int,
        visitedLeafRanges: MutableSet<ByteRange>,
    ): List<DirectoryEntry> {
        if (depth > options.limits.maxDirectoryDepth) {
            throw pmTilesException(
                PmTilesErrorCode.LimitExceeded,
                "Directory depth $depth exceeds limit ${options.limits.maxDirectoryDepth}.",
            )
        }
        if (depth > 1) {
            if (options.validationMode == ValidationMode.Strict) {
                throw pmTilesException(
                    PmTilesErrorCode.InvalidDirectory,
                    "Nested leaf directories are not allowed in strict mode.",
                )
            }
            appendWarningLocked(
                ArchiveWarning(
                    code = ArchiveWarningCode.NestedLeafDirectory,
                    message = "Lookup traversed a nested leaf directory.",
                    context = "depth=$depth tileId=${entry.tileId}",
                )
            )
        }

        val range = entry.toLeafRange()
        if (!visitedLeafRanges.add(range)) {
            throw pmTilesException(
                PmTilesErrorCode.LimitExceeded,
                "Lookup revisited leaf directory range $range.",
            )
        }

        cachedLeafDirectory(range)?.let {
            return it
        }

        val compressedBytes =
            readSourceRangeDeduplicated(
                range,
                maxBytes = options.limits.maxDirectoryCompressedBytes,
            )
        val directoryBytes =
            decodeCompression(
                header.internalCompression,
                compressedBytes,
                DecodeLimits(
                    maxCompressedBytes = options.limits.maxDirectoryCompressedBytes,
                    maxDecompressedBytes = options.limits.maxDirectoryDecompressedBytes,
                    purpose = DecodePurpose.LeafDirectory,
                ),
            )
        val directory = decodeDirectory(directoryBytes, header, options.limits)
        return cacheLeafDirectory(range, directory)
    }

    private fun DirectoryEntry.toTileRange(tileId: Long, coord: TileCoord, depth: Int): TileRange =
        TileRange(
            tileId = tileId,
            coord = coord,
            archiveRange =
                ByteRange(
                    offset =
                        checkedAdd(
                            header.tileData.offset,
                            offset,
                            PmTilesErrorCode.InvalidDirectory,
                        ),
                    length = length,
                ),
            tileType = header.tileType,
            compression = header.tileCompression,
            directoryDepth = depth,
        )

    private fun DirectoryEntry.toLeafRange(): ByteRange =
        ByteRange(
            offset =
                checkedAdd(
                    header.leafDirectories.offset,
                    offset,
                    PmTilesErrorCode.InvalidDirectory,
                ),
            length = length,
        )

    private fun checkOpen() {
        if (closed.load()) {
            throw pmTilesException(PmTilesErrorCode.Closed, "PMTiles archive is closed.")
        }
    }

    private fun checkOpenLocked() {
        if (closed.load()) {
            clearStateForCloseLocked()
            throw closedException()
        }
    }

    private fun closedException(): PmTilesException =
        pmTilesException(PmTilesErrorCode.Closed, "PMTiles archive is closed.")

    private suspend fun cachedRawMetadataJson(): String? = stateMutex.withLock {
        checkOpenLocked()
        rawMetadataJsonCache
    }

    private suspend fun cacheRawMetadataJson(json: String): String = stateMutex.withLock {
        checkOpenLocked()
        rawMetadataJsonCache ?: json.also { rawMetadataJsonCache = it }
    }

    private suspend fun cachedMetadata(): ArchiveMetadata? = stateMutex.withLock {
        checkOpenLocked()
        metadataCache
    }

    private suspend fun parseAndCacheMetadata(rawJson: String): ArchiveMetadata =
        stateMutex.withLock {
            checkOpenLocked()
            metadataCache ?: parseMetadata(rawJson).also { metadataCache = it }
        }

    private suspend fun cachedLeafDirectory(range: ByteRange): List<DirectoryEntry>? =
        stateMutex.withLock {
            checkOpenLocked()
            val cached = leafDirectoryCache.remove(range) ?: return@withLock null
            leafDirectoryCache[range] = cached
            cached
        }

    private suspend fun cacheLeafDirectory(
        range: ByteRange,
        directory: List<DirectoryEntry>,
    ): List<DirectoryEntry> = stateMutex.withLock {
        checkOpenLocked()
        leafDirectoryCache.remove(range)?.let { cached ->
            leafDirectoryCache[range] = cached
            return@withLock cached
        }
        if (options.limits.maxLeafDirectoryCacheEntries > 0) {
            leafDirectoryCache[range] = directory
            while (leafDirectoryCache.size > options.limits.maxLeafDirectoryCacheEntries) {
                leafDirectoryCache.remove(leafDirectoryCache.keys.first())
            }
        }
        directory
    }

    private suspend fun readSourceRangeDeduplicated(
        range: ByteRange,
        maxBytes: Int,
    ): ByteArray {
        val key = SourceReadKey(range = range, maxBytes = maxBytes)
        var ownsRead = false
        val inFlight = stateMutex.withLock {
            checkOpenLocked()
            inFlightSourceReads[key]?.let {
                return@withLock it
            }
            CompletableDeferred<ByteArray>().also {
                inFlightSourceReads[key] = it
                ownsRead = true
            }
        }

        if (!ownsRead) return inFlight.await()

        return try {
            val bytes =
                source.readSourceRange(
                    range,
                    archiveSize = archiveSize,
                    maxBytes = maxBytes,
                )
            completeSourceRead(key, inFlight, bytes)
            bytes
        } catch (error: Throwable) {
            val completionError = failSourceRead(key, inFlight, error)
            throw completionError
        }
    }

    private suspend fun completeSourceRead(
        key: SourceReadKey,
        inFlight: CompletableDeferred<ByteArray>,
        bytes: ByteArray,
    ) {
        stateMutex.withLock {
            inFlightSourceReads.remove(key)
            if (closed.load()) {
                clearStateForCloseLocked()
                val error = closedException()
                inFlight.completeExceptionally(error)
                throw error
            }
            inFlight.complete(bytes)
        }
    }

    private suspend fun failSourceRead(
        key: SourceReadKey,
        inFlight: CompletableDeferred<ByteArray>,
        error: Throwable,
    ): Throwable =
        withContext(NonCancellable) {
            stateMutex.withLock {
                inFlightSourceReads.remove(key)
                val completionError =
                    if (closed.load() && error !is CancellationException) {
                        closedException()
                    } else {
                        error
                    }
                if (closed.load()) clearStateForCloseLocked()
                inFlight.completeExceptionally(completionError)
                completionError
            }
        }

    private fun appendWarningLocked(warning: ArchiveWarning) {
        archiveWarnings.store(archiveWarnings.load() + warning)
    }

    private fun clearStateForCloseLocked() {
        rawMetadataJsonCache = null
        metadataCache = null
        leafDirectoryCache.clear()
        val error = closedException()
        inFlightSourceReads.values.forEach { it.completeExceptionally(error) }
        inFlightSourceReads.clear()
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
            val compressedRoot = initialBytes.slice(header.rootDirectory, options.limits)
            val rootDirectoryBytes =
                decodeCompression(
                    header.internalCompression,
                    compressedRoot,
                    DecodeLimits(
                        maxCompressedBytes = options.limits.maxDirectoryCompressedBytes,
                        maxDecompressedBytes = options.limits.maxDirectoryDecompressedBytes,
                        purpose = DecodePurpose.RootDirectory,
                    ),
                )
            val rootDirectory = decodeDirectory(rootDirectoryBytes, header, options.limits)

            return PmTilesArchive(
                header = header,
                source = source,
                options = options,
                archiveSize = sourceSize,
                rootDirectory = rootDirectory,
                initialWarnings = parsedHeader.warnings,
            )
        }
    }
}

private data class SourceReadKey(
    val range: ByteRange,
    val maxBytes: Int,
)

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
