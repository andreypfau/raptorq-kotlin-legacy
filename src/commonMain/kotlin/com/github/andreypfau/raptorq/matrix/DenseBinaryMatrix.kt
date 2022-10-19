@file:Suppress("OPT_IN_USAGE")

package com.github.andreypfau.raptorq.matrix


class DenseBinaryMatrix(
    val height: Int,
    val width: Int,
    val elements: ULongArray
) {
    fun bitPosition(i: Int, j: Int): Pair<Int, Int> {
        val word = i * runWordWidth() + j / WORD_WIDTH
        val bit = j % WORD_WIDTH
        return Pair(word, bit)
    }

    fun runWordWidth(): Int {
        return (width + WORD_WIDTH - 1) / WORD_WIDTH
    }

    operator fun set(i: Int, j: Int, value: UByte) {
        val (word, bit) = bitPosition(i, j)
        if (value.toInt() == 0) {
            clearBit(elements[word], bit)
        } else {
            setBit(elements[word], bit)
        }
    }

    fun sizeInBytes(): Int = elements.size * Long.SIZE_BYTES

    fun countOnes(row: Int, startCol: Int, endCol: Int): Int {
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

    companion object {
        private const val WORD_WIDTH = 64
        private const val WORD_WIDTH_LOG2 = 6
        private const val WORD_WIDTH_MASK = 0x3F

        fun wordOffset(col: Int): Int = col / WORD_WIDTH

        fun selectMask(bit: Int): ULong = 1uL shl bit

        fun selectBitAndAllLeftMask(bit: Int): ULong = selectAllRightOfMask(bit).inv()

        fun selectAllRightOfMask(bit: Int): ULong = selectMask(bit) - 1uL

        fun clearBit(word: ULong, bit: Int): ULong = word and selectMask(bit).inv()

        fun setBit(word: ULong, bit: Int): ULong = word or selectMask(bit)
    }
}
