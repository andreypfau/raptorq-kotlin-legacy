package com.github.andreypfau.raptorq.matrix

import com.github.andreypfau.raptorq.octet.*

class DenseOctetMatrix(
    val height: Int,
    val width: Int,
    private val elements: Array<ByteArray> = Array(height) { ByteArray(width) }
) {
    operator fun get(row: Int, col: Int): Octet = Octet(elements[row][col].toUByte())

    operator fun set(i: Int, j: Int, value: Octet) {
        elements[i][j] = value.value.toByte()
    }

    fun mulAssignRow(row: Int, value: Octet) {
        mulAssignScalar(elements[row], value)
    }

    fun swapRows(i: Int, j: Int) {
        val tmp = elements[i]
        elements[i] = elements[j]
        elements[j] = tmp
    }

    fun swapColumns(i: Int, j: Int, startRowHint: Int) {
        for (row in startRowHint until elements.size) {
            val tmp = elements[row][i]
            elements[row][i] = elements[row][j]
            elements[row][j] = tmp
        }
    }

    fun fmaSubRow(
        row: Int,
        startCol: Int,
        scalar: Octet,
        other: BinaryOctetVec
    ) {
        fusedAddAssignMulScalarBinary(
            elements[row],
            startCol,
            other.length,
            other,
            scalar
        )
    }

    fun fmaRows(dest: Int, multiplicand: Int, scalar: Octet) {
        require(dest != multiplicand)
        val destRow = elements[dest]
        val tempRow = elements[multiplicand]
        if (scalar == Octet.ONE) {
            addAssign(destRow, tempRow)
        } else {
            fusedAddAssignMulScalar(destRow, tempRow, scalar)
        }
    }
}
