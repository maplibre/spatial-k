package org.maplibre.spatialk.turf.other

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position

class RTreeTest {
    @Test
    fun testLoad() {
        val features = listOf(Feature(Point(0.0, 0.0), null), Feature(Point(1.0, 1.0), null))
        val tree = RTree<Feature<Point, Nothing?>>()
        tree.insert(features)
        assertEquals(2, tree.size)
    }

    @Test
    fun testInsert() {
        val tree = RTree<Feature<Point, Nothing?>>()
        tree.insert(Feature(Point(0.0, 0.0), null))
        tree.insert(Feature(Point(1.0, 1.0), null))
        assertEquals(2, tree.size)
    }

    @Test
    fun testAll() {
        val features = listOf(Feature(Point(0.0, 0.0), null), Feature(Point(1.0, 1.0), null))
        val tree = RTree<Feature<Point, Nothing?>>()
        tree.insert(features)
        assertEquals(features.size, tree.size)
    }

    @Test
    fun testSearch() {
        val features =
            listOf(
                Feature(Point(0.0, 0.0), null),
                Feature(Point(1.0, 1.0), null),
                Feature(Point(10.0, 10.0), null),
            )
        val tree = RTree<Feature<Point, Nothing?>>()
        tree.insert(features)

        val result = tree.search(BoundingBox(0.0, 0.0, 1.0, 1.0))
        assertEquals(2, result.size)

        val result2 = tree.search(BoundingBox(5.0, 5.0, 15.0, 15.0))
        assertEquals(1, result2.size)
    }

    @Test
    fun testCollides() {
        val features = listOf(Feature(Point(0.0, 0.0), null), Feature(Point(1.0, 1.0), null))
        val tree = RTree<Feature<Point, Nothing?>>()
        tree.insert(features)

        assertTrue(tree.collides(BoundingBox(0.5, 0.5, 1.5, 1.5)))
        assertFalse(tree.collides(BoundingBox(2.0, 2.0, 3.0, 3.0)))
    }

    @Test
    fun testRemove() {
        val features = listOf(Feature(Point(0.0, 0.0), null), Feature(Point(1.0, 1.0), null))
        val tree = RTree<Feature<Point, Nothing?>>()
        tree.insert(features)

        tree.remove(features[0])
        assertEquals(1, tree.size)
    }

    @Test
    fun testClear() {
        val features = listOf(Feature(Point(0.0, 0.0), null), Feature(Point(1.0, 1.0), null))
        val tree = RTree<Feature<Point, Nothing?>>()
        tree.insert(features)

        tree.clear()
        assertEquals(0, tree.size)
    }

    private fun createPointFeature(x: Double, y: Double): Feature<Point, Nothing?> {
        val position = Position(x, y)
        return Feature(
            geometry = Point(position),
            properties = null,
            bbox = BoundingBox(x, y, x, y),
        )
    }

    // Generate test data
    private fun someData(n: Int): List<Feature<Point, Nothing?>> {
        return (0 until n).map { i -> createPointFeature(i.toDouble(), i.toDouble()) }
    }

    // Test data matching the JavaScript version
    private val data =
        listOf(
                listOf(0.0, 0.0, 0.0, 0.0),
                listOf(10.0, 10.0, 10.0, 10.0),
                listOf(20.0, 20.0, 20.0, 20.0),
                listOf(25.0, 0.0, 25.0, 0.0),
                listOf(35.0, 10.0, 35.0, 10.0),
                listOf(45.0, 20.0, 45.0, 20.0),
                listOf(0.0, 25.0, 0.0, 25.0),
                listOf(10.0, 35.0, 10.0, 35.0),
                listOf(20.0, 45.0, 20.0, 45.0),
                listOf(25.0, 25.0, 25.0, 25.0),
                listOf(35.0, 35.0, 35.0, 35.0),
                listOf(45.0, 45.0, 45.0, 45.0),
                listOf(50.0, 0.0, 50.0, 0.0),
                listOf(60.0, 10.0, 60.0, 10.0),
                listOf(70.0, 20.0, 70.0, 20.0),
                listOf(75.0, 0.0, 75.0, 0.0),
                listOf(85.0, 10.0, 85.0, 10.0),
                listOf(95.0, 20.0, 95.0, 20.0),
                listOf(50.0, 25.0, 50.0, 25.0),
                listOf(60.0, 35.0, 60.0, 35.0),
                listOf(70.0, 45.0, 70.0, 45.0),
                listOf(75.0, 25.0, 75.0, 25.0),
                listOf(85.0, 35.0, 85.0, 35.0),
                listOf(95.0, 45.0, 95.0, 45.0),
                listOf(0.0, 50.0, 0.0, 50.0),
                listOf(10.0, 60.0, 10.0, 60.0),
                listOf(20.0, 70.0, 20.0, 70.0),
                listOf(25.0, 50.0, 25.0, 50.0),
                listOf(35.0, 60.0, 35.0, 60.0),
                listOf(45.0, 70.0, 45.0, 70.0),
                listOf(0.0, 75.0, 0.0, 75.0),
                listOf(10.0, 85.0, 10.0, 85.0),
                listOf(20.0, 95.0, 20.0, 95.0),
                listOf(25.0, 75.0, 25.0, 75.0),
                listOf(35.0, 85.0, 35.0, 85.0),
                listOf(45.0, 95.0, 45.0, 95.0),
                listOf(50.0, 50.0, 50.0, 50.0),
                listOf(60.0, 60.0, 60.0, 60.0),
                listOf(70.0, 70.0, 70.0, 70.0),
                listOf(75.0, 50.0, 75.0, 50.0),
                listOf(85.0, 60.0, 85.0, 60.0),
                listOf(95.0, 70.0, 95.0, 70.0),
                listOf(50.0, 75.0, 50.0, 75.0),
                listOf(60.0, 85.0, 60.0, 85.0),
                listOf(70.0, 95.0, 70.0, 95.0),
                listOf(75.0, 75.0, 75.0, 75.0),
                listOf(85.0, 85.0, 85.0, 85.0),
                listOf(95.0, 95.0, 95.0, 95.0),
            )
            .map { createPointFeature(it[0], it[1]) }

    // Helper to sort and compare feature lists
    private fun assertFeatureListsEqual(
        expected: List<Feature<Point, Nothing?>>,
        actual: List<Feature<Point, Nothing?>>,
    ) {
        val sortedExpected =
            expected.sortedWith(
                compareBy({ it.bbox?.southwest?.longitude }, { it.bbox?.southwest?.latitude })
            )
        val sortedActual =
            actual.sortedWith(
                compareBy({ it.bbox?.southwest?.longitude }, { it.bbox?.southwest?.latitude })
            )

        assertEquals(sortedExpected.size, sortedActual.size, "Lists have different sizes")

        sortedExpected.zip(sortedActual).forEach { (exp, act) ->
            assertEquals(exp.bbox!!.southwest.longitude, act.bbox!!.southwest.longitude, 0.0001)
            assertEquals(exp.bbox!!.southwest.latitude, act.bbox!!.southwest.latitude, 0.0001)
            assertEquals(exp.bbox!!.northeast.longitude, act.bbox!!.northeast.longitude, 0.0001)
            assertEquals(exp.bbox!!.northeast.latitude, act.bbox!!.northeast.latitude, 0.0001)
        }
    }

    @Test
    fun `constructor uses 16 max entries by default`() {
        val tree = RTree<Feature<Point, Nothing?>>()
        tree.insert(someData(16))
        assertEquals(1, tree.data.height)

        val tree2 = RTree<Feature<Point, Nothing?>>()
        tree2.insert(someData(17))
        assertEquals(2, tree2.data.height)
    }

    @Test
    fun `insert bulk-loads the given data and forms a proper search tree`() {
        val tree = RTree(data)
        assertFeatureListsEqual(data, tree.toList())
    }

    @Test
    fun `insert uses standard insertion when given a low number of items`() {
        val tree = RTree(data)
        tree.insert(data.slice(0..2))

        val tree2 = RTree(data)
        tree2.insert(data[0])
        tree2.insert(data[1])
        tree2.insert(data[2])

        assertFeatureListsEqual(tree.toList(), tree2.toList())
    }

    @Test
    fun `insert does nothing if loading empty data`() {
        val tree = RTree<Feature<Point, Nothing?>>()
        tree.insert(emptyList())

        assertEquals(0, tree.toList().size)
    }

    @Test
    fun `insert properly splits tree root when merging trees of the same height`() {
        val tree = RTree<Feature<Point, Nothing?>>()
        tree.insert(data)
        tree.insert(data)

        assertFeatureListsEqual(data + data, tree.toList())
    }

    @Test
    fun `insert properly merges data of smaller or bigger tree heights`() {
        val smaller = someData(10)

        val tree1 = RTree<Feature<Point, Nothing?>>()
        tree1.insert(data)
        tree1.insert(smaller)

        val tree2 = RTree<Feature<Point, Nothing?>>()
        tree2.insert(smaller)
        tree2.insert(data)

        assertFeatureListsEqual(data + smaller, tree1.toList())
        assertFeatureListsEqual(data + smaller, tree2.toList())
    }

    @Test
    fun `search finds matching points in the tree given a bbox`() {
        val tree = RTree(data)
        val result = tree.search(BoundingBox(40.0, 20.0, 80.0, 70.0))

        val expected =
            listOf(
                    listOf(70.0, 20.0, 70.0, 20.0),
                    listOf(75.0, 25.0, 75.0, 25.0),
                    listOf(45.0, 45.0, 45.0, 45.0),
                    listOf(50.0, 50.0, 50.0, 50.0),
                    listOf(60.0, 60.0, 60.0, 60.0),
                    listOf(70.0, 70.0, 70.0, 70.0),
                    listOf(45.0, 20.0, 45.0, 20.0),
                    listOf(45.0, 70.0, 45.0, 70.0),
                    listOf(75.0, 50.0, 75.0, 50.0),
                    listOf(50.0, 25.0, 50.0, 25.0),
                    listOf(60.0, 35.0, 60.0, 35.0),
                    listOf(70.0, 45.0, 70.0, 45.0),
                )
                .map { createPointFeature(it[0], it[1]) }

        assertFeatureListsEqual(expected, result)
    }

    @Test
    fun `collides returns true when search finds matching points`() {
        val tree = RTree(data)
        val result = tree.collides(BoundingBox(40.0, 20.0, 80.0, 70.0))

        assertTrue(result)
    }

    @Test
    fun `search returns an empty list if nothing found`() {
        val tree = RTree(data)
        val result = tree.search(BoundingBox(200.0, 200.0, 210.0, 210.0))

        assertEquals(0, result.size)
    }

    @Test
    fun `collides returns false if nothing found`() {
        val tree = RTree(data)
        val result = tree.collides(BoundingBox(200.0, 200.0, 210.0, 210.0))

        assertFalse(result)
    }

    @Test
    fun `all returns all points in the tree`() {
        val tree = RTree(data)
        val result = tree.toList()

        assertFeatureListsEqual(data, result)
        assertFeatureListsEqual(data, tree.search(BoundingBox(0.0, 0.0, 100.0, 100.0)))
    }

    @Test
    fun `insert single item adds an item to an existing tree correctly`() {
        val items =
            listOf(
                    listOf(0.0, 0.0, 0.0, 0.0),
                    listOf(1.0, 1.0, 1.0, 1.0),
                    listOf(2.0, 2.0, 2.0, 2.0),
                    listOf(3.0, 3.0, 3.0, 3.0),
                    listOf(1.0, 1.0, 2.0, 2.0),
                )
                .map { createPointFeature(it[0], it[1]) }

        val tree = RTree<Feature<Point, Nothing?>>()
        tree.insert(items.slice(0..2))

        tree.insert(items[3])
        assertFeatureListsEqual(items.slice(0..3), tree.toList())

        tree.insert(items[4])
        assertFeatureListsEqual(items, tree.toList())
    }

    @Test
    fun `insert forms a valid tree if items are inserted one by one`() {
        val tree = RTree<Feature<Point, Nothing?>>()

        for (i in data.indices) {
            tree.insert(data[i])
        }

        val tree2 = RTree(data)

        assertFeatureListsEqual(data, tree.toList())
        assertFeatureListsEqual(tree2.toList(), tree.toList())
    }

    @Test
    fun `remove removes items correctly`() {
        val tree = RTree(data)

        val len = data.size

        tree.remove(data[0])
        tree.remove(data[1])
        tree.remove(data[2])

        tree.remove(data[len - 1])
        tree.remove(data[len - 2])
        tree.remove(data[len - 3])

        assertFeatureListsEqual(data.slice(3 until len - 3), tree.toList())
    }

    @Test
    fun `remove does nothing if nothing found`() {
        val tree = RTree(data)
        val allBefore = tree.toList()

        tree.remove(createPointFeature(13.0, 13.0))

        assertFeatureListsEqual(allBefore, tree.toList())
    }

    @Test
    fun `remove brings the tree to a clear state when removing everything one by one`() {
        val tree = RTree(data)

        for (i in data.indices) {
            tree.remove(data[i])
        }

        assertEquals(0, tree.size)
    }

    @Test
    fun `clear should clear all the data in the tree`() {
        val tree = RTree(data)
        tree.clear()

        assertEquals(0, tree.size)
    }
}
