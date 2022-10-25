package com.github.andreypfau.raptorq.pisolver

import com.github.andreypfau.raptorq.octet.Octet
import com.github.andreypfau.raptorq.octet.asOctet
import com.github.andreypfau.raptorq.utils.swap

// First phase (section 5.4.2.2)
//
// Returns the row operations required to convert the X matrix into the identity
internal fun IntermediateSymbolDecoder<*>.firstPhase(): List<RowOp>? {
    // First phase (section 5.4.2.2)

    //    ----------> i                 u <--------
    //  | +-----------+-----------------+---------+
    //  | |           |                 |         |
    //  | |     I     |    All Zeros    |         |
    //  v |           |                 |         |
    //  i +-----------+-----------------+    U    |
    //    |           |                 |         |
    //    |           |                 |         |
    //    | All Zeros |       V         |         |
    //    |           |                 |         |
    //    |           |                 |         |
    //    +-----------+-----------------+---------+
    // Figure 6: Submatrices of A in the First Phase

    val aHdpcRows = requireNotNull(aHdpcRows)
    val numHdpcRows = aHdpcRows.height

    val selectionHelper = FirstPhaseRowSelectionStats(
        matrix = A,
        endCol = A.width - u,
        endRow = A.height - numHdpcRows
    )

    // Record of first phase row operations performed on non-HDPC rows
    val rowOps = ArrayList<RowOp>()

    while (i + u < L) {
        // Calculate r
        // "Let r be the minimum integer such that at least one row of A has
        // exactly r nonzeros in V."
        // Exclude the HDPC rows, since Errata 2 guarantees they won't be chosen.
        val (chosenRow, r) = selectionHelper.firstPhaseSelection(
            startRow = i,
            endRow = A.width - numHdpcRows,
            matrix = A
        )
        if (r == null || chosenRow == null) return null
        check(chosenRow >= i)

        // See paragraph beginning: "After the row is chosen in this step..."
        // Reorder rows
        var temp = i
        swapRows(temp, chosenRow)
        if (debug) {
            X?.swapRows(temp, chosenRow)
        }
        rowOps.add(
            RowOp.Swap(
                row1 = temp,
                row2 = chosenRow
            )
        )
        selectionHelper.swapRows(temp, chosenRow)
        // Reorder columns
        firstPhaseSwapColumnSubstep(r, selectionHelper)
        // Zero out leading value in following rows
        temp = i
        // self.i will never reference an HDPC row, so can ignore self.A_hdpc_rows
        // because of Errata 2.
        val tempValue = A[temp, temp]

        val onesInColumn = A.onesInColumn(temp, i + 1, A.height - numHdpcRows).toList()
        selectionHelper.resize(
            startRow = i + 1,
            endRow = A.height - aHdpcRows.height,
            startCol = i + 1,
            endCol = A.width - u - (r - 1),
            onesInStartCol = onesInColumn,
            matrix = A
        )
        for (i in 0 until (r - 1)) {
            A.hintColumnDenseAndFrozen(A.width - u - 1 - i)
        }

        // Skip the first element since that's the i'th row
        for (row in onesInColumn) {
            require(tempValue)
            if (debug) {
                fmaRows(temp, row, Octet.ONE, 0)
            } else {
                fmaRows(temp, row, Octet.ONE, A.width - (u + (r - 1)))
            }
            rowOps.add(
                RowOp.AddAssign(
                    src = temp,
                    dest = row
                )
            )
            if (r == 1) {
                // No need to update the selection helper, since we already resized it to remove
                // the first column
            } else {
                selectionHelper.recomputeRow(row, A)
            }
        }

        val piOctets = A.subRowAsOctets(temp, A.width - (u + (r - 1)))

        for (row in 0 until numHdpcRows) {
            val leadingValue = aHdpcRows[row, temp]
            if (leadingValue != Octet.ZERO) {
                // Addition is equivalent to subtraction
                val beta = leadingValue / tempValue.asOctet()
                fmaRowsWithPi(
                    i = temp,
                    iPrime = row + (A.height - numHdpcRows),
                    beta = beta,
                    // self.i is the only non-PI column which can have a nonzero,
                    // since all the rest were column swapped into the PI submatrix.
                    onluNonPiNonZeroColumn = temp,
                    piOctets = piOctets,
                    startCol = 0
                )
                // It's safe to skip updating the selection helper, since it will never
                // select an HDPC row
            }
        }

        i += 1
        u += r - 1

        if (debug) {
            firstPhaseVerify()
        }
    }

    recordSymbolOps(0)

    val mapping = IntArray(A.height) { it }
    return rowOps.asReversed()
        .asSequence()
        .map {
            when (it) {
                is RowOp.AddAssign -> {
                    val (src, dest) = it
                    require(mapping[src] < i)
                    if (mapping[dest] < i) {
                        RowOp.AddAssign(
                            src = mapping[src],
                            dest = mapping[dest]
                        )
                    } else null
                }

                is RowOp.Swap -> {
                    val (row1, row2) = it
                    mapping.swap(row1, row2)
                    null
                }
            }
        }.filterNotNull().toList().reversed()
}

private fun IntermediateSymbolDecoder<*>.firstPhaseSwapColumnSubstep(
    r: Int,
    selectionHelper: FirstPhaseRowSelectionStats
) {
    // Fast path when r == 1, since this is very common
    if (r == 1) {
        // self.i will never reference an HDPC row, so can ignore self.A_hdpc_rows
        // because of Errata 2.
        val (col, _) = A.rowIterator(i, i, A.width - u)
            .asSequence()
            .first { (_, value) -> value }
        // No need to swap the first i rows, as they are all zero (see submatrix above V)
        swapColumns(i, col, i)
        selectionHelper.swapColumns(i, col)
        if (debug) {
            X?.swapColumns(i, col, 0)
        }
        return
    }

    var remainingSwaps = r
    var foundFirst = A[i, i]
    // self.i will never reference an HDPC row, so can ignore self.A_hdpc_rows
    // because of Errata 2.
    for ((col, value) in A.rowIterator(i, i, A.width - u).clone()) {
        if (!value) continue
        if (col >= A.width - u - (r - 1)) {
            // Skip the column, if it's one of the trailing columns that shouldn't move
            remainingSwaps--
            continue
        }
        if (col == i) {
            // Skip the column, if it's already in the first position
            remainingSwaps--
            foundFirst = true
            continue
        }
        var dest: Int
        if (!foundFirst) {
            dest = i
            foundFirst = true
        } else {
            dest = A.width - u - 1
            // Some of the right most columns may already contain non-zeros
            while (A[i, dest]) {
                dest--
            }
        }
        // No need to swap the first i rows, as they are all zero (see submatrix above V)
        swapColumns(dest, col, i)
        selectionHelper.swapColumns(dest, col)
        if (debug) {
            X?.swapColumns(dest, col, 0)
        }
        remainingSwaps--
        if (remainingSwaps == 0) break
    }
    check(remainingSwaps == 0)
}

// See section 5.4.2.2. Verifies the two all-zeros submatrices and the identity submatrix
internal fun IntermediateSymbolDecoder<*>.firstPhaseVerify() {
    for (row in 0 until i) {
        for (col in 0 until i) {
            if (row == col) {
                check(A[row, col])
            } else {
                check(!A[row, col])
            }
        }
    }
    check(allZeros(0, i, i, A.width - u))
    check(allZeros(i, A.height, 0, i))
}
