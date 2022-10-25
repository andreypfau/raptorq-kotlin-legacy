package com.github.andreypfau.raptorq.pisolver

import com.github.andreypfau.raptorq.octet.Octet
import com.github.andreypfau.raptorq.octet.asOctet

fun IntermediateSymbolDecoder<*>.getAvalue(
    row: Int,
    col: Int
): Octet {
    val hdpc = aHdpcRows
    if (hdpc != null) {
        if (row >= A.height - hdpc.height) {
            return hdpc[row - (A.height - hdpc.height), col]
        }
    }
    return A[row, col].asOctet()
}

fun IntermediateSymbolDecoder<*>.allZeros(
    startRow: Int,
    endRow: Int,
    startColumn: Int,
    endColumn: Int
): Boolean {
    for (row in startRow until endRow) {
        for (col in startColumn until endColumn) {
            if (getAvalue(row, col) != Octet.ZERO) {
                return false
            }
        }
    }
    return true
}
