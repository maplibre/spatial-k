package org.maplibre.spatialk.pmtiles

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
    public suspend fun size(): ULong

    /** Reads exactly [range.length] bytes from [range]. */
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
    public fun fromZxy(z: Int, x: Int, y: Int): Long = throw NotImplementedError()

    /** Converts a PMTiles TileID to a Web tile coordinate. */
    public fun toZxy(tileId: Long): TileCoord = throw NotImplementedError()

    /** Returns the first PMTiles TileID for [z]. */
    public fun zoomStart(z: Int): Long = throw NotImplementedError()
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
)

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
