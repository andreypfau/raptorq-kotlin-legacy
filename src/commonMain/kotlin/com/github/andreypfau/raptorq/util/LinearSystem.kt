package com.github.andreypfau.raptorq.util

import com.github.andreypfau.raptorq.util.linear.factory.Basic2DFactory
import com.github.andreypfau.raptorq.util.linear.matrix.ByteMatrix
import com.github.andreypfau.raptorq.util.math.OctetOps

object LinearSystem {
    // there is no benefit for a dense matrix in all values of K
    private const val A_SPARSE_THRESHOLD = 0L
    private const val MT_SPARSE_THRESHOLD = 0L

    private val DENSE_FACTORY = Basic2DFactory
    private val SPARSE_FACTORY = CRS_FACTORY

    internal fun enc(Kprime: Int, C: Array<ByteArray>, tuple: Tuple, T: Int): ByteArray {
        // necessary parameters
        val Ki = SystematicIndices.getKIndex(Kprime)
        val S = SystematicIndices.S(Ki)
        val H = SystematicIndices.H(Ki)
        val W = SystematicIndices.W(Ki)
        val L = (Kprime + S + H).toLong()
        val P = L - W
        val P1 = MatrixUtilities.ceilPrime(P).toInt()
        val d = tuple.d
        val a = tuple.a

        var b = tuple.b

        val d1 = tuple.d1
        val a1 = tuple.a1

        var b1 = tuple.b1

        // allocate memory and initialize the encoding symbol
        val result = C[b.toInt()].copyOf(T)

        /*
         * encoding -- refer to section 5.3.5.3 of RFC 6330
         */

        for (j in 1 until d) {
            b = (b + a) % W
            OctetOps.vectorVectorAddition(C[b.toInt()], result, result)
        }

        while (b1 >= P) b1 = (b1 + a1) % P1

        OctetOps.vectorVectorAddition(C[(W + b1).toInt()], result, result)

        for (j in 1 until d1) {
            do b1 = (b1 + a1) % P1 while (b1 >= P)
            OctetOps.vectorVectorAddition(C[(W + b1).toInt()], result, result)
        }

        return result
    }

    fun generateConstraintMatrix(kPrime: Int, overheadRows: Int = 0): ByteMatrix {
        // calculate necessary parameters
        val Ki = SystematicIndices.getKIndex(kPrime)
        val S = SystematicIndices.S(Ki)
        val H = SystematicIndices.H(Ki)
        val W = SystematicIndices.W(Ki)
        val L = kPrime + S + H
        val P = L - W
        val U = P - H
        val B = W - S

        // allocate memory for the constraint matrix

        // allocate memory for the constraint matrix
        val A: ByteMatrix = getMatrixAfactory(L, overheadRows).createMatrix(L + overheadRows, L)


        TODO("Not yet implemented")
    }

    private fun getMatrixAfactory(L: Int, overheadRows: Int): Any {
        return if (L.toLong() * (L + overheadRows) < LinearSystem.A_SPARSE_THRESHOLD) {
            LinearSystem.DENSE_FACTORY
        } else {
            LinearSystem.SPARSE_FACTORY
        }
    }
}
