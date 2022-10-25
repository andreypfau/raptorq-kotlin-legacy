package com.github.andreypfau.raptorq.pisolver

import com.github.andreypfau.raptorq.octet.Octet

internal fun IntermediateSymbolDecoder<*>.fourthPhase() {
    for (i in 0 until i) {
        for (j in A.nonZeroColumns(i, this.i)) {
            if (debug) {
                fmaRows(j, i, Octet.ONE, 0)
            } else {
                // Skip applying to cols before i due to Errata 11
                fmaRows(j, i, Octet.ONE, i)
            }
        }
    }

    recordSymbolOps(phase = 3)
    if (debug) {
        fourthPhaseVerify()
    }
}

internal fun IntermediateSymbolDecoder<*>.fourthPhaseVerify() {
    //    ---------> i u <------
    //  | +-----------+--------+
    //  | |\          |        |
    //  | |  \ Zeros  | Zeros  |
    //  v |     \     |        |
    //  i |  X     \  |        |
    //  u +---------- +--------+
    //  ^ |           |        |
    //  | | All Zeros |   I    |
    //  | |           |        |
    //    +-----------+--------+
    // Same assertion about X being equal to the upper left of A
    thirdPhaseVerifyEnd()
    check(allZeros(0, i, A.width - u, A.width))
    check(allZeros(A.height - u, A.height, 0, i))
    for (row in (A.height - u) until A.height) {
        for (col in (A.width - u) until A.width) {
            if (row == col) {
                check(A[row, col])
            } else {
                check(!A[row, col])
            }
        }
    }
}
