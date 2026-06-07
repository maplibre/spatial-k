@file:OptIn(ExperimentalObjCName::class, ExperimentalObjCRefinement::class)

package org.maplibre.spatialk.pmtiles

import kotlin.experimental.ExperimentalObjCName
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.jvm.JvmInline
import kotlin.native.HiddenFromObjC
import kotlin.native.ObjCName
import kotlin.native.ShouldRefineInSwift
import kotlinx.io.bytestring.ByteString
import org.maplibre.spatialk.pmtiles.internal.pmTilesException

/**
 * How [ArchiveWriteTile.payload] bytes should be interpreted by the writer.
 *
 * @property code Raw tile payload mode code.
 */
@JvmInline public value class TilePayloadMode(public val code: UInt)

/** Tile payload mode constants. */
public object TilePayloadModes {
    /** Payload bytes are already in the archive's stored tile-compression format. */
    @ShouldRefineInSwift
    @ObjCName(swiftName = "stored")
    public val Stored: TilePayloadMode = TilePayloadMode(0u)

    /** Payload bytes are uncompressed and should be compressed by the writer. */
    @ShouldRefineInSwift
    @ObjCName(swiftName = "uncompressed")
    public val Uncompressed: TilePayloadMode = TilePayloadMode(1u)
}

/**
 * Tile payload input for PMTiles archive writing.
 *
 * @property coord Web tile coordinate.
 * @property payload Immutable tile payload bytes.
 * @property payloadMode Whether [payload] is already stored bytes or uncompressed bytes.
 */
public data class ArchiveWriteTile
public constructor(
    public val coord: TileCoord,
    @ShouldRefineInSwift public val payload: ByteString,
    @ShouldRefineInSwift public val payloadMode: TilePayloadMode,
) {
    /** Factory helpers for explicit tile payload modes. */
    public companion object {
        /** Creates tile input whose payload bytes are already in stored archive form. */
        public fun stored(coord: TileCoord, payload: ByteString): ArchiveWriteTile =
            ArchiveWriteTile(
                coord = coord,
                payload = payload,
                payloadMode = TilePayloadModes.Stored,
            )

        /** Creates tile input whose payload bytes should be compressed by the writer. */
        public fun uncompressed(coord: TileCoord, payload: ByteString): ArchiveWriteTile =
            ArchiveWriteTile(
                coord = coord,
                payload = payload,
                payloadMode = TilePayloadModes.Uncompressed,
            )
    }
}

/**
 * Geographic longitude/latitude bounds used when writing an archive header.
 *
 * @property west Western longitude.
 * @property south Southern latitude.
 * @property east Eastern longitude.
 * @property north Northern latitude.
 */
public data class ArchiveWriteBounds
@Throws(PmTilesException::class)
public constructor(
    public val west: Double,
    public val south: Double,
    public val east: Double,
    public val north: Double,
) {
    /** Creates world bounds. */
    @Throws(PmTilesException::class) public constructor() : this(-180.0, -90.0, 180.0, 90.0)

    init {
        if (
            !west.isLongitude() ||
                !east.isLongitude() ||
                !south.isLatitude() ||
                !north.isLatitude() ||
                east < west ||
                north < south
        ) {
            throw invalidHeaderConfig(
                "Archive write bounds must be sane longitude/latitude bounds."
            )
        }
    }
}

/**
 * Geographic center point used when writing an archive header.
 *
 * @property longitude Center longitude.
 * @property latitude Center latitude.
 * @property zoom Center zoom.
 */
public data class ArchiveWriteCenter
@Throws(PmTilesException::class)
public constructor(
    public val longitude: Double,
    public val latitude: Double,
    public val zoom: Int,
) {
    /** Creates a center at longitude 0, latitude 0, zoom 0. */
    @Throws(PmTilesException::class) public constructor() : this(0.0, 0.0, 0)

    init {
        if (!longitude.isLongitude() || !latitude.isLatitude() || zoom !in 0..MAX_HEADER_ZOOM) {
            throw invalidHeaderConfig(
                "Archive write center must use sane longitude/latitude values and zoom 0..31."
            )
        }
    }
}

/**
 * Header and metadata configuration for PMTiles archive writing.
 *
 * @property tileType Tile payload type code.
 * @property bounds Geographic archive bounds.
 * @property center Geographic archive center.
 * @property metadataJson Raw metadata JSON object string.
 */
public class ArchiveWriteConfig
private constructor(
    @ShouldRefineInSwift public val tileType: TileTypeCode,
    public val bounds: ArchiveWriteBounds,
    public val center: ArchiveWriteCenter,
    public val metadataJson: String,
) {
    /** Creates write configuration with documented defaults. */
    public constructor() :
        this(
            tileType = TileTypeCodes.Unknown,
            bounds = ArchiveWriteBounds(),
            center = ArchiveWriteCenter(),
            metadataJson = "{}",
        )

    /** Returns a mutable builder initialized from this configuration. */
    @ShouldRefineInSwift public fun toBuilder(): Builder = Builder(this)

    /** Mutable Kotlin builder for [ArchiveWriteConfig]. */
    public class Builder public constructor() {
        private val defaults: ArchiveWriteConfig = ArchiveWriteConfig()

        /** Tile payload type code. */
        public var tileType: TileTypeCode = defaults.tileType

        /** Geographic archive bounds. */
        public var bounds: ArchiveWriteBounds = defaults.bounds

        /** Geographic archive center. */
        public var center: ArchiveWriteCenter = defaults.center

        /** Raw metadata JSON object string. */
        public var metadataJson: String = defaults.metadataJson

        internal constructor(config: ArchiveWriteConfig) : this() {
            tileType = config.tileType
            bounds = config.bounds
            center = config.center
            metadataJson = config.metadataJson
        }

        /** Builds immutable write configuration from this builder. */
        public fun build(): ArchiveWriteConfig =
            ArchiveWriteConfig(
                tileType = tileType,
                bounds = bounds,
                center = center,
                metadataJson = metadataJson,
            )
    }

    /** Factory for Kotlin DSL construction. */
    public companion object {
        /** Builds [ArchiveWriteConfig] with a Kotlin DSL. */
        @HiddenFromObjC
        public fun build(configure: Builder.() -> Unit): ArchiveWriteConfig =
            Builder().apply(configure).build()
    }
}

private fun Double.isLongitude(): Boolean = isFinite() && this in -180.0..180.0

private fun Double.isLatitude(): Boolean = isFinite() && this in -90.0..90.0

private fun invalidHeaderConfig(message: String): PmTilesException =
    pmTilesException(PmTilesErrorCodes.InvalidHeader, message)

private const val MAX_HEADER_ZOOM = 31
