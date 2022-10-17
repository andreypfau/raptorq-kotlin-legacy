package com.github.andreypfau.raptorq.matrix

import com.github.andreypfau.raptorq.iterators.OctetIterator
import com.github.andreypfau.raptorq.octet.BinaryOctetVec
import com.github.andreypfau.raptorq.octet.Octet

interface BinaryMatrix {
    val height: Int
    val width: Int

    operator fun set(i: Int, j: Int, value: Octet)

    operator fun get(i: Int, j: Int): Octet

    fun countOnes(row: Int, startCol: Int, endCol: Int): Int

    fun getRowIter(row: Int, startCol: Int, endCol: Int): OctetIterator

    fun getOnesInColumn(col: Int, startRow: Int, endRow: Int): List<Int>

    fun getSubRowAsOctets(row: Int, startCol: Int): BinaryOctetVec

    fun queryNonZeroColumns(row: Int, startCol: Int): List<Int>

    fun swapRows(i: Int, j: Int)

    fun swapColumns(i: Int, j: Int, startRowHint: Int)

    fun enableColumnAccessAcceleration()

    fun disableColumnAccessAcceleration()

    fun hintColumnDenseAndFrozen(i: Int)

    fun addAssignRows(dest: Int, src: Int, startCol: Int)

    fun resize(newHeight: Int, newWidth: Int)
}
