package com.github.andreypfau.raptorq.arraymap

class AdjacentIterator(
    val edges: Iterator<Pair<UShort, UShort>>,
    val node: UShort
) : Iterator<UShort> {
    private var next: UShort? = null

    override fun hasNext(): Boolean {
        adjacent()
        return next != null
    }

    override fun next(): UShort = next ?: throw NoSuchElementException()

    private fun adjacent() {
        while (edges.hasNext()) {
            val (node, adjacent) = edges.next()
            if (node == this.node) {
                next = adjacent
            }
        }
    }
}
