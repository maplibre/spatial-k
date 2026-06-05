package org.maplibre.spatialk.pmtiles

import kotlin.test.Test
import kotlin.test.assertEquals

class ModelPredicatesTest {
    @Test
    fun compressionPreservesKnownAndUnknownCodes() {
        assertEquals(0u, CompressionCodes.Unknown.code)
        assertEquals(1u, CompressionCodes.None.code)
        assertEquals(2u, CompressionCodes.Gzip.code)
        assertEquals(3u, CompressionCodes.Brotli.code)
        assertEquals(4u, CompressionCodes.Zstd.code)
        assertEquals(CompressionCodes.Brotli, CompressionCodes.Brotli)
        assertEquals(99u, CompressionCode(99u).code)
    }

    @Test
    fun tileTypePreservesKnownAndUnknownCodes() {
        assertEquals(0u, TileTypeCodes.Unknown.code)
        assertEquals(1u, TileTypeCodes.Mvt.code)
        assertEquals(2u, TileTypeCodes.Png.code)
        assertEquals(3u, TileTypeCodes.Jpeg.code)
        assertEquals(4u, TileTypeCodes.Webp.code)
        assertEquals(5u, TileTypeCodes.Avif.code)
        assertEquals(6u, TileTypeCodes.Mlt.code)
        assertEquals(TileTypeCodes.Png, TileTypeCodes.Png)
        assertEquals(99u, TileTypeCode(99u).code)
    }
}
