@file:OptIn(ExperimentalObjCName::class, ExperimentalObjCRefinement::class)

package org.maplibre.spatialk.pmtiles

import kotlin.coroutines.cancellation.CancellationException
import kotlin.experimental.ExperimentalObjCName
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.jvm.JvmInline
import kotlin.native.HiddenFromObjC
import kotlin.native.ObjCName
import kotlin.native.ShouldRefineInSwift
import kotlinx.io.bytestring.ByteString
import org.maplibre.spatialk.pmtiles.internal.pmTilesException

/**
 * Absolute byte range in a PMTiles archive.
 *
 * @property offset Absolute byte offset from the start of the archive.
 * @property length Number of bytes to read.
 */
public data class ByteRange
internal constructor(
    public val offset: ULong,
    public val length: ULong,
)

/** Caller-provided byte range source used by the PMTiles reader. */
@ObjCName(swiftName = "KotlinByteRangeSource")
public interface ByteRangeSource {
    /** Returns the stable archive size in bytes. */
    @Throws(PmTilesException::class, CancellationException::class) public suspend fun size(): ULong

    /** Reads exactly the requested number of bytes from [range]. */
    @Throws(PmTilesException::class, CancellationException::class)
    public suspend fun read(range: ByteRange): ByteString
}

/** Caller-provided append-only byte sink used by the PMTiles writer. */
@ObjCName(swiftName = "KotlinByteSink")
public interface ByteSink {
    /** Appends [bytes] to the output in write order. */
    @Throws(PmTilesException::class, CancellationException::class)
    public suspend fun write(bytes: ByteString)

    /** Flushes pending output. */
    @Throws(PmTilesException::class, CancellationException::class) public suspend fun flush()

    /** Closes the output. */
    @Throws(PmTilesException::class, CancellationException::class) public suspend fun close()
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
 * @property isClustered True when the archive declares clustered tile ordering.
 * @property internalCompression CompressionCode used for directories and metadata.
 * @property tileCompression CompressionCode used for tile payloads.
 * @property tileType Tile payload type.
 * @property minZoom Minimum zoom advertised by the archive.
 * @property maxZoom Maximum zoom advertised by the archive.
 * @property bounds Geographic bounds advertised by the archive.
 * @property center Center coordinate advertised by the archive.
 */
public data class ArchiveHeader
internal constructor(
    public val specVersion: Int,
    public val rootDirectory: ArchiveSection,
    public val metadata: ArchiveSection,
    public val leafDirectories: ArchiveSection,
    public val tileData: ArchiveSection,
    public val counts: HeaderCounts,
    public val isClustered: Boolean,
    @ShouldRefineInSwift public val internalCompression: CompressionCode,
    @ShouldRefineInSwift public val tileCompression: CompressionCode,
    @ShouldRefineInSwift public val tileType: TileTypeCode,
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
public data class ArchiveSection
internal constructor(
    public val offset: ULong,
    public val length: ULong,
)

/**
 * PMTiles header counts.
 *
 * @property addressedTiles Addressed tile count from the header. PMTiles uses zero as the "unknown
 *   count" sentinel.
 * @property tileEntries Tile-entry count from the header. PMTiles uses zero as the "unknown count"
 *   sentinel.
 * @property tileContents Tile-content count from the header. PMTiles uses zero as the "unknown
 *   count" sentinel.
 */
public data class HeaderCounts
internal constructor(
    public val addressedTiles: ULong,
    public val tileEntries: ULong,
    public val tileContents: ULong,
)

/**
 * Geographic longitude/latitude bounds.
 *
 * @property west Western longitude.
 * @property south Southern latitude.
 * @property east Eastern longitude.
 * @property north Northern latitude.
 */
public data class LonLatBounds
internal constructor(
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
public data class TileCenter
internal constructor(
    public val longitude: Double,
    public val latitude: Double,
    public val zoom: Int,
)

/**
 * PMTiles compression code.
 *
 * PMTiles can contain future compression codes, so this type stores the raw code while providing
 * constants for currently defined codes.
 *
 * @property code Raw PMTiles compression code.
 */
@JvmInline public value class CompressionCode(public val code: UInt)

/** PMTiles compression-code constants. */
public object CompressionCodes {
    /** Unknown compression code. */
    @ShouldRefineInSwift
    @ObjCName(swiftName = "unknown")
    public val Unknown: CompressionCode = CompressionCode(0u)

    /** No compression. */
    @ShouldRefineInSwift
    @ObjCName(swiftName = "none")
    public val None: CompressionCode = CompressionCode(1u)

    /** gzip compression. */
    @ShouldRefineInSwift
    @ObjCName(swiftName = "gzip")
    public val Gzip: CompressionCode = CompressionCode(2u)

    /** brotli compression. */
    @ShouldRefineInSwift
    @ObjCName(swiftName = "brotli")
    public val Brotli: CompressionCode = CompressionCode(3u)

    /** zstd compression. */
    @ShouldRefineInSwift
    @ObjCName(swiftName = "zstd")
    public val Zstd: CompressionCode = CompressionCode(4u)
}

/**
 * PMTiles tile type code.
 *
 * PMTiles can contain future tile type codes, so this type stores the raw code while providing
 * constants for currently defined codes.
 *
 * @property code Raw PMTiles tile type code.
 */
@JvmInline public value class TileTypeCode(public val code: UInt)

/** PMTiles tile-type-code constants. */
public object TileTypeCodes {
    /** Unknown tile type. */
    @ShouldRefineInSwift
    @ObjCName(swiftName = "unknown")
    public val Unknown: TileTypeCode = TileTypeCode(0u)

    /** Mapbox Vector Tile payload. */
    @ShouldRefineInSwift
    @ObjCName(swiftName = "mvt")
    public val Mvt: TileTypeCode = TileTypeCode(1u)

    /** PNG raster payload. */
    @ShouldRefineInSwift
    @ObjCName(swiftName = "png")
    public val Png: TileTypeCode = TileTypeCode(2u)

    /** JPEG raster payload. */
    @ShouldRefineInSwift
    @ObjCName(swiftName = "jpeg")
    public val Jpeg: TileTypeCode = TileTypeCode(3u)

    /** WebP raster payload. */
    @ShouldRefineInSwift
    @ObjCName(swiftName = "webp")
    public val Webp: TileTypeCode = TileTypeCode(4u)

    /** AVIF raster payload. */
    @ShouldRefineInSwift
    @ObjCName(swiftName = "avif")
    public val Avif: TileTypeCode = TileTypeCode(5u)

    /** MapLibre Tile payload. */
    @ShouldRefineInSwift
    @ObjCName(swiftName = "mlt")
    public val Mlt: TileTypeCode = TileTypeCode(6u)
}

/**
 * Web tile coordinate.
 *
 * @property z Zoom level.
 * @property x Tile column.
 * @property y Tile row.
 */
public data class TileCoord
@Throws(PmTilesException::class)
constructor(
    public val z: Int,
    public val x: Int,
    public val y: Int,
) {
    init {
        TileIds.validateZxy(z, x, y)
    }

    /** Converts this Web tile coordinate to a PMTiles TileID. */
    @Throws(PmTilesException::class) public fun toTileId(): Long = TileIds.fromZxy(z, x, y)
}

/** PMTiles TileID conversion utilities. */
@OptIn(ExperimentalObjCRefinement::class)
@HiddenFromObjC
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
private val MAX_SUPPORTED_TILE_ID: Long = TileIds.zoomStart(MAX_ZOOM) + (1L shl (2 * MAX_ZOOM)) - 1

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
public data class TileRange
internal constructor(
    public val tileId: Long,
    public val coord: TileCoord,
    public val archiveRange: ByteRange,
    @ShouldRefineInSwift public val tileType: TileTypeCode,
    @ShouldRefineInSwift public val compression: CompressionCode,
    public val directoryDepth: Int,
)

/**
 * Tile payload returned by the archive reader.
 *
 * @property tileId PMTiles TileID for the tile.
 * @property coord Web tile coordinate for the tile.
 * @property payload Immutable tile payload bytes.
 * @property tileType Tile payload type.
 * @property compression CompressionCode represented by the payload bytes.
 * @property wasDecompressed True when the payload bytes were decompressed by the reader.
 * @property range Located source range for this tile.
 */
public data class ArchiveTile
internal constructor(
    public val tileId: Long,
    public val coord: TileCoord,
    @ShouldRefineInSwift public val payload: ByteString,
    @ShouldRefineInSwift public val tileType: TileTypeCode,
    @ShouldRefineInSwift public val compression: CompressionCode,
    public val wasDecompressed: Boolean,
    public val range: TileRange,
)

/**
 * Result for one coordinate in a batch tile read.
 *
 * @property coord Requested tile coordinate.
 * @property tile Tile payload, or null when the archive does not contain [coord].
 */
public data class TileReadResult
internal constructor(
    public val coord: TileCoord,
    public val tile: ArchiveTile?,
) {
    /** True when [tile] is present. */
    public val isFound: Boolean
        get() = tile != null
}

/**
 * Typed PMTiles metadata fields.
 *
 * @property name Tileset name.
 * @property description PMTiles metadata `description`.
 * @property attribution Tileset attribution.
 * @property type Tileset kind.
 * @property version Tileset version.
 * @property encoding PMTiles-defined encoding string.
 * @property vectorLayersJson Raw JSON for the `vector_layers` value.
 */
public data class ArchiveMetadata
internal constructor(
    public val name: String?,
    @property:ObjCName(swiftName = "archiveDescription") public val description: String?,
    public val attribution: String?,
    public val type: String?,
    public val version: String?,
    public val encoding: String?,
    public val vectorLayersJson: String?,
)

/**
 * Warning recorded by a lenient archive operation.
 *
 * @property code Stable warning code.
 * @property message Human-readable warning message.
 * @property context Optional warning context.
 */
public data class ArchiveWarning
internal constructor(
    public val code: ArchiveWarningCode,
    public val message: String,
    public val context: String? = null,
)
