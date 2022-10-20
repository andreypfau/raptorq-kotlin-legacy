@file:Suppress("OPT_IN_USAGE")

package com.github.andreypfau.raptorq.iterators

import com.github.andreypfau.raptorq.matrix.DenseBinaryMatrix
import com.github.andreypfau.raptorq.sparse.SparseBinaryVec

class BinaryIterator(
    val startCol: Int,
    val endCol: Int,
    val denseElements: ULongArray,
    var denseIndex: Int,
    var denseWordIndex: Int,
    var denseBitIndex: Int,
    val sparseElements: SparseBinaryVec?,
    var sparseIndex: Int,
    val sparsePhysicalColToLogical: UShortArray
) : Iterator<Pair<Int, Boolean>> {
    private var nextElement: Pair<Int, Boolean>? = nextOrNull()

    override fun hasNext(): Boolean = nextElement != null

    override fun next(): Pair<Int, Boolean> {
        try {
            return nextElement ?: throw NoSuchElementException()
        } finally {
            nextElement = nextOrNull()
        }
    }

    private fun nextOrNull(): Pair<Int, Boolean>? {
        if (sparseElements != null) {
            val elements = sparseElements
            // Need to iterate over the whole array, since they're not sorted by logical col
            if (sparseIndex < elements.size) {
                while (sparseIndex < elements.size) {
                    val entry = elements.getByRawIndex(sparseIndex)
                    sparseIndex++
                    val logicalCol = sparsePhysicalColToLogical[entry.first].toInt()
                    if (logicalCol in startCol until endCol) {
                        return Pair(logicalCol, entry.second)
                    }
                }
            }
            return null
        } else if (denseIndex == endCol) {
            return null
        } else {
            val oldIndex = denseIndex
            denseIndex += 1
            val value = denseElements[denseWordIndex] and DenseBinaryMatrix.selectMask(denseBitIndex) != 0uL
            denseBitIndex++
            if (denseBitIndex == 64) {
                denseBitIndex = 0
                denseWordIndex++
            }
            return oldIndex to value
        }
    }

    companion object {
        fun sparse(
            startCol: Int,
            endCol: Int,
            sparseElements: SparseBinaryVec,
            sparsePhysicalColToLogical: UShortArray
        ): BinaryIterator {
            return BinaryIterator(
                startCol = startCol,
                endCol = endCol,
                denseElements = ULongArray(0),
                denseIndex = 0,
                denseWordIndex = 0,
                denseBitIndex = 0,
                sparseElements = sparseElements,
                sparseIndex = 0,
                sparsePhysicalColToLogical = sparsePhysicalColToLogical
            )
        }

        fun dense(
            startCol: Int,
            endCol: Int,
            startBit: Int,
            denseElements: ULongArray
        ): BinaryIterator {
            return BinaryIterator(
                startCol = 0,
                endCol = endCol,
                denseElements = denseElements,
                denseIndex = startCol,
                denseWordIndex = 0,
                denseBitIndex = startBit,
                sparseElements = null,
                sparseIndex = 0,
                sparsePhysicalColToLogical = ushortArrayOf()
            )
        }
    }
}