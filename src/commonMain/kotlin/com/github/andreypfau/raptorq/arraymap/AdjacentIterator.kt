package com.github.andreypfau.raptorq.arraymap

class AdjacentIterator(
    val edges: Iterator<Pair<UShort, UShort>>,
    val node: UShort
) : Iterator<UShort> {
    private var next: UShort? = adjacent()

    override fun hasNext(): Boolean = next != null

    override fun next(): UShort = try {
        next ?: throw NoSuchElementException()
    } finally {
        next = adjacent()
    }

    private fun adjacent(): UShort? {
        while (edges.hasNext()) {
            val (node, adjacent) = edges.next()
            if (node == this.node) {
                return adjacent
            }
        }
        return null
    }
}
