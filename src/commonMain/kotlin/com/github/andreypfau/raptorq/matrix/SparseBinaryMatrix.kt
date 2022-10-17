@file:Suppress("OPT_IN_USAGE")

package com.github.andreypfau.raptorq.matrix

import com.github.andreypfau.raptorq.arraymap.ImmutableListMap
import com.github.andreypfau.raptorq.arraymap.ImmutableListMapBuilder
import com.github.andreypfau.raptorq.iterators.OctetIterator
import com.github.andreypfau.raptorq.octet.BinaryOctetVec
import com.github.andreypfau.raptorq.octet.Octet
import com.github.andreypfau.raptorq.sparse.SparseBinaryVec
import com.github.andreypfau.raptorq.utils.bothIndices

class SparseBinaryMatrix(
    override var height: Int,
    override var width: Int,
    var sparseElements: MutableList<SparseBinaryVec>,
    var denseElements: ULongArray,
    var sparseColumnarValues: ImmutableListMap?,
    var logicalRowToPhysical: UIntArray,
    var physicalRowToLogical: UIntArray,
    var logicalColToPhysical: UShortArray,
    var physicalColToLogical: UShortArray,
    var columnIndexDisabled: Boolean,
    var numDenseColumns: Int,
) : BinaryMatrix {
    companion object {
        val WORD_WIDTH = 64

        fun selectMask(bit: Int) = 1UL shl bit
        fun clearBit(word: ULong, bit: Int) = word and selectMask(bit).inv()
        fun setBit(word: ULong, bit: Int) = word or selectMask(bit)
    }

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

    override operator fun set(i: Int, j: Int, value: Octet) {
        val physicalI = logicalRowToPhysical[i].toInt()
        val physicalJ = logicalColToPhysical[j].toInt()
        if (width - j <= numDenseColumns) {
            val (word, bit) = bitPosition(physicalI, logicalColToDenseCol(j))
            if (value == Octet.ZERO) {
                denseElements[word] = clearBit(denseElements[word], bit)
            } else {
                denseElements[word] = setBit(denseElements[word], bit)
            }
        } else {
            sparseElements[physicalJ].insert(physicalI, value)
        }
    }

    override fun countOnes(row: Int, startCol: Int, endCol: Int): Int {
        if (endCol > width - numDenseColumns) {
            TODO("It was assumed that this wouldn't be needed, because the method would only be called on the V section of matrix A")
        }
        var ones = 0
        val physicalRow = logicalRowToPhysical[row].toInt()
        for ((physicalCol, value) in sparseElements[physicalRow].keysValues()) {
            val col = physicalColToLogical[physicalCol].toInt()
            if (col in startCol until endCol && value == Octet.ONE) {
                ones++
            }
        }
        return ones
    }

    override fun getSubRowAsOctets(row: Int, startCol: Int): BinaryOctetVec {
        val firstDenseColumn = width - numDenseColumns
        check(startCol == firstDenseColumn)
        val physicalRow = logicalRowToPhysical[row].toInt()
        val firstWord = bitPosition(physicalRow, 0).first
        val lastWord = firstWord + rowWordWidth()
        return BinaryOctetVec(denseElements.slice(firstWord until lastWord), numDenseColumns)
    }

    override fun queryNonZeroColumns(row: Int, startCol: Int): List<Int> {
        require(startCol == width - numDenseColumns)
        val result = ArrayList<Int>()
        val physicalRow = logicalRowToPhysical[row].toInt()
        var (word, bit) = bitPosition(physicalRow, logicalColToDenseCol(startCol))
        var col = startCol
        var block = denseElements[word]
        while (block.countTrailingZeroBits() < WORD_WIDTH) {
            result.add(col + block.countTrailingZeroBits() - bit)
            block = block and selectMask(block.countTrailingZeroBits()).inv()
        }
        col += WORD_WIDTH - bit
        word++

        while (col < width) {
            var block = denseElements[word]
            while (block.countTrailingZeroBits() < WORD_WIDTH) {
                result.add(col + block.countTrailingZeroBits())
                block = block and selectMask(block.countTrailingZeroBits()).inv()
            }
            col += WORD_WIDTH
            word++
        }

        return result
    }

    override operator fun get(i: Int, j: Int): Octet {
        val physicalI = logicalRowToPhysical[i].toInt()
        val physicalJ = logicalColToPhysical[j].toInt()
        return if (width - j <= numDenseColumns) {
            val (word, bit) = bitPosition(physicalI, logicalColToDenseCol(j))
            if (denseElements[word] and selectMask(bit) != 0UL) {
                Octet.ONE
            } else {
                Octet.ZERO
            }
        } else {
            sparseElements[physicalJ].get(physicalI) ?: Octet.ZERO
        }
    }

    override fun getRowIter(row: Int, startCol: Int, endCol: Int): OctetIterator {
        if (endCol > width - numDenseColumns) {
            TODO("It was assumed that this wouldn't be needed, because the method would only be called on the V section of matrix A")
        }
        val physicalRow = logicalRowToPhysical[row].toInt()
        val sparseElements = sparseElements[physicalRow]
        return OctetIterator.newSparse(
            startCol,
            endCol,
            sparseElements,
            physicalColToLogical
        )
    }

    override fun getOnesInColumn(col: Int, startRow: Int, endRow: Int): List<Int> {
        val physicalCol = logicalColToPhysical[col].toInt()
        val rows = ArrayList<Int>()
        for (physicalRow in sparseColumnarValues!![physicalCol]) {
            val logicalRow = physicalRowToLogical[physicalRow].toInt()
            if (logicalRow in startRow until endRow) {
                rows.add(logicalRow)
            }
        }
        return rows
    }

    override fun swapRows(i: Int, j: Int) {
        val physicalI = logicalRowToPhysical[i].toInt()
        val physicalJ = logicalRowToPhysical[j].toInt()
        logicalRowToPhysical[i] = j.toUInt()
        physicalRowToLogical[physicalI] = physicalJ.toUInt()
    }

    override fun swapColumns(i: Int, j: Int, startRowHint: Int) {
        val physicalI = logicalColToPhysical[i].toInt()
        val physicalJ = logicalColToPhysical[j].toInt()
        logicalColToPhysical[i] = j.toUShort()
        physicalColToLogical[physicalI] = physicalJ.toUShort()
    }

    override fun enableColumnAccessAcceleration() {
        columnIndexDisabled = false
        val builder = ImmutableListMapBuilder(height)
        sparseElements.forEachIndexed { physicalRow, elements ->
            elements.keysValues().forEach { (physicalRow, _) ->
                builder.add(physicalRow.toShort(), physicalRow)
            }
        }
        sparseColumnarValues = builder.build()
    }

    override fun disableColumnAccessAcceleration() {
        columnIndexDisabled = true
        sparseColumnarValues = null
    }

    override fun hintColumnDenseAndFrozen(i: Int) {
        require(width - numDenseColumns - 1 == i) { "Can only freeze the last sparse column" }
        require(!columnIndexDisabled)
        numDenseColumns++
        val (lastWord, _) = bitPosition(height - 1, numDenseColumns - 1)
        if (lastWord >= denseElements.size) {
            var src = denseElements.size
            denseElements = denseElements.copyOf(height)
            var dest = denseElements.size
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
            val physicalRow = maybePresentInRow
            val value = sparseElements[physicalI].remove(physicalI)
            if (value != null) {
                val (word, bit) = bitPosition(physicalRow.toInt(), 0)
                denseElements[word] = if (value == Octet.ZERO) {
                    clearBit(denseElements[word], bit)
                } else {
                    setBit(denseElements[word], bit)
                }
            }
        }
    }

    override fun addAssignRows(dest: Int, src: Int, startCol: Int) {
        require(dest != src)
        require(startCol == 0 || startCol == width - numDenseColumns)
        val physicalDest = logicalRowToPhysical[dest].toInt()
        val physicalSrc = logicalRowToPhysical[src].toInt()
        if (numDenseColumns > 0) {
            val (destWord, _) = bitPosition(physicalDest, 0)
            val (srcWord, _) = bitPosition(physicalSrc, 0)
            for (word in 0 until rowWordWidth()) {
                denseElements[destWord + word] = denseElements[destWord + word] xor denseElements[srcWord + word]
            }
        }

        if (startCol == 0) {
            val (destRow, tempRow) = sparseElements.bothIndices(physicalDest, physicalSrc)
            check(columnIndexDisabled || tempRow.size == 1)

            val columnAdded = destRow.addAssign(tempRow)
            check(columnIndexDisabled || !columnAdded)
        }
    }

    override fun resize(newHeight: Int, newWidth: Int) {
        require(newHeight <= height)
        var columnsToRemove = width - newWidth
        require(columnsToRemove == 0 || columnsToRemove >= numDenseColumns)
        if (!columnIndexDisabled) {
            TODO("Resize should only be used in phase 2, after column indexing is no longer needed")
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
            val newDense = ULongArray(newHeight * rowWordWidth())
            for (logicalRow in 0 until rowWordWidth()) {
                val physicalRow = logicalRowToPhysical[logicalRow].toInt()
                for (word in 0 until rowWordWidth()) {
                    newDense[logicalRow * rowWordWidth() + word] = denseElements[physicalRow * rowWordWidth() + word]
                }
            }
        } else {
            columnsToRemove -= numDenseColumns
            denseElements = ULongArray(0)
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
}
