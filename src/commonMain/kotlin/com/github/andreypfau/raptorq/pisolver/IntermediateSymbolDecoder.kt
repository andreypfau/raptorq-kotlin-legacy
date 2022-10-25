package com.github.andreypfau.raptorq.pisolver

import com.github.andreypfau.raptorq.core.numHdpcSymbols
import com.github.andreypfau.raptorq.core.numIntermediateSymbols
import com.github.andreypfau.raptorq.core.numLdpcSymbols
import com.github.andreypfau.raptorq.core.numPiSymbols
import com.github.andreypfau.raptorq.matrix.BinaryMatrix
import com.github.andreypfau.raptorq.matrix.DenseOctetMatrix
import com.github.andreypfau.raptorq.octet.BinaryOctetVec
import com.github.andreypfau.raptorq.octet.Octet
import com.github.andreypfau.raptorq.octet.asOctet
import com.github.andreypfau.raptorq.symbol.Symbol
import com.github.andreypfau.raptorq.symbol.SymbolOps
import com.github.andreypfau.raptorq.utils.swap

class IntermediateSymbolDecoder<T : BinaryMatrix> private constructor(
    internal val A: T,
    internal var aHdpcRows: DenseOctetMatrix?,
    internal val D: Array<Symbol>,
    internal val c: IntArray,
    internal val d: IntArray,
    internal var i: Int,
    internal var u: Int,
    internal val L: Int,
    internal val deferredDops: MutableList<SymbolOps>,
    internal val numSourceBlocks: Int,
    internal val debug: Boolean = true,
    @Suppress("UNCHECKED_CAST")
    internal val X: T? = if (debug) A.copy().also {
        // Drop the PI symbols, since they will never be accessed in X. X will be resized to
        // i-by-i in the second phase.
        it.resize(it.height, it.width - numPiSymbols(numSourceBlocks))
    } as T else null
) {
    var symbolMulOps: Int = 0
        private set
    var symbolAddOps: Int = 0
        private set
    private val symbolMulOpsByPhase: IntArray = IntArray(5)
    private val symbolAddOpsByPhase: IntArray = IntArray(5)

    operator fun invoke(): Pair<List<Symbol>?, List<SymbolOps>?> {
        if (debug) {
            X?.disableColumnAccessAcceleration()
        }

        val xEliminationOps = firstPhase()
        if (xEliminationOps != null) {
            A.disableColumnAccessAcceleration()

            if (!secondPhase(xEliminationOps)) {
                return null to null
            }

            thirdPhase(xEliminationOps)
            fourthPhase()
            fifthPhase(xEliminationOps)
        } else {
            return null to null
        }
        applyDeferredSymbolOps()

        // See end of section 5.4.2.1
        val indexMapping = IntArray(L)
        for (i in 0 until L) {
            indexMapping[c[i]] = d[i]
        }

        val result = ArrayList<Symbol>(L)
        for (i in 0 until L) {
            result.add(D[indexMapping[i]])
        }
        val reorder = IntArray(L) { it }
        deferredDops + SymbolOps.Reorder(reorder)
        return result to deferredDops
    }

    private fun applyDeferredSymbolOps() {
        deferredDops.forEach { ops -> ops(D) }
    }

    internal fun recordSymbolOps(phase: Int) {
        symbolAddOpsByPhase[phase] = symbolAddOps
        symbolMulOpsByPhase[phase] = symbolMulOps
        for (i in 0 until phase) {
            symbolAddOpsByPhase[phase] -= symbolAddOpsByPhase[i]
            symbolMulOpsByPhase[phase] -= symbolMulOpsByPhase[i]
        }
    }

    // Reduces the size x size submatrix, starting at row_offset and col_offset as the upper left
    // corner, to row echelon form.
    // Returns the reduced submatrix, which should be written back into this submatrix of A.
    // The state of this submatrix in A is undefined, after calling this function.
    internal fun recordReduceToRowEchelon(
        hdpcRows: DenseOctetMatrix,
        rowOffset: Int,
        colOffset: Int,
        size: Int
    ): DenseOctetMatrix? {
        // Copy U_lower into a new matrix and merge it with the HDPC rows
        val subMatrix = DenseOctetMatrix(A.height - rowOffset, size)
        val firstHdpcRow = A.height - hdpcRows.height
        for (row in rowOffset until A.height) {
            for (col in colOffset until (colOffset + size)) {
                val value = if (row < firstHdpcRow) {
                    A[row, col].asOctet()
                } else {
                    hdpcRows[row - firstHdpcRow, col]
                }
                subMatrix[row - rowOffset, col - colOffset] = value
            }
        }

        for (i in 0 until size) {
            // Swap a row with leading coefficient i into place
            for (j in i until subMatrix.height) {
                if (subMatrix[j, i] != Octet.ZERO) {
                    subMatrix.swapRows(i, j)
                    // Record the swap, in addition to swapping in the working submatrix
                    // TODO: optimize to not perform op on A
                    swapRows(rowOffset + i, j + rowOffset)
                    break
                }
            }

            if (subMatrix[i, i] == Octet.ZERO) {
                // If all following rows are zero in this column, then matrix is singular
                return null
            }

            // Scale leading coefficient to 1
            if (subMatrix[i, i] != Octet.ONE) {
                val elementInverse = Octet.ONE / subMatrix[i, i]
                subMatrix.mulAssignRow(i, elementInverse)
                // Record the multiplication, in addition to multiplying the working submatrix
                recordMulRows(rowOffset + i, elementInverse)
            }

            // Zero out all following elements in i'th column
            for (j in (i + 1) until subMatrix.height) {
                if (subMatrix[j, i] != Octet.ZERO) {
                    val scalar = subMatrix[j, i]
                    subMatrix.fmaRows(j, i, scalar)
                    // Record the FMA, in addition to performing it on the working submatrix
                    recordFmaRows(rowOffset + i, rowOffset + j, scalar)
                }
            }
        }

        return subMatrix
    }

    // Performs backwards elimination in a size x size submatrix, starting at
    // row_offset and col_offset as the upper left corner of the submatrix
    //
    // Applies the submatrix to the size-by-size lower right of A, and performs backwards
    // elimination on it. "submatrix" must be in row echelon form.
    internal fun backwardsElimination(
        submatrix: DenseOctetMatrix,
        rowOffset: Int,
        colOffset: Int,
        size: Int
    ) {
        for (i in size - 1 downTo 0) {
            // Zero out all preceding elements in i'th column
            for (j in 0 until i) {
                val scalar = submatrix[j, i]
                if (scalar != Octet.ZERO) {
                    // Record the FMA. No need to actually apply it to the submatrix,
                    // since it will be discarded, and we never read these values
                    recordFmaRows(rowOffset + i, rowOffset + j, scalar)
                }
            }
        }

        // Write the identity matrix into A, since that's the resulting value of this function
        for (row in rowOffset until (rowOffset + size)) {
            for (col in colOffset until (colOffset + size)) {
                A[row, col] = row == col
            }
        }
    }

    // Record operation to apply operations to D.
    private fun recordMulRows(i: Int, beta: Octet) {
        symbolMulOps++
        deferredDops.add(
            SymbolOps.MulAssign(
                dest = d[i],
                scalar = beta
            )
        )
        require(aHdpcRows == null)
    }

    internal fun fmaRows(i: Int, iPrime: Int, beta: Octet, startCol: Int) {
        fmaRowsWithPi(i, iPrime, beta, null, null, startCol)
    }

    internal fun recordFmaRows(i: Int, iPrime: Int, beta: Octet) {
        symbolAddOps++
        if (beta == Octet.ONE) {
            deferredDops.add(
                SymbolOps.AddAssign(
                    dest = d[iPrime],
                    src = d[i]
                )
            )
        } else {
            symbolMulOps++
            deferredDops.add(
                SymbolOps.FMA(
                    dest = d[iPrime],
                    src = d[i],
                    scalar = beta
                )
            )
        }
    }

    internal fun fmaRowsWithPi(
        i: Int,
        iPrime: Int,
        beta: Octet,
        onluNonPiNonZeroColumn: Int?,
        piOctets: BinaryOctetVec?,
        startCol: Int
    ) {
        recordFmaRows(i, iPrime, beta)
        val hdpc = aHdpcRows
        if (hdpc != null) {
            val firstHdpcRow = A.height - hdpc.height
            // Adding HDPC rows to other rows isn't supported, since it should never happen
            require(i < firstHdpcRow)
            if (iPrime >= firstHdpcRow) {
                // Only update the V section of HDPC rows, for debugging. (they will never be read)
                if (debug) {
                    val col = onluNonPiNonZeroColumn!!
                    val multiplicand = A[i, col].asOctet()
                    var value = hdpc[iPrime - firstHdpcRow, col]
                    value = value.fma(multiplicand, beta)
                    hdpc[iPrime - firstHdpcRow, col] = value
                }

                // Handle this part separately, since it's in the dense U part of the matrix
                val octets = requireNotNull(piOctets)
                hdpc.fmaSubRow(
                    iPrime - firstHdpcRow,
                    A.width - octets.length,
                    beta,
                    octets
                )
            } else {
                require(beta == Octet.ONE)
                A.addAssignRows(iPrime, i, startCol)
            }
        } else {
            require(beta == Octet.ONE)
            A.addAssignRows(iPrime, i, startCol)
        }
    }

    internal fun swapRows(
        i: Int,
        iPrime: Int
    ) {
        val hdpcRows = aHdpcRows
        if (hdpcRows != null) {
            // Can't swap HDPC rows
            require(i < A.height - hdpcRows.height)
            require(iPrime < A.height - hdpcRows.height)
        }
        A.swapRows(i, iPrime)
        d.swap(i, iPrime)
    }

    internal fun swapColumns(
        j: Int,
        jPrime: Int,
        startRow: Int
    ) {
        A.swapColumns(j, jPrime, startRow)
        aHdpcRows?.swapColumns(j, jPrime, 0)
        c.swap(j, jPrime)
    }

    companion object {
        fun <T : BinaryMatrix> create(
            matrix: T,
            hdpcRows: DenseOctetMatrix,
            symbols: Array<Symbol>,
            numSourceBlocks: Int,
            intermediateSymbols: Int = numIntermediateSymbols(numSourceBlocks),
            debug: Boolean = true
        ): IntermediateSymbolDecoder<T> {
            require(matrix.width <= symbols.size)
            require(matrix.height == symbols.size)
            val temp = IntermediateSymbolDecoder(
                A = matrix.apply {
                    enableColumnAccessAcceleration()
                },
                aHdpcRows = null,
                D = symbols,
                c = IntArray(matrix.width) { it },
                d = IntArray(symbols.size) { it },
                i = 0,
                u = numPiSymbols(numSourceBlocks),
                L = intermediateSymbols,
                deferredDops = ArrayList(70 * intermediateSymbols),
                numSourceBlocks = numSourceBlocks,
                debug = debug
            )

            // Swap the HDPC rows, so that they're the last in the matrix
            val S = numLdpcSymbols(numSourceBlocks)
            val H = numHdpcSymbols(numSourceBlocks)
            // See section 5.3.3.4.2, Figure 5.
            for (i in 0 until H) {
                temp.swapRows(S + i, matrix.height - H + i)
                if (debug) {
                    temp.X!!.swapRows(S + i, matrix.height - H + i)
                }
            }
            temp.aHdpcRows = hdpcRows
            return temp
        }
    }
}
