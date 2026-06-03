@file:OptIn(
    kotlin.experimental.ExperimentalObjCName::class,
    kotlin.experimental.ExperimentalObjCRefinement::class,
)

package org.maplibre.spatialk.pmtiles

import kotlin.coroutines.cancellation.CancellationException
import kotlin.native.HiddenFromObjC
import kotlin.native.ObjCName

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
    public val tileType: TileType,
    public val internalCompression: Compression,
    public val tileCompression: Compression,
    private val archiveWarnings: List<ArchiveWarning>,
) : AutoCloseable {
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
        ): PmTilesArchive = throw NotImplementedError()
    }
}
