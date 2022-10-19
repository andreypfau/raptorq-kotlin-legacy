package com.github.andreypfau.raptorq.matrix

import com.github.andreypfau.raptorq.iterators.OctetIterator
import com.github.andreypfau.raptorq.octet.BinaryOctetVec

interface BinaryMatrix {
    val height: Int
    val width: Int

    operator fun set(i: Int, j: Int, value: Boolean)

    operator fun get(i: Int, j: Int): Boolean

    fun countOnes(row: Int, startCol: Int, endCol: Int): Int

    fun rowIterator(row: Int, startCol: Int, endCol: Int): OctetIterator

    fun onesInColumn(col: Int, startRow: Int, endRow: Int): List<Int>

    fun subRowAsOctets(row: Int, startCol: Int): BinaryOctetVec

    fun nonZeroColumns(row: Int, startCol: Int): Sequence<Int>

    fun swapRows(i: Int, j: Int)

    fun swapColumns(i: Int, j: Int, startRowHint: Int)

    fun enableColumnAccessAcceleration()

    fun disableColumnAccessAcceleration()

    fun hintColumnDenseAndFrozen(i: Int)

    fun addAssignRows(dest: Int, src: Int, startCol: Int)

    fun resize(newHeight: Int, newWidth: Int)
}
