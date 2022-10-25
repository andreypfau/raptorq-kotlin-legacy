package com.github.andreypfau.raptorq.pisolver

import com.github.andreypfau.raptorq.octet.Octet

internal fun IntermediateSymbolDecoder<*>.thirdPhase(xEliminationOps: List<RowOp>) {
    if (debug) {
        thirdPhaseVerify()
    }

    // Perform A[0..i][..] = X * A[0..i][..] by applying Errata 10
    for (op in xEliminationOps.asReversed()) {
        when (op) {
            is RowOp.AddAssign -> {
                val (src, dest) = op
                if (debug) {
                    fmaRows(src, dest, Octet.ONE, 0)
                } else {
                    // Skip applying to cols before i due to Errata 11
                    fmaRows(src, dest, Octet.ONE, i)
                }
            }

            else -> {}
        }
    }

    recordSymbolOps(phase = 2)

    if (debug) {
        thirdPhaseVerifyEnd()
    }
}

internal fun IntermediateSymbolDecoder<*>.thirdPhaseVerify() {
    for (row in 0 until A.height) {
        for (col in 0 until A.width) {
            if (row < i && col >= A.width - u) {
                // element is in U_upper, which can have arbitrary values at this point
                continue
            }
            // The rest of A should be identity matrix
            if (row == col) {
                require(A[row, col])
            } else {
                require(!A[row, col])
            }
        }
    }
}

internal fun IntermediateSymbolDecoder<*>.thirdPhaseVerifyEnd() {
    if (X != null) {
        for (row in 0 until i) {
            for (col in 0 until i) {
                check(X[row, col] == A[row, col])
            }
        }
    }
}
