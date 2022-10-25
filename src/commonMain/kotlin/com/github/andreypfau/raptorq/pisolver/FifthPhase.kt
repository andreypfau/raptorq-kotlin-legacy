package com.github.andreypfau.raptorq.pisolver

import com.github.andreypfau.raptorq.octet.Octet

// Fifth phase (section 5.4.2.6)
internal fun IntermediateSymbolDecoder<*>.fifthPhase(xEliminationOps: List<RowOp>) {
    for (op in xEliminationOps) {
        when (op) {
            is RowOp.AddAssign -> {
                val (src, dest) = op
                if (debug) {
                    fmaRows(src, dest, Octet.ONE, 0)
                } else {
                    // In release builds skip updating the A matrix, since it will never be read
                    recordFmaRows(src, dest, Octet.ONE)
                }
            }

            else -> {}
        }
    }

    recordSymbolOps(phase = 4)

    if (debug) {
        fifthPhaseVerify()
    }
}

internal fun IntermediateSymbolDecoder<*>.fifthPhaseVerify() {
    check(L == A.height)
    for (row in 0 until A.height) {
        check(L == A.width) {
            for (col in 0 until A.width) {
                if (row == col) {
                    check(A[row, col])
                } else {
                    check(!A[row, col])
                }
            }
        }
    }
}
