@file:Suppress("OPT_IN_USAGE")

package com.github.andreypfau.raptorq.matrix

import com.github.andreypfau.raptorq.iterators.OctetIterator
import com.github.andreypfau.raptorq.octet.BinaryOctetVec
import com.github.andreypfau.raptorq.utils.addAssignBinary
import com.github.andreypfau.raptorq.utils.both

class DenseBinaryMatrix(
    height: Int,
    width: Int,
    elements: ULongArray = ULongArray(height * (width + WORD_WIDTH - 1) / WORD_WIDTH)
) : BinaryMatrix {
    override var height: Int = height
        private set
    override var width: Int = width
        private set
    private var elements: ULongArray = elements

    val rowWordWidth get() = (width + WORD_WIDTH - 1) / WORD_WIDTH

    fun bitPosition(row: Int, col: Int): Pair<Int, Int> {
        val word = row * rowWordWidth + wordOffset(col)
        val bit = col % WORD_WIDTH
        return Pair(word, bit)
    }

    override operator fun set(i: Int, j: Int, value: Boolean) {
        val (word, bit) = bitPosition(i, j)
        if (value) {
            setBit(elements[word], bit)
        } else {
            clearBit(elements[word], bit)
        }
    }

    override fun countOnes(row: Int, startCol: Int, endCol: Int): Int {
        val (startWord, startBit) = bitPosition(row, startCol)
        val (endWord, endBit) = bitPosition(row, endCol)
        // Handle case when there is only one word
        if (startWord == endWord) {
            var mask = selectBitAndAllLeftMask(startBit)
            mask = mask and selectAllRightOfMask(endBit)
            val bits = elements[startWord] and mask
            return bits.countOneBits()
        }

        val firstWordMask = elements[startWord] and selectBitAndAllLeftMask(startBit)
        var ones = firstWordMask.countOneBits()
        for (word in startWord + 1 until endWord) {
            ones += elements[word].countOneBits()
        }
        if (endBit > 0) {
            val bits = elements[endWord] and selectAllRightOfMask(endBit)
            ones += bits.countOneBits()
        }
        return ones
    }

    override fun rowIterator(row: Int, startCol: Int, endCol: Int): OctetIterator {
        val (firstWord, firstBit) = bitPosition(row, startCol)
        val (lastWord, _) = bitPosition(row, endCol)
        return OctetIterator.denseBinary(
            startCol,
            endCol,
            firstBit,
            elements.copyOfRange(firstWord, lastWord + 1)
        )
    }

    override fun onesInColumn(col: Int, startRow: Int, endRow: Int): List<Int> {
        val rows = ArrayList<Int>()
        for (row in startRow until endRow) {
            if (get(row, col)) {
                rows.add(row)
            }
        }
        return rows
    }

    override fun subRowAsOctets(row: Int, startCol: Int): BinaryOctetVec {
        val result = ULongArray((width - startCol + BinaryOctetVec.WORD_WIDTH - 1) / BinaryOctetVec.WORD_WIDTH)
        var word = result.size
        var bit = 0
        for (col in width downTo startCol) {
            if (bit == 0) {
                bit = BinaryOctetVec.WORD_WIDTH - 1
                word--
            } else {
                bit--
            }
            if (get(row, col)) {
                result[word] = result[word] or BinaryOctetVec.selectMask(bit)
            }
        }
        return BinaryOctetVec(result, width - startCol)
    }

    override fun nonZeroColumns(row: Int, startCol: Int): Sequence<Int> =
        (startCol until width).asSequence()
            .filter { col -> get(row, col) }

    override operator fun get(i: Int, j: Int): Boolean {
        val (word, bit) = bitPosition(i, j)
        return elements[word] and selectMask(bit) != 0uL
    }

    override fun swapRows(i: Int, j: Int) {
        val (rowI, _) = bitPosition(i, 0)
        val (rowJ, _) = bitPosition(j, 0)
        for (word in 0 until rowWordWidth) {
            val tmp = elements[rowI + word]
            elements[rowI + word] = elements[rowJ + word]
            elements[rowJ + word] = tmp
        }
    }

    override fun swapColumns(i: Int, j: Int, startRowHint: Int) {
        val (wordI, tmpBitI) = bitPosition(0, i)
        val (wordJ, tmpBitJ) = bitPosition(0, j)
        val unsetI = selectMask(tmpBitI).inv()
        val unsetJ = selectMask(tmpBitJ).inv()
        val bitI = selectMask(tmpBitI)
        val bitJ = selectMask(tmpBitJ)
        val rowWidth = rowWordWidth
        for (row in startRowHint until height) {
            val iSet = elements[row * rowWidth + wordI] and bitI != 0uL
            if (elements[row * rowWidth + wordJ] and bitJ == 0uL) {
                elements[row * rowWidth + wordI] = elements[row * rowWidth + wordI] and unsetI
            } else {
                elements[row * rowWidth + wordI] = elements[row * rowWidth + wordI] or bitI
            }
            if (iSet) {
                elements[row * rowWidth + wordJ] = elements[row * rowWidth + wordJ] or bitJ
            } else {
                elements[row * rowWidth + wordJ] = elements[row * rowWidth + wordJ] and unsetJ
            }
        }
    }

    override fun enableColumnAccessAcceleration() {
    }

    override fun disableColumnAccessAcceleration() {
    }

    override fun hintColumnDenseAndFrozen(i: Int) {
    }

    override fun addAssignRows(dest: Int, src: Int, startCol: Int) {
        require(dest != src)
        val (destWord, _) = bitPosition(dest, 0)
        val (srcWord, _) = bitPosition(src, 0)
        val rowWidth = rowWordWidth
        val (destRow, tempRow) = elements.both(destWord, srcWord, rowWidth)
        addAssignBinary(destRow, tempRow)
    }

    override fun resize(newHeight: Int, newWidth: Int) {
        require(newHeight <= height)
        require(newWidth <= width)
        val oldRowWidth = rowWordWidth
        height = newHeight
        width = width
        val newRowWidth = rowWordWidth
        val wordsToRemove = oldRowWidth - newRowWidth
        if (wordsToRemove > 0) {
            var src = 0
            var dest = 0
            while (dest < newHeight * newRowWidth) {
                elements[dest++] = elements[src++]
                if (dest % newRowWidth == 0) {
                    // After copying each row, skip over the elements being dropped
                    src += wordsToRemove
                }
            }
            check(src == newHeight * oldRowWidth)
        }
        elements = elements.copyOf(newHeight * newRowWidth)
    }

    companion object {
        const val WORD_WIDTH = 64
        const val WORD_WIDTH_LOG2 = 6
        const val WORD_WIDTH_MASK = 0x3F

        inline fun wordOffset(col: Int): Int = col / WORD_WIDTH

        inline fun selectMask(bit: Int): ULong = 1uL shl bit

        inline fun selectBitAndAllLeftMask(bit: Int): ULong = selectAllRightOfMask(bit).inv()

        inline fun selectAllRightOfMask(bit: Int): ULong = selectMask(bit) - 1uL

        inline fun clearBit(word: ULong, bit: Int): ULong = word and selectMask(bit).inv()

        inline fun setBit(word: ULong, bit: Int): ULong = word or selectMask(bit)
    }
}
