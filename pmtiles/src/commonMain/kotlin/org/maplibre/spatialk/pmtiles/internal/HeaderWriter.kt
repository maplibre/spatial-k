package org.maplibre.spatialk.pmtiles.internal

import kotlinx.io.bytestring.ByteString
import org.maplibre.spatialk.pmtiles.ArchiveHeader
import org.maplibre.spatialk.pmtiles.ArchiveSection

internal fun encodeHeader(header: ArchiveHeader, archiveSize: ULong): ByteString {
    validateHeader(header, archiveSize)
    return buildBinary {
            writeMagic()
            writeUInt8(SUPPORTED_VERSION.toUInt())
            writeSection(header.rootDirectory)
            writeSection(header.metadata)
            writeSection(header.leafDirectories)
            writeSection(header.tileData)
            writeULong64Le(header.counts.addressedTiles)
            writeULong64Le(header.counts.tileEntries)
            writeULong64Le(header.counts.tileContents)
            writeUInt8(if (header.isClustered) 1u else 0u)
            writeUInt8(header.internalCompression.code)
            writeUInt8(header.tileCompression.code)
            writeUInt8(header.tileType.code)
            writeUInt8(header.minZoom.toUInt())
            writeUInt8(header.maxZoom.toUInt())
            writePosition(header.bounds.west, header.bounds.south)
            writePosition(header.bounds.east, header.bounds.north)
            writeUInt8(header.center.zoom.toUInt())
            writePosition(header.center.longitude, header.center.latitude)
        }
        .also { bytes ->
            check(bytes.size == HEADER_BYTES) { "Encoded header length ${bytes.size} is invalid." }
        }
}

private fun BinaryWriter.writeMagic() {
    PMTILES_MAGIC_BYTES.forEach { byte -> writeUInt8(byte.toUByte().toUInt()) }
}

private fun BinaryWriter.writeSection(section: ArchiveSection) {
    writeULong64Le(section.offset)
    writeULong64Le(section.length)
}

private const val SUPPORTED_VERSION = 3
private val PMTILES_MAGIC_BYTES =
    byteArrayOf(
        0x50.toByte(),
        0x4d.toByte(),
        0x54.toByte(),
        0x69.toByte(),
        0x6c.toByte(),
        0x65.toByte(),
        0x73.toByte(),
    )
