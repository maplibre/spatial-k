package org.maplibre.spatialk.pmtiles

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ModelPredicatesTest {
    @Test
    fun compressionPredicatesMatchKnownCodes() {
        assertEquals(KnownCompression.Unknown, Compression(KnownCompression.Unknown).known)
        assertEquals(KnownCompression.None, Compression(KnownCompression.None).known)
        assertEquals(KnownCompression.Gzip, Compression(KnownCompression.Gzip).known)
        assertEquals(KnownCompression.Brotli, Compression(KnownCompression.Brotli).known)
        assertEquals(KnownCompression.Zstd, Compression(KnownCompression.Zstd).known)
        assertEquals(Compression(KnownCompression.Brotli), Compression(KnownCompression.Brotli))
        assertNull(Compression(99u).known)
        assertEquals(KnownCompression.Gzip, Compression(99u).knownOr(KnownCompression.Gzip))
        assertEquals(
            KnownCompression.Unknown,
            Compression(KnownCompression.Unknown).knownOr(KnownCompression.Gzip),
        )

        assertTrue(Compression(KnownCompression.Unknown).isKnown)
        assertFalse(Compression(99u).isKnown)
        assertTrue(Compression(KnownCompression.Unknown).isUnknown)
        assertTrue(Compression(KnownCompression.None).isNone)
        assertTrue(Compression(KnownCompression.Gzip).isGzip)
        assertTrue(Compression(KnownCompression.Brotli).isBrotli)
        assertTrue(Compression(KnownCompression.Zstd).isZstd)

        assertFalse(Compression(99u).isUnknown)
        assertFalse(Compression(KnownCompression.None).isGzip)
    }

    @Test
    fun tileTypePredicatesMatchKnownCodes() {
        assertEquals(KnownTileType.Unknown, TileType(KnownTileType.Unknown).known)
        assertEquals(KnownTileType.Mvt, TileType(KnownTileType.Mvt).known)
        assertEquals(KnownTileType.Png, TileType(KnownTileType.Png).known)
        assertEquals(KnownTileType.Jpeg, TileType(KnownTileType.Jpeg).known)
        assertEquals(KnownTileType.Webp, TileType(KnownTileType.Webp).known)
        assertEquals(KnownTileType.Avif, TileType(KnownTileType.Avif).known)
        assertEquals(KnownTileType.Mlt, TileType(KnownTileType.Mlt).known)
        assertEquals(TileType(KnownTileType.Png), TileType(KnownTileType.Png))
        assertNull(TileType(99u).known)
        assertEquals(KnownTileType.Mvt, TileType(99u).knownOr(KnownTileType.Mvt))
        assertEquals(
            KnownTileType.Unknown,
            TileType(KnownTileType.Unknown).knownOr(KnownTileType.Mvt),
        )

        assertTrue(TileType(KnownTileType.Unknown).isKnown)
        assertFalse(TileType(99u).isKnown)
        assertTrue(TileType(KnownTileType.Unknown).isUnknown)
        assertTrue(TileType(KnownTileType.Mvt).isMvt)
        assertTrue(TileType(KnownTileType.Png).isPng)
        assertTrue(TileType(KnownTileType.Jpeg).isJpeg)
        assertTrue(TileType(KnownTileType.Webp).isWebp)
        assertTrue(TileType(KnownTileType.Avif).isAvif)
        assertTrue(TileType(KnownTileType.Mlt).isMlt)

        assertFalse(TileType(99u).isUnknown)
        assertFalse(TileType(KnownTileType.Png).isMvt)
    }

    @Test
    fun tilesetKindPredicatesMatchKnownValues() {
        assertEquals(KnownTilesetKind.Overlay, TilesetKind(KnownTilesetKind.Overlay).known)
        assertEquals(KnownTilesetKind.BaseLayer, TilesetKind(KnownTilesetKind.BaseLayer).known)
        assertEquals(
            TilesetKind(KnownTilesetKind.BaseLayer),
            TilesetKind(KnownTilesetKind.BaseLayer),
        )
        assertNull(TilesetKind("custom").known)
        assertEquals(
            KnownTilesetKind.Overlay,
            TilesetKind("custom").knownOr(KnownTilesetKind.Overlay),
        )
        assertEquals(
            KnownTilesetKind.BaseLayer,
            TilesetKind(KnownTilesetKind.BaseLayer).knownOr(KnownTilesetKind.Overlay),
        )

        assertTrue(TilesetKind(KnownTilesetKind.Overlay).isKnown)
        assertFalse(TilesetKind("custom").isKnown)
        assertTrue(TilesetKind(KnownTilesetKind.Overlay).isOverlay)
        assertTrue(TilesetKind(KnownTilesetKind.BaseLayer).isBaseLayer)

        assertFalse(TilesetKind("custom").isOverlay)
        assertFalse(TilesetKind(KnownTilesetKind.Overlay).isBaseLayer)
    }
}
