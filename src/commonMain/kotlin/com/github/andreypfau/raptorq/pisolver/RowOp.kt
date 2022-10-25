package com.github.andreypfau.raptorq.pisolver

internal sealed class RowOp {
    data class AddAssign(
        val src: Int,
        val dest: Int
    ) : RowOp()

    data class Swap(
        val row1: Int,
        val row2: Int
    ) : RowOp()
}
