package org.maplibre.spatialk.pmtiles.internal

import org.maplibre.spatialk.pmtiles.ArchiveHeader
import org.maplibre.spatialk.pmtiles.ArchiveSection
import org.maplibre.spatialk.pmtiles.Clustered
import org.maplibre.spatialk.pmtiles.Compression
import org.maplibre.spatialk.pmtiles.HeaderCounts
import org.maplibre.spatialk.pmtiles.LonLatBounds
import org.maplibre.spatialk.pmtiles.PmTilesErrorCode
import org.maplibre.spatialk.pmtiles.TileCenter
import org.maplibre.spatialk.pmtiles.TileType

internal const val HEADER_BYTES = 127
internal const val FIRST_READ_BYTES = 16 * 1024

internal fun parseHeader(bytes: ByteArray, archiveSize: ULong): ArchiveHeader {
    if (bytes.size < HEADER_BYTES) {
        throw pmTilesException(
            PmTilesErrorCode.InvalidHeader,
            "PMTiles header requires $HEADER_BYTES bytes.",
        )
    }

    val reader = BinaryReader(bytes, PmTilesErrorCode.InvalidHeader)
    validateMagic(reader)
    val specVersion = reader.readUInt8().toInt()
    if (specVersion != SUPPORTED_VERSION) {
        throw pmTilesException(
            PmTilesErrorCode.UnsupportedVersion,
            "PMTiles version $specVersion is not supported.",
        )
    }

    val rootDirectory = reader.readSection()
    val metadata = reader.readSection()
    val leafDirectories = reader.readSection()
    val tileData = reader.readSection()
    val rawAddressedTiles = reader.readULong64Le()
    val rawTileEntries = reader.readULong64Le()
    val rawTileContents = reader.readULong64Le()
    val clustered = reader.readClustered()
    val internalCompression = Compression(reader.readUInt8())
    val tileCompression = Compression(reader.readUInt8())
    val tileType = TileType(reader.readUInt8())
    val minZoom = reader.readUInt8().toInt()
    val maxZoom = reader.readUInt8().toInt()
    val minPosition = reader.readPosition()
    val maxPosition = reader.readPosition()
    val centerZoom = reader.readUInt8().toInt()
    val centerPosition = reader.readPosition()

    val header =
        ArchiveHeader(
            specVersion = specVersion,
            rootDirectory = rootDirectory,
            metadata = metadata,
            leafDirectories = leafDirectories,
            tileData = tileData,
            counts =
                HeaderCounts(
                    addressedTiles = rawAddressedTiles.knownCount(),
                    tileEntries = rawTileEntries.knownCount(),
                    tileContents = rawTileContents.knownCount(),
                    rawAddressedTiles = rawAddressedTiles,
                    rawTileEntries = rawTileEntries,
                    rawTileContents = rawTileContents,
                ),
            clustered = clustered,
            internalCompression = internalCompression,
            tileCompression = tileCompression,
            tileType = tileType,
            minZoom = minZoom,
            maxZoom = maxZoom,
            bounds =
                LonLatBounds(
                    west = minPosition.longitude,
                    south = minPosition.latitude,
                    east = maxPosition.longitude,
                    north = maxPosition.latitude,
                ),
            center =
                TileCenter(
                    longitude = centerPosition.longitude,
                    latitude = centerPosition.latitude,
                    zoom = centerZoom,
                ),
        )

    validateHeader(header, archiveSize)
    return header
}

private fun validateMagic(reader: BinaryReader) {
    MAGIC_BYTES.forEach { expected ->
        val actual = reader.readUInt8().toInt().toByte()
        if (actual != expected) {
            throw pmTilesException(
                PmTilesErrorCode.InvalidMagic,
                "Archive does not start with PMTiles magic bytes.",
            )
        }
    }
}

private fun BinaryReader.readSection(): ArchiveSection =
    ArchiveSection(offset = readULong64Le(), length = readULong64Le())

private fun BinaryReader.readClustered(): Clustered =
    when (val code = readUInt8().toInt()) {
        0 -> Clustered.No
        1 -> Clustered.Yes
        else ->
            throw pmTilesException(
                PmTilesErrorCode.InvalidHeader,
                "Clustered flag $code is invalid.",
            )
    }

private fun BinaryReader.readPosition(): HeaderPosition =
    HeaderPosition(
        longitude = readInt32Le() / POSITION_SCALE,
        latitude = readInt32Le() / POSITION_SCALE,
    )

private fun validateHeader(header: ArchiveHeader, archiveSize: ULong) {
    validateZooms(header)
    validateCoordinates(header)
    validateSections(header, archiveSize)
    validateRootLocation(header.rootDirectory)
}

private fun validateZooms(header: ArchiveHeader) {
    if (
        header.minZoom !in 0..MAX_HEADER_ZOOM ||
            header.maxZoom !in 0..MAX_HEADER_ZOOM ||
            header.center.zoom !in 0..MAX_HEADER_ZOOM ||
            header.maxZoom < header.minZoom
    ) {
        throw pmTilesException(
            PmTilesErrorCode.InvalidHeader,
            "Header zoom fields are outside the supported range.",
        )
    }
}

private fun validateCoordinates(header: ArchiveHeader) {
    val bounds = header.bounds
    if (
        !bounds.west.isLongitude() ||
            !bounds.east.isLongitude() ||
            !bounds.south.isLatitude() ||
            !bounds.north.isLatitude() ||
            !header.center.longitude.isLongitude() ||
            !header.center.latitude.isLatitude() ||
            bounds.east < bounds.west ||
            bounds.north < bounds.south
    ) {
        throw pmTilesException(
            PmTilesErrorCode.InvalidHeader,
            "Header coordinate fields are outside sane longitude/latitude ranges.",
        )
    }
}

private fun validateSections(header: ArchiveHeader, archiveSize: ULong) {
    if (header.rootDirectory.length == 0uL) {
        throw pmTilesException(PmTilesErrorCode.InvalidHeader, "Root directory length is zero.")
    }

    val sections =
        listOf(
                NamedSection("root directory", header.rootDirectory),
                NamedSection("metadata", header.metadata),
                NamedSection("leaf directories", header.leafDirectories),
                NamedSection("tile data", header.tileData),
            )
            .filter { it.section.length > 0uL }

    sections.forEach { named ->
        val end = named.section.endOffset(PmTilesErrorCode.InvalidSectionLayout)
        if (named.section.offset < HEADER_BYTES.toULong()) {
            throw pmTilesException(
                PmTilesErrorCode.InvalidSectionLayout,
                "${named.name} overlaps the fixed header.",
            )
        }
        if (end > archiveSize) {
            throw pmTilesException(
                PmTilesErrorCode.InvalidSectionLayout,
                "${named.name} exceeds archive size $archiveSize.",
            )
        }
    }

    sections
        .sortedBy { it.section.offset }
        .zipWithNext()
        .forEach { (left, right) ->
            if (
                right.section.offset < left.section.endOffset(PmTilesErrorCode.InvalidSectionLayout)
            ) {
                throw pmTilesException(
                    PmTilesErrorCode.InvalidSectionLayout,
                    "${left.name} overlaps ${right.name}.",
                )
            }
        }
}

private fun validateRootLocation(rootDirectory: ArchiveSection) {
    val rootEnd = rootDirectory.endOffset(PmTilesErrorCode.InvalidSectionLayout)
    if (rootEnd > FIRST_READ_BYTES.toULong()) {
        throw pmTilesException(
            PmTilesErrorCode.InvalidRootDirectoryLocation,
            "Root directory is not fully contained in the first $FIRST_READ_BYTES bytes.",
        )
    }
}

private fun ULong.knownCount(): ULong? = if (this == 0uL) null else this

private fun Double.isLongitude(): Boolean = this in -180.0..180.0

private fun Double.isLatitude(): Boolean = this in -90.0..90.0

private data class HeaderPosition(
    val longitude: Double,
    val latitude: Double,
)

private data class NamedSection(
    val name: String,
    val section: ArchiveSection,
)

private const val SUPPORTED_VERSION = 3
private const val MAX_HEADER_ZOOM = 31
private const val POSITION_SCALE = 10_000_000.0
private val MAGIC_BYTES = byteArrayOf(0x50, 0x4d, 0x54, 0x69, 0x6c, 0x65, 0x73)
