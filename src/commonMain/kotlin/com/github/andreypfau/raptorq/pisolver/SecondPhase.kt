package com.github.andreypfau.raptorq.pisolver

internal fun IntermediateSymbolDecoder<*>.secondPhase(xEliminationOps: List<RowOp>): Boolean {
    if (debug) {
        secondPhaseVerify(xEliminationOps)
        X!!.resize(i, i)
    }

    // Convert U_lower to row echelon form
    val temp = i
    val size = u
    // HDPC rows can be removed, since they can't have been selected for U_upper
    val hdpcRows = requireNotNull(aHdpcRows)
    aHdpcRows = null
    val subMatrix = recordReduceToRowEchelon(hdpcRows, temp, temp, size)
    if (subMatrix != null) {
        // Perform backwards elimination
        backwardsElimination(subMatrix, temp, temp, size)
    } else {
        return false
    }
    A.resize(L, L)
    recordSymbolOps(1)
    return true
}

// Verifies that X is lower triangular. See section 5.4.2.3
internal fun IntermediateSymbolDecoder<*>.secondPhaseVerify(xEliminationOps: List<RowOp>) {
    for (row in 0 until i) {
        for (col in (row + 1) until i) {
            check(!X!![row, col])
        }
    }

    // Also verify Errata 9
    val tempX = X!!.copy()
    for (op in xEliminationOps) {
        when (op) {
            is RowOp.AddAssign -> {
                val (src, dest) = op
                tempX.addAssignRows(dest, src, 0)
            }

            else -> {}
        }
    }
    for (row in 0 until i) {
        for (col in 0 until i) {
            if (row == col) {
                check(tempX[row, col])
            } else {
                check(!tempX[row, col])
            }
        }
    }
}
