@file:Suppress("OPT_IN_USAGE")

package com.github.andreypfau.raptorq.core

import com.github.andreypfau.raptorq.core.RaptorQ.rand
import com.github.andreypfau.raptorq.matrix.BinaryMatrix
import com.github.andreypfau.raptorq.matrix.DenseOctetMatrix
import com.github.andreypfau.raptorq.matrix.invoke
import com.github.andreypfau.raptorq.octet.Octet
import com.github.andreypfau.raptorq.octet.asOctet
import kotlin.experimental.xor

// Simulates Enc[] function to get indices of accessed intermediate symbols, as defined in section 5.3.5.3
internal fun encIndices(
    sourceTuple: UIntArray,
    ltSymbols: UInt,
    piSymbols: UInt,
    p1: UInt
): List<Int> {
    val w = ltSymbols
    val p = piSymbols
    val d = sourceTuple[0]
    val a = sourceTuple[1]
    var b = sourceTuple[2]
    val d1 = sourceTuple[3]
    val a1 = sourceTuple[4]
    var b1 = sourceTuple[5]

    require(d > 0u)
    require(a in 1u until w)
    require(b < w)
    require(d1 == 2u || d1 == 3u)
    require(a1 in 1u until p1)
    require(b1 < p1)

    val indices = ArrayList<Int>((d + d1).toInt())
    indices.add(b.toInt())

    for (i in 1u until d) {
        b = (b + a) % w
        indices.add(b.toInt())
    }

    while (b1 >= p) {
        b1 = (b1 + a1) % p1
    }

    indices.add((w + b1).toInt())

    for (i in 1u until d1) {
        b1 = (b1 + a1) % p1
        while (b1 >= p) {
            b1 = (b1 + a1) % p1
        }
        indices.add(((w + b1).toInt()))
    }

    return indices
}

internal fun generateHdpcRows(
    kPrime: Int,
    S: Int,
    H: Int
): DenseOctetMatrix {
    val matrix = DenseOctetMatrix(H, kPrime + S + H)
    // Compute G_HDPC using recursive formulation, since this is much faster than a
    // naive matrix multiplication approach

    val result = Array(H) { ByteArray(kPrime + S) }
    // Initialize the last column to alpha^i, which comes from multiplying the last column of MT
    // with the lower right 1 in GAMMA
    for (i in 0 until H) {
        result[i][kPrime + S - 1] = Octet.alpha(i).toByte()
    }

    // Now compute the rest of G_HDPC.
    // Note that for each row in GAMMA, i'th col = alpha * (i + 1)'th col
    // Therefore we can compute this right to left, by multiplying by alpha each time, and adding
    // the Rand() entries which will be associatively handled
    for (j in (kPrime + S - 2) downTo 0) {
        for (i in 0 until H) {
            result[i][j] = (Octet.alpha(1) * Octet(result[i][j + 1].toUByte())).toByte()
        }
        val rand6 = rand((j + 1).toUInt(), 6u, H.toUInt()).toInt()
        val rand7 = rand((j + 1).toUInt(), 7u, (H - 1).toUInt()).toInt()
        val i1 = rand6
        val i2 = (rand6 + rand7 + 1) % H
        result[i1][j] = result[i1][j] xor Octet.ONE.toByte()
        result[i2][j] = result[i2][j] xor Octet.ONE.toByte()
    }

    // Copy G_HDPC into matrix
    for (i in 0 until H) {
        for (j in 0 until kPrime + S) {
            if (result[i][j] != 0.toByte()) {
                matrix[i, j] = result[i][j].asOctet()
            }
        }
    }

    // I_H
    for (i in 0 until H) {
        matrix[i, kPrime + S + i] = Octet.ONE
    }

    return matrix
}

// See section 5.3.3.4.2
// Returns the HDPC rows separately. These logically replace the rows `S..(S + H)` of the constraint
// matrix. They are returned separately to allow easier optimizations.
fun <T : BinaryMatrix> generateConstraintMatrix(
    sourceBlockSymbols: Int,
    encodedSymbolIndices: IntArray,
    matrixFactory: BinaryMatrix.Factory<T>
): Pair<T, DenseOctetMatrix> {
    val kPrime = extendedSourceBlockSymbols(sourceBlockSymbols)
    val S = numLdpcSymbols(sourceBlockSymbols)
    val H = numHdpcSymbols(sourceBlockSymbols)
    val W = numLtSymbols(sourceBlockSymbols)
    val B = W - S
    val P = numPiSymbols(sourceBlockSymbols)
    val L = numIntermediateSymbols(sourceBlockSymbols)

    require(S + H + encodedSymbolIndices.size >= L)
    val matrix = matrixFactory(S + H + encodedSymbolIndices.size, L, P)

    // G_LDPC,1
    // See section 5.3.3.3
    for (i in 0 until B) {
        val a = 1 + i / S

        var b = i % S
        matrix[b, i] = true

        b = (b + a) % S
        matrix[b, i] = true

        b = (b + a) % S
        matrix[b, i] = true
    }

    // I_S
    for (i in 0 until S) {
        matrix[i, B + i] = true
    }

    // G_LDPC,2
    // See section 5.3.3.3
    for (i in 0 until S) {
        matrix[i, (i % P) + W] = true
        matrix[i, ((i + 1) % P) + W] = true
    }

    // G_ENC
    val ltSymbols = numLtSymbols(kPrime)
    val piSymbols = numPiSymbols(kPrime)
    val sysIndex = systematicIndex(kPrime)
    val p1 = calculateP1(kPrime)
    encodedSymbolIndices.forEachIndexed { row, i ->
        // row != i, because i is the ESI
        val tuple = RaptorQ.tuple(i.toUInt(), ltSymbols.toUInt(), sysIndex.toUInt(), p1.toUInt())

        for (j in encIndices(tuple, ltSymbols.toUInt(), piSymbols.toUInt(), p1.toUInt())) {
            matrix[row + S + H, j] = true
        }
    }

    return matrix to generateHdpcRows(kPrime, S, H)
}
