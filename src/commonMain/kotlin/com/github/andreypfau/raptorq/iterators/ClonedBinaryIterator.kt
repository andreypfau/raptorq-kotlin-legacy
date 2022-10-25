@file:OptIn(ExperimentalUnsignedTypes::class)

package com.github.andreypfau.raptorq.iterators

import com.github.andreypfau.raptorq.matrix.DenseBinaryMatrix

class ClonedBinaryIterator(
    private val endCol: Int,
    private val denseElements: ULongArray,
    private var denseIndex: Int,
    private var denseWordIndex: Int,
    private var denseBitIndex: Int,
    private val sparseElements: List<Pair<Int, Boolean>>?,
    private var sparseIndex: Int
) : Iterator<Pair<Int, Boolean>> {
    override fun hasNext(): Boolean {
        return if (sparseElements != null) {
            sparseIndex != sparseElements.size
        } else {
            denseIndex != endCol
        }
    }

    override fun next(): Pair<Int, Boolean> = nextElement() ?: throw NoSuchElementException()

    private fun nextElement(): Pair<Int, Boolean>? {
        if (sparseElements != null) {
            val elements = sparseElements
            return if (sparseIndex == elements.size) {
                null
            } else {
                val oldIndex = sparseIndex
                sparseIndex++
                elements[oldIndex]
            }
        } else if (denseIndex == endCol) {
            return null
        } else {
            val oldIndex = denseIndex
            val value = denseElements[denseWordIndex] and DenseBinaryMatrix.selectMask(denseBitIndex) != 0uL
            denseIndex++
            denseBitIndex++
            if (denseBitIndex == 64) {
                denseBitIndex = 0
                denseWordIndex++
            }
            return oldIndex to value
        }
    }
}
