package com.github.andreypfau.raptorq.util

object MatrixUtilities {
    fun ceilPrime(p: Long): Long {
        var result = p
        if (result == 1L) result++
        while (!isPrime(result)) result++
        return result
    }

    fun isPrime(n: Long): Boolean {
        // check if n is a multiple of 2
        if (n % 2 == 0L) return false

        // if not, then just check the odds
        var i: Long = 3
        while (i * i <= n) {
            if (n % i == 0L) return false
            i += 2
        }
        return true
    }
}
