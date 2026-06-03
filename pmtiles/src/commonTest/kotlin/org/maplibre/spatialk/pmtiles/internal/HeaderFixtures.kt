package org.maplibre.spatialk.pmtiles.internal

import org.maplibre.spatialk.pmtiles.ByteRange
import org.maplibre.spatialk.pmtiles.ByteRangeSource
import org.maplibre.spatialk.pmtiles.Compression
import org.maplibre.spatialk.pmtiles.TileType

internal data class TestHeaderFields(
    val rootOffset: ULong = HEADER_BYTES.toULong(),
    val rootLength: ULong = 1uL,
    val metadataOffset: ULong = 0uL,
    val metadataLength: ULong = 0uL,
    val leafDirectoriesOffset: ULong = 0uL,
    val leafDirectoriesLength: ULong = 0uL,
    val tileDataOffset: ULong = 0uL,
    val tileDataLength: ULong = 0uL,
    val addressedTiles: ULong = 0uL,
    val tileEntries: ULong = 0uL,
    val tileContents: ULong = 0uL,
    val clustered: UInt = 0u,
    val internalCompression: UInt = Compression.None.code,
    val tileCompression: UInt = Compression.None.code,
    val tileType: UInt = TileType.Unknown.code,
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
    rootBytes: ByteArray = byteArrayOf(0),
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
        if (range.length == 0) return ByteArray(0)
        val start = range.offset.toInt()
        val end = start + range.length
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
