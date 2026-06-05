package org.maplibre.spatialk.pmtiles.internal

import org.maplibre.spatialk.pmtiles.ByteRange
import org.maplibre.spatialk.pmtiles.ByteRangeSource
import org.maplibre.spatialk.pmtiles.CompressionCodes
import org.maplibre.spatialk.pmtiles.TileTypeCodes

internal val MINIMAL_ROOT_DIRECTORY_BYTES: ByteArray =
    encodeDirectory(DirectoryEntry(tileId = 0, offset = 0uL, length = 1, runLength = 1))

internal data class TestHeaderFields(
    val rootOffset: ULong = HEADER_BYTES.toULong(),
    val rootLength: ULong = MINIMAL_ROOT_DIRECTORY_BYTES.size.toULong(),
    val metadataOffset: ULong = 0uL,
    val metadataLength: ULong = 0uL,
    val leafDirectoriesOffset: ULong = 0uL,
    val leafDirectoriesLength: ULong = 0uL,
    val tileDataOffset: ULong =
        HEADER_BYTES.toULong() + MINIMAL_ROOT_DIRECTORY_BYTES.size.toULong(),
    val tileDataLength: ULong = 1uL,
    val addressedTiles: ULong = 0uL,
    val tileEntries: ULong = 0uL,
    val tileContents: ULong = 0uL,
    val clustered: UInt = 0u,
    val internalCompression: UInt = CompressionCodes.None.code,
    val tileCompression: UInt = CompressionCodes.None.code,
    val tileType: UInt = TileTypeCodes.Unknown.code,
    val minZoom: UInt = 0u,
    val maxZoom: UInt = 0u,
    val minLongitude: Double = 0.0,
    val minLatitude: Double = 0.0,
    val maxLongitude: Double = 0.0,
    val maxLatitude: Double = 0.0,
    val centerZoom: UInt = 0u,
    val centerLongitude: Double = 0.0,
    val centerLatitude: Double = 0.0,
)

internal fun buildHeader(fields: TestHeaderFields = TestHeaderFields()): ByteArray =
    ByteArray(HEADER_BYTES).also { bytes ->
        byteArrayOf(0x50, 0x4d, 0x54, 0x69, 0x6c, 0x65, 0x73).copyInto(bytes)
        bytes[7] = 3
        bytes.writeU64(8, fields.rootOffset)
        bytes.writeU64(16, fields.rootLength)
        bytes.writeU64(24, fields.metadataOffset)
        bytes.writeU64(32, fields.metadataLength)
        bytes.writeU64(40, fields.leafDirectoriesOffset)
        bytes.writeU64(48, fields.leafDirectoriesLength)
        bytes.writeU64(56, fields.tileDataOffset)
        bytes.writeU64(64, fields.tileDataLength)
        bytes.writeU64(72, fields.addressedTiles)
        bytes.writeU64(80, fields.tileEntries)
        bytes.writeU64(88, fields.tileContents)
        bytes[96] = fields.clustered.toByte()
        bytes[97] = fields.internalCompression.toByte()
        bytes[98] = fields.tileCompression.toByte()
        bytes[99] = fields.tileType.toByte()
        bytes[100] = fields.minZoom.toByte()
        bytes[101] = fields.maxZoom.toByte()
        bytes.writePosition(102, fields.minLongitude, fields.minLatitude)
        bytes.writePosition(110, fields.maxLongitude, fields.maxLatitude)
        bytes[118] = fields.centerZoom.toByte()
        bytes.writePosition(119, fields.centerLongitude, fields.centerLatitude)
    }

internal fun buildArchive(
    fields: TestHeaderFields = TestHeaderFields(),
    archiveSize: Int = fields.minimumArchiveSize(),
    rootBytes: ByteArray = MINIMAL_ROOT_DIRECTORY_BYTES,
): ByteArray {
    val bytes = ByteArray(archiveSize)
    buildHeader(fields).copyInto(bytes)
    if (
        fields.rootOffset <= Int.MAX_VALUE.toULong() &&
            fields.rootOffset + rootBytes.size.toULong() <= archiveSize.toULong()
    ) {
        rootBytes.copyInto(bytes, destinationOffset = fields.rootOffset.toInt())
    }
    return bytes
}

internal fun buildArchiveWithSections(
    fields: TestHeaderFields,
    rootBytes: ByteArray,
    metadataBytes: ByteArray = ByteArray(0),
    leafBytes: ByteArray = ByteArray(0),
    tileBytes: ByteArray = ByteArray(0),
    minimumArchiveSize: ULong = 400uL,
): ByteArray {
    var archiveSize = minimumArchiveSize
    archiveSize = maxOf(archiveSize, fields.rootOffset + fields.rootLength)
    archiveSize = maxOf(archiveSize, fields.metadataOffset + fields.metadataLength)
    archiveSize = maxOf(archiveSize, fields.leafDirectoriesOffset + fields.leafDirectoriesLength)
    archiveSize = maxOf(archiveSize, fields.tileDataOffset + fields.tileDataLength)

    return buildArchive(fields, archiveSize = archiveSize.toInt(), rootBytes = rootBytes).also {
        bytes ->
        if (metadataBytes.isNotEmpty()) {
            metadataBytes.copyInto(bytes, destinationOffset = fields.metadataOffset.toInt())
        }
        if (leafBytes.isNotEmpty()) {
            leafBytes.copyInto(bytes, destinationOffset = fields.leafDirectoriesOffset.toInt())
        }
        if (tileBytes.isNotEmpty()) {
            tileBytes.copyInto(bytes, destinationOffset = fields.tileDataOffset.toInt())
        }
    }
}

internal fun buildSingleTileArchive(
    tileBytes: ByteArray,
    tileCompression: UInt = CompressionCodes.None.code,
    tileType: UInt = TileTypeCodes.Unknown.code,
): ByteArray {
    val rootBytes =
        encodeDirectory(
            DirectoryEntry(tileId = 0, offset = 0uL, length = tileBytes.size, runLength = 1)
        )
    val fields =
        TestHeaderFields(
            rootLength = rootBytes.size.toULong(),
            tileDataOffset = HEADER_BYTES.toULong() + rootBytes.size.toULong(),
            tileDataLength = tileBytes.size.toULong(),
            tileCompression = tileCompression,
            tileType = tileType,
        )
    return buildArchiveWithSections(fields, rootBytes = rootBytes, tileBytes = tileBytes)
}

internal fun encodeDirectory(vararg entries: DirectoryEntry): ByteArray {
    require(entries.isNotEmpty()) { "Directory entries are required." }
    val bytes = mutableListOf<Byte>()
    bytes.writeVarint(entries.size.toULong())

    var lastTileId = 0uL
    entries.forEach { entry ->
        val tileId = entry.tileId.toULong()
        bytes.writeVarint(tileId - lastTileId)
        lastTileId = tileId
    }
    entries.forEach { entry -> bytes.writeVarint(entry.runLength.toULong()) }
    entries.forEach { entry -> bytes.writeVarint(entry.length.toULong()) }

    var nextOffset = 0uL
    entries.forEachIndexed { index, entry ->
        if (index > 0 && entry.offset == nextOffset) {
            bytes.writeVarint(0uL)
        } else {
            bytes.writeVarint(entry.offset + 1uL)
        }
        nextOffset = entry.offset + entry.length.toULong()
    }

    return bytes.toByteArray()
}

internal class TestByteRangeSource(
    private val bytes: ByteArray,
    private val sizeError: Throwable? = null,
    private val readError: Throwable? = null,
    private val shortRead: Boolean = false,
) : ByteRangeSource {
    val reads = mutableListOf<ByteRange>()

    override suspend fun size(): ULong {
        sizeError?.let { throw it }
        return bytes.size.toULong()
    }

    override suspend fun read(range: ByteRange): ByteArray {
        readError?.let { throw it }
        reads += range
        if (range.length == 0uL) return ByteArray(0)
        val start = range.offset.toInt()
        val end = start + range.length.toInt()
        val result = bytes.copyOfRange(start, end)
        return if (shortRead) result.copyOf(result.size - 1) else result
    }
}

private fun TestHeaderFields.minimumArchiveSize(): Int {
    val rootEnd = rootOffset + rootLength
    val metadataEnd = metadataOffset + metadataLength
    val leafEnd = leafDirectoriesOffset + leafDirectoriesLength
    val tileEnd = tileDataOffset + tileDataLength
    var maxEnd = HEADER_BYTES.toULong()
    if (rootEnd > maxEnd) maxEnd = rootEnd
    if (metadataEnd > maxEnd) maxEnd = metadataEnd
    if (leafEnd > maxEnd) maxEnd = leafEnd
    if (tileEnd > maxEnd) maxEnd = tileEnd
    return maxEnd.toInt()
}

private fun ByteArray.writePosition(offset: Int, longitude: Double, latitude: Double) {
    writeI32(offset, longitude.toScaledPosition())
    writeI32(offset + 4, latitude.toScaledPosition())
}

private fun Double.toScaledPosition(): Int = (this * 10_000_000).toInt()

private fun ByteArray.writeU64(offset: Int, value: ULong) {
    var remaining = value
    repeat(ULong.SIZE_BYTES) { index ->
        this[offset + index] = (remaining and 0xffu).toByte()
        remaining = remaining shr Byte.SIZE_BITS
    }
}

private fun ByteArray.writeI32(offset: Int, value: Int) {
    repeat(Int.SIZE_BYTES) { index ->
        this[offset + index] = (value ushr (index * Byte.SIZE_BITS)).toByte()
    }
}

private fun MutableList<Byte>.writeVarint(value: ULong) {
    var remaining = value
    while (remaining >= 0x80uL) {
        add(((remaining and 0x7fuL) or 0x80uL).toByte())
        remaining = remaining shr 7
    }
    add(remaining.toByte())
}
