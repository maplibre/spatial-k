@file:Suppress("UnusedVariable", "unused")

package org.maplibre.spatialk.geohash

import kotlin.test.Test
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.maplibre.spatialk.geojson.Position

class KotlinDocsTest {
    @Test
    fun encode() {
        // --8<-- [start:encode]
        val cell =
            Geohash.of(
                Position(longitude = 10.40744, latitude = 57.64911),
                length = 11,
            )
        println(cell.text)
        // u4pruydqqvj
        // --8<-- [end:encode]
    }

    @Test
    fun parse() {
        // --8<-- [start:parse]
        val cell = Geohash.parse("ezs42")
        val center = cell.center
        val boundingBox = cell.boundingBox
        val parent = cell.parent
        val children = cell.children
        // --8<-- [end:parse]
    }

    @Test
    fun neighbors() {
        // --8<-- [start:neighbors]
        val cell = Geohash.parse("ezs42")
        val east = cell.neighbors.east
        val existingNeighbors = cell.neighbors.toList()
        val twoCellsWest = cell.offsetBy(east = -2, north = 0)
        // --8<-- [end:neighbors]
    }

    @Test
    fun containment() {
        // --8<-- [start:containment]
        val cell = Geohash.parse("ezs42")
        val containsCenter = cell.center in cell
        val containsChild = Geohash.parse("ezs420") in cell
        // --8<-- [end:containment]
    }

    @Test
    fun osmShortlink() {
        // --8<-- [start:osmShortlink]
        val shortlink = OsmShortlink.parse("https://osm.org/go/0EEQjE--")
        println(shortlink.zoom)
        println(shortlink.center)

        val encoded =
            OsmShortlink.of(
                Position(longitude = 0.054, latitude = 51.510),
                zoom = 9,
            )
        // --8<-- [end:osmShortlink]
    }

    @Test
    fun serialize() {
        // --8<-- [start:serialize]
        val cell = Geohash.parse("ezs42")
        val json = Json.encodeToString(cell)
        val parsed = Json.decodeFromString<Geohash>(json)
        // --8<-- [end:serialize]
    }
}
