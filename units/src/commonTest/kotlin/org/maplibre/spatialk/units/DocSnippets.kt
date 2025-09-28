@file:Suppress("UnusedVariable", "unused")

package org.maplibre.spatialk.units

import kotlin.test.Test

// These snippets are primarily intended to be included in docs/turf.md. Though they exist as
// part of the test suite, they are not intended to be comprehensive tests.

class DocSnippets {
    @Test
    fun example() {
        // --8<-- [start:example]
        val distance: Length = 123.miles
        println(distance.inKilometers)

        val area: Area = 45.acres
        println(area.inSquareMeters)
        // --8<-- [end:example]
    }
}
