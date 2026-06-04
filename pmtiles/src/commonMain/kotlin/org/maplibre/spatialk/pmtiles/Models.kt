package org.maplibre.spatialk.pmtiles

import kotlin.coroutines.cancellation.CancellationException
import org.maplibre.spatialk.pmtiles.internal.pmTilesException

/**
 * Absolute byte range in a PMTiles archive.
 *
 * @property offset Absolute byte offset from the start of the archive.
 * @property length Number of bytes to read.
 */
public data class ByteRange(
    public val offset: ULong,
    public val length: Int,
)

/** Caller-provided byte range source used by the PMTiles reader. */
public interface ByteRangeSource {
    /** Returns the stable archive size in bytes. */
    @Throws(PmTilesException::class, CancellationException::class) public suspend fun size(): ULong

    /** Reads exactly the requested number of bytes from [range]. */
    @Throws(PmTilesException::class, CancellationException::class)
    public suspend fun read(range: ByteRange): ByteArray
}

/**
 * Parsed PMTiles v3 header.
 *
 * @property specVersion PMTiles spec version from the header.
 * @property rootDirectory Root directory section.
 * @property metadata Metadata section.
 * @property leafDirectories Leaf directory section.
 * @property tileData Tile payload section.
 * @property counts Header tile counts.
 * @property clustered Header clustered flag.
 * @property internalCompression Compression used for directories and metadata.
 * @property tileCompression Compression used for tile payloads.
 * @property tileType Tile payload type.
 * @property minZoom Minimum zoom advertised by the archive.
 * @property maxZoom Maximum zoom advertised by the archive.
 * @property bounds Geographic bounds advertised by the archive.
 * @property center Center coordinate advertised by the archive.
 */
public data class ArchiveHeader(
    public val specVersion: Int,
    public val rootDirectory: ArchiveSection,
    public val metadata: ArchiveSection,
    public val leafDirectories: ArchiveSection,
    public val tileData: ArchiveSection,
    public val counts: HeaderCounts,
    public val clustered: Clustered,
    public val internalCompression: Compression,
    public val tileCompression: Compression,
    public val tileType: TileType,
    public val minZoom: Int,
    public val maxZoom: Int,
    public val bounds: LonLatBounds,
    public val center: TileCenter,
)

/**
 * Header section location and size.
 *
 * @property offset Absolute byte offset from the start of the archive.
 * @property length Section length in bytes.
 */
public data class ArchiveSection(
    public val offset: ULong,
    public val length: ULong,
)

/**
 * PMTiles header counts.
 *
 * @property addressedTiles Semantic addressed tile count, or null when unknown.
 * @property tileEntries Semantic tile-entry count, or null when unknown.
 * @property tileContents Semantic tile-content count, or null when unknown.
 * @property rawAddressedTiles Raw addressed tile count value from the header.
 * @property rawTileEntries Raw tile-entry count value from the header.
 * @property rawTileContents Raw tile-content count value from the header.
 */
public data class HeaderCounts(
    public val addressedTiles: ULong?,
    public val tileEntries: ULong?,
    public val tileContents: ULong?,
    public val rawAddressedTiles: ULong,
    public val rawTileEntries: ULong,
    public val rawTileContents: ULong,
)

/**
 * PMTiles clustered flag.
 *
 * @property value True when the archive declares clustered tile ordering.
 */
public data class Clustered(public val value: Boolean) {
    /** Clustered flag constants. */
    public companion object {
        /** Archive does not declare clustered tile ordering. */
        public val No: Clustered = Clustered(false)

        /** Archive declares clustered tile ordering. */
        public val Yes: Clustered = Clustered(true)
    }
}

/**
 * Geographic longitude/latitude bounds.
 *
 * @property west Western longitude.
 * @property south Southern latitude.
 * @property east Eastern longitude.
 * @property north Northern latitude.
 */
public data class LonLatBounds(
    public val west: Double,
    public val south: Double,
    public val east: Double,
    public val north: Double,
)

/**
 * Geographic center point and zoom.
 *
 * @property longitude Center longitude.
 * @property latitude Center latitude.
 * @property zoom Center zoom.
 */
public data class TileCenter(
    public val longitude: Double,
    public val latitude: Double,
    public val zoom: Int,
)

/**
 * PMTiles compression code.
 *
 * @property code Raw PMTiles compression code.
 */
public data class Compression(public val code: UInt) {
    /** Known PMTiles compression constants. */
    public companion object {
        /** Unknown compression code. */
        public val Unknown: Compression = Compression(0u)

        /** No compression. */
        public val None: Compression = Compression(1u)

        /** gzip compression. */
        public val Gzip: Compression = Compression(2u)

        /** brotli compression. */
        public val Brotli: Compression = Compression(3u)

        /** zstd compression. */
        public val Zstd: Compression = Compression(4u)
    }
}

/**
 * PMTiles tile type code.
 *
 * @property code Raw PMTiles tile type code.
 */
public data class TileType(public val code: UInt) {
    /** Known PMTiles tile type constants. */
    public companion object {
        /** Unknown tile type. */
        public val Unknown: TileType = TileType(0u)

        /** Mapbox Vector Tile payload. */
        public val Mvt: TileType = TileType(1u)

        /** PNG raster payload. */
        public val Png: TileType = TileType(2u)

        /** JPEG raster payload. */
        public val Jpeg: TileType = TileType(3u)

        /** WebP raster payload. */
        public val Webp: TileType = TileType(4u)

        /** AVIF raster payload. */
        public val Avif: TileType = TileType(5u)

        /** MapLibre Tile payload. */
        public val Mlt: TileType = TileType(6u)
    }
}

/**
 * Web tile coordinate.
 *
 * @property z Zoom level.
 * @property x Tile column.
 * @property y Tile row.
 */
public data class TileCoord(
    public val z: Int,
    public val x: Int,
    public val y: Int,
)

/** PMTiles TileID conversion utilities. */
public object TileIds {
    /** Converts a Web tile coordinate to a PMTiles TileID. */
    @Throws(PmTilesException::class)
    public fun fromZxy(z: Int, x: Int, y: Int): Long {
        validateZxy(z, x, y)
        return zoomStart(z) + hilbertIndex(z, x, y)
    }

    /** Converts a PMTiles TileID to a Web tile coordinate. */
    @Throws(PmTilesException::class)
    public fun toZxy(tileId: Long): TileCoord {
        if (tileId < 0 || tileId > MAX_SUPPORTED_TILE_ID) {
            throw invalidTileCoordinate("TileID $tileId is outside the supported range.")
        }

        var z = 0
        while (z < MAX_ZOOM && tileId >= zoomStart(z + 1)) {
            z += 1
        }

        val position = tileId - zoomStart(z)
        val (x, y) = hilbertCoordinate(z, position)
        return TileCoord(z = z, x = x, y = y)
    }

    /** Returns the first PMTiles TileID for [z]. */
    @Throws(PmTilesException::class)
    public fun zoomStart(z: Int): Long {
        validateZoom(z)
        return if (z == 0) 0 else ((1L shl (2 * z)) - 1) / 3
    }

    internal fun validateZxy(z: Int, x: Int, y: Int) {
        validateZoom(z)
        val limit = 1L shl z
        if (x < 0 || y < 0 || x.toLong() >= limit || y.toLong() >= limit) {
            throw invalidTileCoordinate(
                "Tile coordinate z=$z x=$x y=$y is outside the supported range."
            )
        }
    }

    private fun validateZoom(z: Int) {
        if (z !in 0..MAX_ZOOM) {
            throw invalidTileCoordinate("Zoom $z is outside the supported range 0..$MAX_ZOOM.")
        }
    }

    private fun hilbertIndex(z: Int, x: Int, y: Int): Long {
        if (z == 0) return 0

        var mutableX = x
        var mutableY = y
        var index = 0L
        var scale = 1 shl (z - 1)

        while (scale > 0) {
            val rx = if ((mutableX and scale) > 0) 1 else 0
            val ry = if ((mutableY and scale) > 0) 1 else 0
            index += scale.toLong() * scale * ((3 * rx) xor ry)

            if (ry == 0) {
                if (rx == 1) {
                    mutableX = scale - 1 - mutableX
                    mutableY = scale - 1 - mutableY
                }

                val nextX = mutableY
                mutableY = mutableX
                mutableX = nextX
            }

            scale /= 2
        }

        return index
    }

    private fun hilbertCoordinate(z: Int, position: Long): Pair<Int, Int> {
        var remaining = position
        var x = 0L
        var y = 0L
        var scale = 1L

        while (scale < (1L shl z)) {
            val rx = ((remaining / 2) and 1).toInt()
            val ry = ((remaining xor rx.toLong()) and 1).toInt()

            if (ry == 0) {
                if (rx == 1) {
                    x = scale - 1 - x
                    y = scale - 1 - y
                }

                val nextX = y
                y = x
                x = nextX
            }

            x += scale * rx
            y += scale * ry
            remaining /= 4
            scale *= 2
        }

        return x.toInt() to y.toInt()
    }

    private fun invalidTileCoordinate(message: String): PmTilesException =
        pmTilesException(PmTilesErrorCode.InvalidTileCoordinate, message)

    private const val MAX_ZOOM = 31
    private val MAX_SUPPORTED_TILE_ID: Long = zoomStart(MAX_ZOOM) + (1L shl (2 * MAX_ZOOM)) - 1
}

/**
 * Located tile byte range.
 *
 * @property tileId PMTiles TileID for the requested tile.
 * @property coord Web tile coordinate for the requested tile.
 * @property archiveRange Absolute archive byte range containing the tile payload.
 * @property tileType Tile payload type.
 * @property compression Tile payload compression.
 * @property directoryDepth Directory traversal depth used to find the tile.
 */
public data class TileRange(
    public val tileId: Long,
    public val coord: TileCoord,
    public val archiveRange: ByteRange,
    public val tileType: TileType,
    public val compression: Compression,
    public val directoryDepth: Int,
)

/**
 * Tile payload returned by the archive reader.
 *
 * @property tileId PMTiles TileID for the tile.
 * @property coord Web tile coordinate for the tile.
 * @property bytes Tile payload bytes.
 * @property tileType Tile payload type.
 * @property compression Compression represented by [bytes].
 * @property wasDecompressed True when [bytes] were decompressed by the reader.
 * @property range Located source range for this tile.
 */
public data class ArchiveTile(
    public val tileId: Long,
    public val coord: TileCoord,
    public val bytes: ByteArray,
    public val tileType: TileType,
    public val compression: Compression,
    public val wasDecompressed: Boolean,
    public val range: TileRange,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ArchiveTile

        if (tileId != other.tileId) return false
        if (wasDecompressed != other.wasDecompressed) return false
        if (coord != other.coord) return false
        if (!bytes.contentEquals(other.bytes)) return false
        if (tileType != other.tileType) return false
        if (compression != other.compression) return false
        if (range != other.range) return false

        return true
    }

    override fun hashCode(): Int {
        var result = tileId.hashCode()
        result = 31 * result + wasDecompressed.hashCode()
        result = 31 * result + coord.hashCode()
        result = 31 * result + bytes.contentHashCode()
        result = 31 * result + tileType.hashCode()
        result = 31 * result + compression.hashCode()
        result = 31 * result + range.hashCode()
        return result
    }
}

/**
 * Typed PMTiles metadata fields.
 *
 * @property name Tileset name.
 * @property description Tileset description.
 * @property attribution Tileset attribution.
 * @property type Tileset kind.
 * @property version Tileset version.
 * @property encoding PMTiles-defined encoding string.
 * @property vectorLayersJson Raw JSON for the `vector_layers` value.
 */
public data class ArchiveMetadata(
    public val name: String?,
    public val description: String?,
    public val attribution: String?,
    public val type: TilesetKind?,
    public val version: String?,
    public val encoding: String?,
    public val vectorLayersJson: String?,
)

/**
 * PMTiles metadata tileset kind.
 *
 * @property value Raw metadata `type` string.
 */
public data class TilesetKind(public val value: String) {
    /** Known tileset kind constants. */
    public companion object {
        /** Overlay tileset. */
        public val Overlay: TilesetKind = TilesetKind("overlay")

        /** Base layer tileset. */
        public val BaseLayer: TilesetKind = TilesetKind("baselayer")
    }
}

/**
 * Warning recorded by a lenient archive operation.
 *
 * @property code Stable warning code.
 * @property message Human-readable warning message.
 * @property context Optional warning context.
 */
public data class ArchiveWarning(
    public val code: ArchiveWarningCode,
    public val message: String,
    public val context: String? = null,
)
