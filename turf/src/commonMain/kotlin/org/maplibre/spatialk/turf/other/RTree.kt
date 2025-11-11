package org.maplibre.spatialk.turf.other

import kotlin.math.E
import kotlin.math.ceil
import kotlin.math.exp
import kotlin.math.log
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.GeoJsonObject
import org.maplibre.spatialk.geojson.GeometryCollection
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.MultiLineString
import org.maplibre.spatialk.geojson.MultiPoint
import org.maplibre.spatialk.geojson.MultiPolygon
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Polygon
import org.maplibre.spatialk.turf.coordinatemutation.flattenCoordinates
import org.maplibre.spatialk.turf.measurement.computeBbox

/**
 * A high-performance R-tree-based spatial index for 2D points and rectangles.
 *
 * RTree is an implementation of an R-tree, a tree data structure used for spatial access methods,
 * i.e., for indexing multi-dimensional information such as geographical coordinates, rectangles, or
 * polygons. It's a popular choice for spatial indexing due to its efficiency in handling dynamic
 * datasets (i.e., data that changes over time) and its speed in performing spatial queries.
 *
 * This implementation is a port of the JavaScript `RBush` library and is optimized for indexing and
 * querying large datasets of geographical features. It supports operations like insertion, removal,
 * and spatial searches (e.g., finding all items within a given bounding box).
 *
 * The performance of the R-tree is heavily influenced by the `maxEntries` parameter. A higher value
 * for `maxEntries` leads to a tree with fewer, fuller nodes. This can speed up initial loading and
 * insertion operations but may slow down search queries because each node covers a larger area.
 * Conversely, a lower `maxEntries` value creates a more fine-grained tree with smaller nodes, which
 * can be faster for searches but slower for insertions. The default value of 16 is a commonly used
 * and well-balanced choice.
 *
 * @param T The type of [Feature] to be stored in the tree. The feature's geometry is used to
 *   calculate its bounding box for indexing.
 * @param maxEntries The maximum number of entries in a single tree node. A higher value leads to
 *   faster loading/insertion but slower searches, and vice versa. Must be 4 or greater. Defaults
 *   to 16.
 */
public class RTree<T : GeoJsonObject>(
    initialData: List<T> = emptyList(),
    private val maxEntries: Int = 16,
) : Collection<T> {
    internal data class Node<T : GeoJsonObject>(
        var item: T? = null,
        var children: MutableList<Node<T>> = mutableListOf(),
        var height: Int = 1,
        var leaf: Boolean = true,
        var bbox: BoundingBox =
            item?.bbox
                ?: BoundingBox(
                    Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    Double.NEGATIVE_INFINITY,
                    Double.NEGATIVE_INFINITY,
                ),
    ) {
        fun computeBbox() {
            val item = item
            bbox =
                if (item != null) {
                    item.bbox!!
                } else {
                    computeBbox(
                        buildList {
                            children.forEach {
                                add(it.bbox.northeast)
                                add(it.bbox.southwest)
                            }
                        }
                    )
                }
        }
    }

    private val minEntries: Int = max(2, ceil(this.maxEntries * 0.4).toInt())
    internal var data: Node<T> = Node()

    private var cachedSize: Int? = null

    init {
        require(maxEntries >= 4) { "maxEntries must be 4 or greater" }
        insert(initialData)
    }

    override val size: Int
        get() {
            if (cachedSize == null) {
                cachedSize = countItems(data)
            }
            return cachedSize!!
        }

    override fun isEmpty(): Boolean {
        return data.children.isEmpty()
    }

    override fun iterator(): Iterator<T> {
        return RTreeIterator(data)
    }

    override fun contains(element: T): Boolean {
        val elementWithBbox = element.withBoundingBox()
        val bbox = elementWithBbox.bbox ?: return false

        val candidates = search(bbox)
        return candidates.any { it == element }
    }

    override fun containsAll(elements: Collection<T>): Boolean {
        return elements.all { contains(it) }
    }

    /**
     * Searches the R-tree for all items that intersect with the given bounding box.
     *
     * This method efficiently finds all features whose bounding boxes overlap with the provided
     * `bbox`. It traverses the tree, pruning branches that do not intersect with the search area,
     * making it much faster than a linear scan for large datasets.
     *
     * @param bbox The [BoundingBox] to search within.
     * @return A list of items (`T`) whose bounding boxes intersect with the given `bbox`. Returns
     *   an empty list if no intersecting items are found.
     */
    public fun search(bbox: BoundingBox): List<T> {
        var node: Node<T>? = data
        val result = mutableListOf<T>()

        if (!intersects(bbox, node!!.bbox)) return result

        val nodesToSearch = mutableListOf<Node<T>>()

        while (node != null) {
            for (i in 0 until node.children.size) {
                val child = node.children[i]

                if (intersects(bbox, child.bbox)) {
                    if (node.leaf) {
                        result.add(child.item!!)
                    } else if (contains(bbox, child.bbox)) {
                        all(child, result)
                    } else {
                        nodesToSearch.add(child)
                    }
                }
            }
            node = nodesToSearch.removeLastOrNull()
        }

        return result
    }

    /**
     * Checks if there are any items in the tree that intersect with the given bounding box. This is
     * a faster alternative to `search` if you only need to know if an intersection exists, as it
     * returns as soon as the first overlapping item is found.
     *
     * @param bbox The bounding box to check for collisions.
     * @return `true` if any item in the tree intersects with the `bbox`, `false` otherwise.
     */
    public fun collides(bbox: BoundingBox): Boolean {
        var node: Node<T>? = data

        if (!intersects(bbox, node!!.bbox)) return false

        val nodesToSearch = mutableListOf<Node<T>>()
        while (node != null) {
            for (i in 0 until node.children.size) {
                val child = node.children[i]
                if (intersects(bbox, child.bbox)) {
                    if (node.leaf || contains(bbox, child.bbox)) return true
                    nodesToSearch.add(child)
                }
            }
            node = nodesToSearch.removeLastOrNull()
        }

        return false
    }

    /**
     * Bulk-loads data into the R-tree.
     *
     * @param data A list of features to load into the tree.
     * @return The current [RTree] instance for chaining.
     */
    public fun insert(data: List<T>) {
        if (data.isEmpty()) return
        cachedSize = null

        val data = data.map { it.withBoundingBox() }

        if (data.size < minEntries) {
            data.forEach { insert(it) }
            return
        }

        var node = build(data.toMutableList(), 0, data.size - 1, 0)

        if (this.data.children.isEmpty()) {
            this.data = node
        } else if (this.data.height == node.height) {
            splitRoot(this.data, node)
        } else {
            if (this.data.height < node.height) {
                val tmpNode = this.data
                this.data = node
                node = tmpNode
            }
            insert(node, this.data.height - node.height - 1)
        }
    }

    /**
     * Inserts a single item into the tree.
     *
     * @param item The feature to insert. Its bounding box will be calculated if not already
     *   present.
     */
    public fun insert(item: T) {
        cachedSize = null
        val node = Node(item.withBoundingBox())
        insert(node, data.height - 1)
    }

    /** Removes all items from the tree. */
    public fun clear() {
        cachedSize = 0
        data = Node()
    }

    /**
     * Removes a specific item from the tree.
     *
     * This method finds the given item in the R-tree and removes it. The tree is then rebalanced by
     * condensing the path from the removed item's leaf node up to the root, ensuring the tree's
     * structural integrity. If the item is not found, the tree remains unchanged and the method
     * returns `false`.
     *
     * **Note:** Item equality is used to find the item to remove. Ensure that the `equals` method
     * of your feature type `T` is correctly implemented. The item's bounding box must also be
     * available for the search to work correctly.
     *
     * @param item The item to remove from the tree.
     * @return `true` if the item was successfully found and removed, `false` otherwise.
     */
    public fun remove(item: T): Boolean {
        val item = item.withBoundingBox()
        var node: Node<T>? = data
        val path = mutableListOf<Node<T>>()
        val indexes = mutableListOf<Int>()
        var i = 0
        var parent: Node<T>?
        var goingUp = false

        while (node != null || path.isNotEmpty()) {
            if (node == null) {
                node = path.removeLastOrNull()
                parent = path.lastOrNull()
                i = indexes.removeLastOrNull() ?: 0
                goingUp = true
            }

            if (node!!.leaf) {
                val index = findItem(item, node.children)
                if (index != -1) {
                    node.children.removeAt(index)
                    path.add(node)
                    condense(path)
                    cachedSize = null
                    return true
                }
            }

            if (!goingUp && !node.leaf && contains(node.bbox, item.bbox!!)) {
                path.add(node)
                indexes.add(i)
                i = 0
                parent = node
                node = node.children[0]
            } else {
                parent = path.lastOrNull()
                if (parent != null) {
                    i++
                    node = if (i < parent.children.size) parent.children[i] else null
                    goingUp = false
                } else {
                    node = null
                }
            }
        }

        return false
    }

    private fun compareMinX(a: T, b: T): Int {
        return (a.bbox!!.southwest.longitude - b.bbox!!.southwest.longitude).compareTo(0.0)
    }

    private fun compareMinY(a: T, b: T): Int {
        return (a.bbox!!.southwest.latitude - b.bbox!!.southwest.latitude).compareTo(0.0)
    }

    private fun all(node: Node<T>, result: MutableList<T>): List<T> {
        val nodesToSearch = mutableListOf<Node<T>>()
        var current: Node<T>? = node

        while (current != null) {
            if (current.leaf) {
                result.addAll(current.children.mapNotNull { it.item })
            } else {
                nodesToSearch.addAll(current.children)
            }
            current = nodesToSearch.removeLastOrNull()
        }
        return result
    }

    private fun build(items: MutableList<T>, left: Int, right: Int, height: Int): Node<T> {
        val n = right - left + 1
        var m = maxEntries
        var nodeHeight = height

        if (n <= m) {
            val node =
                Node(
                    null,
                    items
                        .subList(left, right + 1)
                        .map {
                            val child = Node(item = it)
                            child.computeBbox()
                            child
                        }
                        .toMutableList(),
                )
            node.computeBbox()
            return node
        }

        if (nodeHeight == 0) {
            nodeHeight = ceil(log(n.toDouble(), m.toDouble())).toInt()
            m = ceil(n / m.toDouble().pow(nodeHeight - 1)).toInt()
        }

        val node = Node<T>()
        node.leaf = false
        node.height = nodeHeight

        val n2 = ceil(n.toDouble() / m).toInt()
        val n1 = n2 * ceil(sqrt(m.toDouble())).toInt()

        multiSelect(items, left, right, n1) { a, b -> compareMinX(a, b) }

        var i = left
        while (i <= right) {
            val right2 = min(i + n1 - 1, right)
            multiSelect(items, i, right2, n2) { a, b -> compareMinY(a, b) }

            var j = i
            while (j <= right2) {
                val right3 = min(j + n2 - 1, right2)
                node.children.add(build(items, j, right3, nodeHeight - 1))
                j += n2
            }
            i += n1
        }

        node.computeBbox()
        return node
    }

    private fun chooseSubtree(
        bbox: BoundingBox,
        node: Node<T>,
        level: Int,
        path: MutableList<Node<T>>,
    ): Node<T> {
        var current = node

        while (true) {
            path.add(current)

            if (current.leaf || path.size - 1 == level) break

            var minArea = Double.POSITIVE_INFINITY
            var minEnlargement = Double.POSITIVE_INFINITY
            var targetNode: Node<T>? = null

            for (i in 0 until current.children.size) {
                val child = current.children[i]
                val area = bboxArea(child.bbox)
                val enlargement = enlargedArea(bbox, child.bbox) - area

                if (enlargement < minEnlargement) {
                    minEnlargement = enlargement
                    minArea = if (area < minArea) area else minArea
                    targetNode = child
                } else if (enlargement == minEnlargement) {
                    if (area < minArea) {
                        minArea = area
                        targetNode = child
                    }
                }
            }

            current = targetNode ?: current.children[0]
        }

        return current
    }

    private fun insert(item: Node<T>, level: Int) {
        val insertPath = mutableListOf<Node<T>>()

        val node = chooseSubtree(item.bbox, data, level, insertPath)

        node.children.add(item)
        node.bbox = extend(node.bbox, item.bbox)

        var currentLevel = level
        while (currentLevel >= 0) {
            if (insertPath[currentLevel].children.size > maxEntries) {
                split(insertPath, currentLevel)
                currentLevel--
            } else break
        }

        adjustParentBBoxes(item.bbox, insertPath, currentLevel)
    }

    private fun split(insertPath: MutableList<Node<T>>, level: Int) {
        val node = insertPath[level]
        val m = node.children.size
        val minEntries = this.minEntries

        chooseSplitAxis(node, minEntries, m)

        val splitIndex = chooseSplitIndex(node, minEntries, m)

        val newNode =
            Node(null, node.children.subList(splitIndex, node.children.size).toMutableList())
        node.children = node.children.subList(0, splitIndex).toMutableList()
        newNode.height = node.height
        newNode.leaf = node.leaf

        node.computeBbox()
        newNode.computeBbox()

        if (level > 0) {
            insertPath[level - 1].children.add(newNode)
        } else {
            splitRoot(node, newNode)
        }
    }

    private fun splitRoot(node: Node<T>, newNode: Node<T>) {
        data = Node(null, mutableListOf(node, newNode))
        data.height = node.height + 1
        data.leaf = false
        data.computeBbox()
    }

    private fun chooseSplitIndex(node: Node<T>, m: Int, bigM: Int): Int {
        var index = m
        var minOverlap = Double.POSITIVE_INFINITY
        var minArea = Double.POSITIVE_INFINITY

        for (i in m..(bigM - m)) {
            val bbox1 = distBBox(node, 0, i)
            val bbox2 = distBBox(node, i, bigM)

            val overlap = intersectionArea(bbox1, bbox2)
            val area = bboxArea(bbox1) + bboxArea(bbox2)

            if (overlap < minOverlap) {
                minOverlap = overlap
                index = i
                minArea = if (area < minArea) area else minArea
            } else if (overlap == minOverlap) {
                if (area < minArea) {
                    minArea = area
                    index = i
                }
            }
        }

        return index
    }

    private fun chooseSplitAxis(node: Node<T>, m: Int, bigM: Int) {
        val compareMinX: (Node<T>, Node<T>) -> Int =
            if (node.leaf) {
                { a, b -> compareMinX(a.item!!, b.item!!) }
            } else {
                { a, b -> compareNodeMinX(a, b) }
            }

        val compareMinY: (Node<T>, Node<T>) -> Int =
            if (node.leaf) {
                { a, b -> compareMinY(a.item!!, b.item!!) }
            } else {
                { a, b -> compareNodeMinY(a, b) }
            }

        val xMargin = allDistMargin(node, m, bigM, compareMinX)
        val yMargin = allDistMargin(node, m, bigM, compareMinY)

        if (xMargin < yMargin) {
            node.children.sortWith { a, b -> compareMinX(a, b) }
        }
    }

    private fun allDistMargin(
        node: Node<T>,
        m: Int,
        bigM: Int,
        compare: (Node<T>, Node<T>) -> Int,
    ): Double {
        node.children.sortWith { a, b -> compare(a, b) }

        var leftBBox = distBBox(node, 0, m)
        var rightBBox = distBBox(node, bigM - m, bigM)
        var margin = bboxMargin(leftBBox) + bboxMargin(rightBBox)

        for (i in m until (bigM - m)) {
            val child = node.children[i]
            leftBBox = extend(leftBBox, child.bbox)
            margin += bboxMargin(leftBBox)
        }

        for (i in (bigM - m - 1) downTo m) {
            val child = node.children[i]
            rightBBox = extend(rightBBox, child.bbox)
            margin += bboxMargin(rightBBox)
        }

        return margin
    }

    private fun adjustParentBBoxes(bbox: BoundingBox, path: MutableList<Node<T>>, level: Int) {
        for (i in level downTo 0) {
            path[i].bbox = extend(path[i].bbox, bbox)
        }
    }

    private fun condense(path: MutableList<Node<T>>) {
        for (i in (path.size - 1) downTo 0) {
            if (path[i].children.isEmpty()) {
                if (i > 0) {
                    val siblings = path[i - 1].children
                    siblings.remove(path[i])
                } else {
                    clear()
                }
            } else {
                path[i].computeBbox()
            }
        }
    }

    private fun findItem(item: T, items: List<Node<T>>): Int {
        for (i in items.indices) {
            if (item == items[i].item) {
                return i
            }
        }
        return -1
    }

    private fun distBBox(node: Node<T>, k: Int, p: Int): BoundingBox {
        var minX = Double.POSITIVE_INFINITY
        var minY = Double.POSITIVE_INFINITY
        var maxX = Double.NEGATIVE_INFINITY
        var maxY = Double.NEGATIVE_INFINITY

        for (i in k until p) {
            val child = node.children[i]

            minX = min(minX, child.bbox.southwest.longitude)
            minY = min(minY, child.bbox.southwest.latitude)
            maxX = max(maxX, child.bbox.northeast.longitude)
            maxY = max(maxY, child.bbox.northeast.latitude)
        }

        return BoundingBox(minX, minY, maxX, maxY)
    }

    private fun compareNodeMinX(a: Node<T>, b: Node<T>): Int {
        return (a.bbox.southwest.longitude - b.bbox.southwest.longitude).compareTo(0.0)
    }

    private fun compareNodeMinY(a: Node<T>, b: Node<T>): Int {
        return (a.bbox.southwest.latitude - b.bbox.southwest.latitude).compareTo(0.0)
    }

    private fun countItems(node: Node<T>): Int {
        if (node.leaf) {
            return node.children.size
        }
        return node.children.sumOf { countItems(it) }
    }

    private inner class RTreeIterator(root: Node<T>) : Iterator<T> {
        private val nodeStack = mutableListOf<Pair<Node<T>, Int>>()
        private var nextItem: T? = null

        init {
            if (root.children.isNotEmpty()) {
                nodeStack.add(root to 0)
                advance()
            }
        }

        private fun advance() {
            nextItem = null
            while (nodeStack.isNotEmpty() && nextItem == null) {
                val (currentNode, childIndex) = nodeStack.removeAt(nodeStack.size - 1)

                if (childIndex < currentNode.children.size) {
                    nodeStack.add(currentNode to childIndex + 1)
                    val child = currentNode.children[childIndex]

                    if (currentNode.leaf) {
                        nextItem = child.item
                    } else {
                        nodeStack.add(child to 0)
                    }
                }
            }
        }

        override fun hasNext(): Boolean {
            return nextItem != null
        }

        override fun next(): T {
            val item = nextItem ?: throw NoSuchElementException()
            advance()
            return item
        }
    }

    private companion object {
        private fun extend(a: BoundingBox, b: BoundingBox): BoundingBox {
            return BoundingBox(
                min(a.west, b.west),
                min(a.south, b.south),
                max(a.east, b.east),
                max(a.north, b.north),
            )
        }

        private fun bboxArea(a: BoundingBox): Double {
            return (a.east - a.west) * (a.north - a.south)
        }

        private fun bboxMargin(a: BoundingBox): Double {
            return (a.east - a.west) + (a.north - a.south)
        }

        private fun enlargedArea(a: BoundingBox, b: BoundingBox): Double {
            return (max(b.east, a.east) - min(b.west, a.west)) *
                (max(b.north, a.north) - min(b.south, a.south))
        }

        private fun intersectionArea(a: BoundingBox, b: BoundingBox): Double {
            val minX = max(a.west, b.west)
            val minY = max(a.south, b.south)
            val maxX = min(a.east, b.east)
            val maxY = min(a.north, b.north)

            return max(0.0, maxX - minX) * max(0.0, maxY - minY)
        }

        private fun contains(a: BoundingBox, b: BoundingBox): Boolean {
            return a.west <= b.west && a.south <= b.south && b.east <= a.east && b.north <= a.north
        }

        private fun intersects(a: BoundingBox, b: BoundingBox): Boolean {
            return b.west <= a.east && b.south <= a.north && b.east >= a.west && b.north >= a.south
        }

        private fun <T> multiSelect(
            arr: MutableList<T>,
            left: Int,
            right: Int,
            n: Int,
            compare: (T, T) -> Int,
        ) {
            val stack = mutableListOf(left, right)

            while (stack.isNotEmpty()) {
                val rightIdx = stack.removeAt(stack.size - 1)
                val leftIdx = stack.removeAt(stack.size - 1)

                if (rightIdx - leftIdx <= n) continue

                val mid = leftIdx + ceil((rightIdx - leftIdx).toDouble() / n / 2).toInt() * n
                quickSelect(arr, mid, leftIdx, rightIdx, compare)

                stack.add(leftIdx)
                stack.add(mid)
                stack.add(mid)
                stack.add(rightIdx)
            }
        }

        private fun <T> quickSelect(
            arr: MutableList<T>,
            k: Int,
            left: Int,
            right: Int,
            compare: (T, T) -> Int,
        ) {
            var l = left
            var r = right

            while (r > l) {
                if (r - l > 600) {
                    val n = (r - l + 1).toDouble()
                    val m = (k - l + 1).toDouble()
                    val z = log(n, E)
                    val s = 0.5 * exp(2 * z / 3)
                    val sd = 0.5 * sqrt(z * s * (n - s) / n) * if (m - n / 2 < 0) -1 else 1
                    val newLeft = max(l.toDouble(), (k - m * s / n + sd).toInt().toDouble()).toInt()
                    val newRight =
                        min(r.toDouble(), (k + (n - m) * s / n + sd).toInt().toDouble()).toInt()
                    quickSelect(arr, k, newLeft, newRight, compare)
                }

                val t = arr[k]
                var i = l
                var j = r

                arr[l] = arr[k].also { arr[k] = arr[l] }
                if (compare(arr[r], t) > 0) {
                    arr[l] = arr[r].also { arr[r] = arr[l] }
                }

                while (i < j) {
                    arr[i] = arr[j].also { arr[j] = arr[i] }
                    i++
                    j--
                    while (compare(arr[i], t) < 0) i++
                    while (compare(arr[j], t) > 0) j--
                }

                if (compare(arr[l], t) == 0) {
                    arr[l] = arr[j].also { arr[j] = arr[l] }
                } else {
                    j++
                    arr[j] = arr[r].also { arr[r] = arr[j] }
                }

                if (j <= k) l = j + 1
                if (k <= j) r = j - 1
            }
        }
    }

    private fun T.withBoundingBox(): T =
        if (bbox == null) {
            @Suppress("UNCHECKED_CAST")
            when (this) {
                is FeatureCollection<*, *> -> {
                    val coords = flattenCoordinates()
                    copy(bbox = if (coords.isNotEmpty()) computeBbox(coords) else null)
                }
                is GeometryCollection<*> -> {
                    val coords = flattenCoordinates()
                    copy(bbox = if (coords.isNotEmpty()) computeBbox(coords) else null)
                }
                is Feature<*, *> -> copy(bbox = geometry?.computeBbox())
                is LineString -> copy(bbox = computeBbox())
                is MultiLineString -> copy(bbox = computeBbox())
                is MultiPoint -> copy(bbox = computeBbox())
                is MultiPolygon -> copy(bbox = computeBbox())
                is Point -> copy(bbox = computeBbox())
                is Polygon -> copy(bbox = computeBbox())
            }
                as T
        } else {
            this
        }
}
