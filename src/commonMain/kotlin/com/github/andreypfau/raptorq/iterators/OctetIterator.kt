@file:Suppress("OPT_IN_USAGE")

package com.github.andreypfau.raptorq.iterators

import com.github.andreypfau.raptorq.matrix.DenseBinaryMatrix
import com.github.andreypfau.raptorq.octet.Octet
import com.github.andreypfau.raptorq.sparse.SparseBinaryVec

class OctetIterator(
    val startCol: Int,
    val endCol: Int,
    val denseElements: ULongArray,
    var denseIndex: Int,
    var denseWordIndex: Int,
    var denseBitIndex: Int,
    val sparseElements: SparseBinaryVec?,
    var sparseIndex: Int,
    val sparsePhysicalColToLogical: UShortArray
) : Iterator<Pair<Int, Octet>> {
    private var nextElement: Pair<Int, Octet>? = nextOrNull()

    override fun hasNext(): Boolean = nextElement != null

    override fun next(): Pair<Int, Octet> {
        try {
            return nextElement ?: throw NoSuchElementException()
        } finally {
            nextElement = nextOrNull()
        }
    }

    private fun nextOrNull(): Pair<Int, Octet>? {
        if (sparseElements != null) {
            val elements = sparseElements
            // Need to iterate over the whole array, since they're not sorted by logical col
            if (sparseIndex < elements.size) {
                while (sparseIndex < elements.size) {
                    val entry = elements.getByRawIndex(sparseIndex)
                    sparseIndex += 1
                    val logicalCol = sparsePhysicalColToLogical[entry.first]
                    if (logicalCol >= startCol.toUShort() && logicalCol < endCol.toUShort()) {
                        return logicalCol.toInt() to entry.second
                    }
                }
            }
            return null
        } else if (denseIndex == endCol) {
            return null
        } else {
            val oldIndex = denseIndex
            denseIndex += 1
            val value = if (denseElements[denseWordIndex] and DenseBinaryMatrix.selectMask(denseBitIndex) == 0uL) {
                Octet.ZERO
            } else {
                Octet.ONE
            }
            denseBitIndex += 1
            if (denseBitIndex == 64) {
                denseBitIndex = 0
                denseWordIndex += 1
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
        ): OctetIterator {
            return OctetIterator(
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

        fun denseBinary(
            startCol: Int,
            endCol: Int,
            startBit: Int,
            denseElements: ULongArray
        ): OctetIterator {
            return OctetIterator(
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
