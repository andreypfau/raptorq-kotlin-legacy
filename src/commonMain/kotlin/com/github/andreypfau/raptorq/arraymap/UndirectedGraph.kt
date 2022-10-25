package com.github.andreypfau.raptorq.arraymap

class UndirectedGraph(
    val edges: MutableList<Pair<UShort, UShort>>,
    val nodeEdgeStartingIndex: U32VecMap
) : Iterable<UShort> {
    constructor(startNode: UShort, endNode: UShort, edges: Int) : this(
        edges = ArrayList(edges * 2),
        nodeEdgeStartingIndex = U32VecMap(startNode.toInt(), endNode.toInt())
    )

    fun addEdge(node1: UShort, node2: UShort) {
        edges.add(node1 to node2)
        edges.add(node2 to node1)
    }

    fun build() {
        // Ordering of adjacencies doesn't matter, so just sort by the first node
        edges.sortBy { it.first }
        if (edges.isEmpty()) return
        var lastNode = edges[0].first
        nodeEdgeStartingIndex[lastNode.toInt()] = 0u
        edges.forEachIndexed { index, (node, _) ->
            if (lastNode != node) {
                lastNode = node
                nodeEdgeStartingIndex[lastNode.toInt()] = index.toUInt()
            }
        }
    }

    fun getAdjacentNodes(node: UShort): AdjacentIterator {
        val firstCandidate = nodeEdgeStartingIndex[node.toInt()]
        return AdjacentIterator(edges.iterator().apply {
            repeat(firstCandidate.toInt()) { next() }
        }, node)
    }

    fun nodes(): Sequence<UShort> = edges.asSequence().map { it.first }.distinct()

    override fun iterator(): Iterator<UShort> = nodes().iterator()
}
