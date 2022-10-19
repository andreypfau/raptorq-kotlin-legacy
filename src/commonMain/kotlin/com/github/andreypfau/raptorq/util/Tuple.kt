package com.github.andreypfau.raptorq.util

internal class Tuple(
    kPrime: Int,
    X: Long
) {
    val d: Long
    val a: Long
    val b: Long
    val d1: Long
    val a1: Long
    val b1: Long

    init {
        val Ki = SystematicIndices.getKIndex(kPrime)
        val S = SystematicIndices.S(Ki)
        val H = SystematicIndices.H(Ki)
        val W = SystematicIndices.W(Ki)
        val L = kPrime + S + H
        val J = SystematicIndices.J(Ki)
        val P = L - W
        val P1 = MatrixUtilities.ceilPrime(P.toLong())

        var A = 53591 + J * 997L
        if (A % 2 == 0L) A++

        val B = 10267L * (J + 1)

        val y = (B + X * A) % 4294967296L // 2^^32

        val v = Rand.rand(y, 0, 1048576L) // 2^^20

        d = Deg.deg(v, W.toLong())
        a = 1 + Rand.rand(y, 1, (W - 1).toLong())
        b = Rand.rand(y, 2, W.toLong())
        d1 = if (d < 4) 2 + Rand.rand(X, 3, 2L) else 2
        a1 = 1 + Rand.rand(X, 4, P1 - 1)
        b1 = Rand.rand(X, 5, P1)
    }
}
