@file:OptIn(
    kotlin.experimental.ExperimentalObjCName::class,
    kotlin.experimental.ExperimentalObjCRefinement::class,
)

package org.maplibre.spatialk.pmtiles

import kotlin.coroutines.cancellation.CancellationException
import kotlin.native.HiddenFromObjC
import kotlin.native.ObjCName
import org.maplibre.spatialk.pmtiles.internal.DecodeLimits
import org.maplibre.spatialk.pmtiles.internal.DecodePurpose
import org.maplibre.spatialk.pmtiles.internal.DirectoryEntry
import org.maplibre.spatialk.pmtiles.internal.FIRST_READ_BYTES
import org.maplibre.spatialk.pmtiles.internal.allocationLength
import org.maplibre.spatialk.pmtiles.internal.decodeCompression
import org.maplibre.spatialk.pmtiles.internal.decodeDirectory
import org.maplibre.spatialk.pmtiles.internal.parseHeader
import org.maplibre.spatialk.pmtiles.internal.readSourceRange
import org.maplibre.spatialk.pmtiles.internal.sourceSize

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
    private val archiveSize: Long,
    private val rootDirectory: List<DirectoryEntry>,
    private val archiveWarnings: List<ArchiveWarning>,
) : AutoCloseable {
    /** Tile payload type from the header. */
    public val tileType: TileType = header.tileType

    /** Compression used for directories and metadata. */
    public val internalCompression: Compression = header.internalCompression

    /** Compression used for tile payloads. */
    public val tileCompression: Compression = header.tileCompression

    /** Returns the raw metadata JSON string. */
    @Throws(PmTilesException::class, CancellationException::class)
    public suspend fun rawMetadataJson(): String = throw NotImplementedError()

    /** Returns typed PMTiles metadata fields. */
    @Throws(PmTilesException::class, CancellationException::class)
    public suspend fun metadata(): ArchiveMetadata = throw NotImplementedError()

    /** Returns the tile at [z], [x], and [y], or null when absent. */
    @Throws(PmTilesException::class, CancellationException::class)
    public suspend fun getTile(z: Int, x: Int, y: Int): ArchiveTile? {
        TileIds.validateZxy(z, x, y)
        throw NotImplementedError()
    }

    /** Returns the tile at [coord], or null when absent. */
    @Throws(PmTilesException::class, CancellationException::class)
    public suspend fun getTile(coord: TileCoord): ArchiveTile? = getTile(coord.z, coord.x, coord.y)

    /** Returns the tile with [tileId], or null when absent. */
    @Throws(PmTilesException::class, CancellationException::class)
    public suspend fun getTileById(tileId: Long): ArchiveTile? {
        TileIds.toZxy(tileId)
        throw NotImplementedError()
    }

    /** Returns the archive byte range for the tile at [z], [x], and [y]. */
    @Throws(PmTilesException::class, CancellationException::class)
    public suspend fun getTileRange(z: Int, x: Int, y: Int): TileRange? {
        TileIds.validateZxy(z, x, y)
        throw NotImplementedError()
    }

    /** Returns compressed tile bytes for the tile at [z], [x], and [y]. */
    @Throws(PmTilesException::class, CancellationException::class)
    public suspend fun getTileCompressed(z: Int, x: Int, y: Int): ArchiveTile? {
        TileIds.validateZxy(z, x, y)
        throw NotImplementedError()
    }

    /** Returns true when the archive contains a tile at [z], [x], and [y]. */
    @Throws(PmTilesException::class, CancellationException::class)
    public suspend fun containsTile(z: Int, x: Int, y: Int): Boolean {
        TileIds.validateZxy(z, x, y)
        throw NotImplementedError()
    }

    /** Number of warnings recorded by this archive. */
    public val warningCount: Int
        get() = archiveWarnings.size

    /** Returns the warning at [index], or null when [index] is outside the warning list. */
    @ObjCName(name = "warningAt", swiftName = "warning")
    public fun warningAt(@ObjCName(name = "at", swiftName = "at") index: Int): ArchiveWarning? =
        archiveWarnings.getOrNull(index)

    /** Returns a snapshot of warnings recorded by this archive. */
    @HiddenFromObjC public fun warnings(): List<ArchiveWarning> = archiveWarnings.toList()

    /** Releases archive-owned caches and in-flight work. */
    override public fun close() {}

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
            val header = parseHeader(initialBytes, sourceSize)
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
                archiveSize = sourceSize.toLong(),
                rootDirectory = rootDirectory,
                archiveWarnings = emptyList(),
            )
        }
    }
}

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
