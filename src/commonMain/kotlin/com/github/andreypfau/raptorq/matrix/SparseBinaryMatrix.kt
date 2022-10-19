@file:Suppress("OPT_IN_USAGE")

package com.github.andreypfau.raptorq.matrix

import com.github.andreypfau.raptorq.arraymap.ImmutableListMap
import com.github.andreypfau.raptorq.arraymap.ImmutableListMapBuilder
import com.github.andreypfau.raptorq.iterators.OctetIterator
import com.github.andreypfau.raptorq.octet.BinaryOctetVec
import com.github.andreypfau.raptorq.sparse.SparseBinaryVec
import com.github.andreypfau.raptorq.utils.bothIndices
import com.github.andreypfau.raptorq.utils.swap

/**
 * Stores a matrix in sparse representation, with an optional dense block for the right most columns
 * The logical storage is as follows:
 * |---------------------------------------|
 * |                          | (optional) |
 * |      sparse rows         | dense      |
 * |                          | columns    |
 * |---------------------------------------|
 */
class SparseBinaryMatrix(
    override var height: Int,
    override var width: Int,
    /**
     * Note these are stored right aligned, so that the right most element is always at
     * `denseElements[x] & (1 << 63)`
     */
    var numDenseColumns: Int,
    var sparseElements: MutableList<SparseBinaryVec> = Array(height) { SparseBinaryVec(10) }.toMutableList(),
    var denseElements: ULongArray = if (numDenseColumns > 0) {
        ULongArray(height * ((numDenseColumns - 1) / WORD_WIDTH + 1))
    } else {
        ULongArray(0)
    },
    var sparseColumnarValues: ImmutableListMap? = null,
    var logicalRowToPhysical: UIntArray = UIntArray(height) { it.toUInt() },
    var physicalRowToLogical: UIntArray = UIntArray(height) { it.toUInt() },
    var logicalColToPhysical: UShortArray = UShortArray(width) { it.toUShort() },
    var physicalColToLogical: UShortArray = UShortArray(width) { it.toUShort() },
    var columnIndexDisabled: Boolean = true,
) : BinaryMatrix {
    fun logicalColToDenseCol(col: Int): Int {
        require(col >= width - numDenseColumns)
        return col - (width - numDenseColumns)
    }

    fun bitPosition(row: Int, col: Int): Pair<Int, Int> {
        return row * rowWordWidth() + wordOffset(col) to (leftPaddingBits() + col) % WORD_WIDTH
    }

    fun rowWordWidth() = (numDenseColumns + WORD_WIDTH - 1) / WORD_WIDTH

    fun leftPaddingBits() = (WORD_WIDTH - (numDenseColumns % WORD_WIDTH)) % WORD_WIDTH

    fun wordOffset(bit: Int) = (leftPaddingBits() + bit) / WORD_WIDTH

    override operator fun set(i: Int, j: Int, value: Boolean) {
        val physicalI = logicalRowToPhysical[i].toInt()
        val physicalJ = logicalColToPhysical[j].toInt()
        if (width - j <= numDenseColumns) {
            val (word, bit) = bitPosition(physicalI, logicalColToDenseCol(j))
            if (value) {
                denseElements[word] = setBit(denseElements[word], bit)
            } else {
                denseElements[word] = clearBit(denseElements[word], bit)
            }
        } else {
            sparseElements[physicalJ][physicalI] = value
        }
    }

    override fun countOnes(row: Int, startCol: Int, endCol: Int): Int {
        if (endCol > width - numDenseColumns) {
            throw NotImplementedError("It was assumed that this wouldn't be needed, because the method would only be called on the V section of matrix A")
        }
        var ones = 0
        val physicalRow = logicalRowToPhysical[row].toInt()
        for ((physicalCol, value) in sparseElements[physicalRow].keysValues()) {
            val col = physicalColToLogical[physicalCol].toInt()
            if (col in startCol until endCol && value) {
                ones++
            }
        }
        return ones
    }

    /**
     * The following implementation is equivalent to
     *
     * `.map{ x -> get(row, x) } `
     *
     * but this implementation optimizes for sequential access and avoids all the
     * extra bit index math
     */
    override fun subRowAsOctets(row: Int, startCol: Int): BinaryOctetVec {
        val firstDenseColumn = width - numDenseColumns
        check(startCol == firstDenseColumn)

        val physicalRow = logicalRowToPhysical[row].toInt()
        val firstWord = bitPosition(physicalRow, 0).first
        val lastWord = firstWord + rowWordWidth()
        return BinaryOctetVec(denseElements.copyOfRange(firstWord, lastWord), numDenseColumns)
    }

    /**
     * The following implementation is equivalent to
     *
     * `.filter { x -> get(row, x) }`
     *
     * but this implementation optimizes for sequential access and avoids all the
     * extra bit index math
     */
    override fun nonZeroColumns(row: Int, startCol: Int): Sequence<Int> {
        require(startCol == width - numDenseColumns)
        return sequence {
            val physicalRow = logicalRowToPhysical[row].toInt()
            var (word, bit) = bitPosition(physicalRow, logicalColToDenseCol(startCol))
            var col = startCol
            // Process the first word, which may not be entirely filled, due to left zero padding
            // Because of assert that [startCol] is always the first dense column, the first one
            // must be the column we're looking for, so they're no need to zero out columns left of it.
            var block = denseElements[word]
            while (block.countTrailingZeroBits() < WORD_WIDTH) {
                yield(col + block.countTrailingZeroBits() - bit)
                block = block and selectMask(block.countTrailingZeroBits()).inv()
            }
            col += WORD_WIDTH - bit
            word++

            while (col < width) {
                block = denseElements[word]
                // process the whole word in one shot to improve efficiency
                while (block.countTrailingZeroBits() < WORD_WIDTH) {
                    yield(col + block.countTrailingZeroBits())
                    block = block and selectMask(block.countTrailingZeroBits()).inv()
                }
                col += WORD_WIDTH
                word++
            }
        }
    }

    override operator fun get(i: Int, j: Int): Boolean {
        val physicalI = logicalRowToPhysical[i].toInt()
        val physicalJ = logicalColToPhysical[j].toInt()
        return if (width - j <= numDenseColumns) {
            val (word, bit) = bitPosition(physicalI, logicalColToDenseCol(j))
            denseElements[word] and selectMask(bit) != 0UL
        } else {
            sparseElements[physicalJ][physicalI]
        }
    }

    override fun rowIterator(row: Int, startCol: Int, endCol: Int): OctetIterator {
        if (endCol > width - numDenseColumns) {
            throw NotImplementedError("It was assumed that this wouldn't be needed, because the method would only be called on the V section of matrix A")
        }
        val physicalRow = logicalRowToPhysical[row].toInt()
        val sparseElements = sparseElements[physicalRow]
        return OctetIterator.sparse(
            startCol,
            endCol,
            sparseElements,
            physicalColToLogical
        )
    }

    override fun onesInColumn(col: Int, startRow: Int, endRow: Int): List<Int> {
        require(!columnIndexDisabled)
        val sparseColumnarValues = requireNotNull(sparseColumnarValues)
        val physicalCol = logicalColToPhysical[col].toInt()
        val rows = ArrayList<Int>()
        for (physicalRow in sparseColumnarValues[physicalCol]) {
            val logicalRow = physicalRowToLogical[physicalRow.toInt()].toInt()
            if (logicalRow in startRow until endRow) {
                rows.add(logicalRow)
            }
        }
        return rows
    }

    override fun swapRows(i: Int, j: Int) {
        val physicalI = logicalRowToPhysical[i].toInt()
        val physicalJ = logicalRowToPhysical[j].toInt()
        logicalRowToPhysical.swap(i, j)
        physicalRowToLogical.swap(physicalI, physicalJ)
    }

    override fun swapColumns(i: Int, j: Int, startRowHint: Int) {
        if (j >= width - numDenseColumns) {
            throw NotImplementedError("It was assumed that this wouldn't be needed, because the method would only be called on the V section of matrix A")
        }
        val physicalI = logicalColToPhysical[i].toInt()
        val physicalJ = logicalColToPhysical[j].toInt()
        logicalColToPhysical.swap(i, j)
        physicalColToLogical.swap(physicalI, physicalJ)
    }

    override fun enableColumnAccessAcceleration() {
        columnIndexDisabled = false
        val builder = ImmutableListMapBuilder(height)
        sparseElements.forEachIndexed { physicalRow, elements ->
            elements.keysValues().forEach { (physicalRow, _) ->
                builder.add(physicalRow.toUShort(), physicalRow.toUInt())
            }
        }
        sparseColumnarValues = builder.build()
    }

    override fun disableColumnAccessAcceleration() {
        columnIndexDisabled = true
        sparseColumnarValues = null
    }

    override fun hintColumnDenseAndFrozen(i: Int) {
        require(width - numDenseColumns - 1 == i) {
            "Can only freeze the last sparse column"
        }
        require(!columnIndexDisabled)
        numDenseColumns++
        val (lastWord, _) = bitPosition(height - 1, numDenseColumns - 1)
        // If this is in a new word
        if (lastWord >= denseElements.size) {
            // Append a new set of words
            var src = denseElements.size
            denseElements = denseElements.copyOf(height)
            var dest = denseElements.size
            // Re-space the elements, so that each row has an empty word
            while (src > 0) {
                src -= 1
                dest -= 1
                denseElements[dest] = denseElements[src]
                if (dest % rowWordWidth() == 1) {
                    dest -= 1
                    denseElements[dest] = 0uL
                }
            }
            check(src == 0)
            check(dest == 0)
        }
        val physicalI = logicalColToPhysical[i].toInt()
        for (maybePresentInRow in sparseElements[physicalI]) {
            val physicalRow = maybePresentInRow.toInt()
            val value = sparseElements[physicalI].remove(physicalI)
            if (value) {
                val (word, bit) = bitPosition(physicalRow, 0)
                denseElements[word] = if (value) {
                    setBit(denseElements[word], bit)
                } else {
                    clearBit(denseElements[word], bit)
                }
            }
        }
    }

    override fun addAssignRows(dest: Int, src: Int, startCol: Int) {
        require(dest != src)
        require(startCol == 0 || startCol == width - numDenseColumns) {
            "startCol must be zero or at the beginning of the U matrix"
        }
        val physicalDest = logicalRowToPhysical[dest].toInt()
        val physicalSrc = logicalRowToPhysical[src].toInt()
        // First handle the dense columns
        if (numDenseColumns > 0) {
            val (destWord, _) = bitPosition(physicalDest, 0)
            val (srcWord, _) = bitPosition(physicalSrc, 0)
            for (word in 0 until rowWordWidth()) {
                denseElements[destWord + word] = denseElements[destWord + word] xor denseElements[srcWord + word]
            }
        }

        if (startCol == 0) {
            // Then the sparse columns
            val (destRow, tempRow) = sparseElements.bothIndices(physicalDest, physicalSrc)
            // This shouldn't be needed, because while column indexing is enabled in first phase,
            // columns are only eliminated one at a time in sparse section of matrix.
            check(columnIndexDisabled || tempRow.size == 1)

            val columnAdded = destRow.addAssign(tempRow)
            // This shouldn't be needed, because while column indexing is enabled in first phase,
            // columns are only removed.
            check(columnIndexDisabled || !columnAdded)
        }
    }

    override fun resize(newHeight: Int, newWidth: Int) {
        require(newHeight <= height)
        // Only support same width or removing all the dense columns
        var columnsToRemove = width - newWidth
        require(columnsToRemove == 0 || columnsToRemove >= numDenseColumns)
        if (!columnIndexDisabled) {
            throw NotImplementedError("Resize should only be used in phase 2, after column indexing is no longer needed")
        }
        val newSparse = Array<SparseBinaryVec?>(newHeight) { null }
        for (i in (sparseElements.indices).reversed()) {
            val logicalRow = physicalRowToLogical[i].toInt()
            val sparse = sparseElements.removeLastOrNull()
            if (logicalRow < newHeight) {
                newSparse[logicalRow] = sparse
            }
        }

        if (columnsToRemove == 0 && numDenseColumns > 0) {
            // TODO: optimize to not allocate this extra vec
            val newDense = ULongArray(newHeight * rowWordWidth())
            for (logicalRow in 0 until rowWordWidth()) {
                val physicalRow = logicalRowToPhysical[logicalRow].toInt()
                for (word in 0 until rowWordWidth()) {
                    newDense[logicalRow * rowWordWidth() + word] = denseElements[physicalRow * rowWordWidth() + word]
                }
            }
            denseElements = newDense
        } else {
            columnsToRemove -= numDenseColumns
            denseElements.fill(0u)
            numDenseColumns = 0
        }

        logicalRowToPhysical = logicalRowToPhysical.copyOf(newHeight)
        physicalRowToLogical = physicalRowToLogical.copyOf(newHeight)

        for (i in 0 until newHeight) {
            logicalRowToPhysical[i] = i.toUInt()
            physicalRowToLogical[i] = i.toUInt()
        }

        for (row in newSparse) {
            sparseElements.add(row!!)
        }

        if (columnsToRemove > 0) {
            val physicalToLogical = physicalColToLogical
            for (row in 0 until sparseElements.size) {
                val sparse = sparseElements[row]
                sparse.retain { (col, _) -> physicalToLogical[col] < newWidth.toUInt() }
            }
        }

        width = newWidth
        height = newHeight
    }

    companion object {
        val WORD_WIDTH = 64

        fun selectMask(bit: Int) = 1UL shl bit
        fun clearBit(word: ULong, bit: Int) = word and selectMask(bit).inv()
        fun setBit(word: ULong, bit: Int) = word or selectMask(bit)
    }
}
