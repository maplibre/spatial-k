package org.maplibre.spatialk.pmtiles.internal

import kotlin.coroutines.cancellation.CancellationException
import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.buildByteString
import org.maplibre.spatialk.pmtiles.ArchiveHeader
import org.maplibre.spatialk.pmtiles.ArchiveSection
import org.maplibre.spatialk.pmtiles.ArchiveWriteConfig
import org.maplibre.spatialk.pmtiles.ArchiveWriteOptions
import org.maplibre.spatialk.pmtiles.ArchiveWriteTile
import org.maplibre.spatialk.pmtiles.ByteSink
import org.maplibre.spatialk.pmtiles.LonLatBounds
import org.maplibre.spatialk.pmtiles.PmTilesErrorCode
import org.maplibre.spatialk.pmtiles.PmTilesException
import org.maplibre.spatialk.pmtiles.TileCenter

internal data class PreparedArchive(
    val header: ByteString,
    val rootDirectory: ByteString,
    val metadata: ByteString,
    val leafDirectories: List<ByteString>,
    val tilePayloads: List<ByteString>,
    val archiveSize: ULong,
)

internal suspend fun prepareArchive(
    tiles: List<ArchiveWriteTile>,
    config: ArchiveWriteConfig,
    options: ArchiveWriteOptions,
): PreparedArchive {
    val tileData = assembleTileData(tiles, options)
    val directories = buildDirectories(tileData.entries, options)
    val metadata = encodeMetadata(config, options)

    var offset = HEADER_BYTES.toULong()
    val rootSection =
        ArchiveSection(offset = offset, length = directories.compressedRoot.size.toULong())
    offset = checkedAdd(offset, rootSection.length, PmTilesErrorCode.InvalidSectionLayout)
    val metadataSection =
        ArchiveSection(offset = offset, length = metadata.compressedBytes.size.toULong())
    offset = checkedAdd(offset, metadataSection.length, PmTilesErrorCode.InvalidSectionLayout)
    val leafDirectoriesSection =
        ArchiveSection(offset = offset, length = directories.leafDirectoriesLength)
    offset =
        checkedAdd(offset, leafDirectoriesSection.length, PmTilesErrorCode.InvalidSectionLayout)
    val tileDataSection = ArchiveSection(offset = offset, length = tileData.length)
    val archiveSize =
        checkedAdd(offset, tileDataSection.length, PmTilesErrorCode.InvalidSectionLayout)
    validateArchiveSize(archiveSize, options)

    val archiveHeader =
        ArchiveHeader(
            specVersion = 3,
            rootDirectory = rootSection,
            metadata = metadataSection,
            leafDirectories = leafDirectoriesSection,
            tileData = tileDataSection,
            counts = tileData.counts,
            isClustered = true,
            internalCompression = options.internalCompression,
            tileCompression = options.tileCompression,
            tileType = config.tileType,
            minZoom = tiles.minOf { it.coord.z },
            maxZoom = tiles.maxOf { it.coord.z },
            bounds =
                LonLatBounds(
                    west = config.bounds.west,
                    south = config.bounds.south,
                    east = config.bounds.east,
                    north = config.bounds.north,
                ),
            center =
                TileCenter(
                    longitude = config.center.longitude,
                    latitude = config.center.latitude,
                    zoom = config.center.zoom,
                ),
        )

    return PreparedArchive(
        header = encodeHeader(archiveHeader, archiveSize),
        rootDirectory = directories.compressedRoot,
        metadata = metadata.compressedBytes,
        leafDirectories = directories.compressedLeaves,
        tilePayloads = tileData.payloads,
        archiveSize = archiveSize,
    )
}

internal suspend fun writePreparedArchive(archive: PreparedArchive, sink: ByteSink) {
    sink.writeOrWrap(archive.header)
    sink.writeOrWrap(archive.rootDirectory)
    sink.writeOrWrap(archive.metadata)
    archive.leafDirectories.forEach { bytes -> sink.writeOrWrap(bytes) }
    archive.tilePayloads.forEach { bytes -> sink.writeOrWrap(bytes) }
    sink.flushOrWrap()
    sink.closeOrWrap()
}

internal fun PreparedArchive.toByteString(maxArchiveBytes: ULong): ByteString {
    val size = allocationLength(archiveSize, maxArchiveBytes, "Archive")
    return buildByteString(size) {
        append(header.toByteArray())
        append(rootDirectory.toByteArray())
        append(metadata.toByteArray())
        leafDirectories.forEach { bytes -> append(bytes.toByteArray()) }
        tilePayloads.forEach { bytes -> append(bytes.toByteArray()) }
    }
}

private fun validateArchiveSize(archiveSize: ULong, options: ArchiveWriteOptions) {
    if (archiveSize > options.limits.maxArchiveBytes) {
        throw pmTilesException(
            PmTilesErrorCode.LimitExceeded,
            "Archive length $archiveSize exceeds limit ${options.limits.maxArchiveBytes}.",
        )
    }
}

private suspend fun ByteSink.writeOrWrap(bytes: ByteString) {
    try {
        write(bytes)
    } catch (error: PmTilesException) {
        throw error
    } catch (error: CancellationException) {
        throw error
    } catch (error: Exception) {
        throw sinkUnavailable("Byte sink write failed.", error)
    }
}

private suspend fun ByteSink.flushOrWrap() {
    try {
        flush()
    } catch (error: PmTilesException) {
        throw error
    } catch (error: CancellationException) {
        throw error
    } catch (error: Exception) {
        throw sinkUnavailable("Byte sink flush failed.", error)
    }
}

private suspend fun ByteSink.closeOrWrap() {
    try {
        close()
    } catch (error: PmTilesException) {
        throw error
    } catch (error: CancellationException) {
        throw error
    } catch (error: Exception) {
        throw sinkUnavailable("Byte sink close failed.", error)
    }
}

private fun sinkUnavailable(message: String, cause: Throwable): PmTilesException =
    pmTilesException(PmTilesErrorCode.SinkUnavailable, message, cause)
