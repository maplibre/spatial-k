package org.maplibre.spatialk.pmtiles.internal

import kotlin.test.Test
import kotlin.test.assertFailsWith
import org.maplibre.spatialk.pmtiles.Compression

class GzipJsTest {
    @Test
    fun gzipPlaceholderThrowsWhenInvoked() {
        assertFailsWith<NotImplementedError> {
            decodeCompression(Compression.Gzip, helloGzipBytes, testDecodeLimits())
        }
    }
}
