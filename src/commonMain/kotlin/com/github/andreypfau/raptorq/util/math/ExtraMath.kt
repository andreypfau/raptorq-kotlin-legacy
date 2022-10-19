package com.github.andreypfau.raptorq.util.math

import kotlin.math.abs

fun ceilDiv(num: Int, den: Int): Int =
    ((num + (den - 1L)) / den).toInt()

fun ceilDiv(num: Long, den: Long): Long =
    ((num.toULong() + (den.toULong() - 1UL)) / den.toULong()).toLong()

/**
 * **NOTE: Copied from `java.lang.Math` in Java 8.**
 *
 *
 * Returns the sum of its arguments, throwing an exception if the result overflows an `int`.
 *
 * @param x the first value
 * @param y the second value
 * @return the result
 * @throws ArithmeticException if the result overflows an int
 */
internal fun addExact(x: Int, y: Int): Int {
    val r = x + y
    // HD 2-12 Overflow iff both arguments have the opposite sign of the result
    if (x xor r and (y xor r) < 0) {
        throw ArithmeticException("integer overflow")
    }
    return r
}

/**
 * **NOTE: Copied from `java.lang.Math` in Java 8.**
 *
 *
 * Returns the sum of its arguments, throwing an exception if the result overflows a `long`.
 *
 * @param x the first value
 * @param y the second value
 * @return the result
 * @throws ArithmeticException if the result overflows a long
 */
internal fun addExact(x: Long, y: Long): Long {
    val r = x + y
    // HD 2-12 Overflow iff both arguments have the opposite sign of the result
    if (x xor r and (y xor r) < 0) {
        throw ArithmeticException("long overflow")
    }
    return r
}

/**
 * **NOTE: Copied from `java.lang.Math` in Java 8.**
 *
 *
 * Returns the product of the arguments,
 * throwing an exception if the result overflows an `int`.
 *
 * @param x the first value
 * @param y the second value
 * @return the result
 * @throws ArithmeticException if the result overflows an int
 */
internal fun multiplyExact(x: Int, y: Int): Int {
    val r = x.toLong() * y.toLong()
    if (r.toInt().toLong() != r) {
        throw ArithmeticException("integer overflow")
    }
    return r.toInt()
}

/**
 * **NOTE: Copied from `java.lang.Math` in Java 8.**
 *
 *
 * Returns the product of the arguments,
 * throwing an exception if the result overflows a `long`.
 *
 * @param x the first value
 * @param y the second value
 * @return the result
 * @throws ArithmeticException if the result overflows a long
 */
internal fun multiplyExact(x: Long, y: Long): Long {
    val r = x * y
    val ax: Long = abs(x)
    val ay: Long = abs(y)
    if (ax or ay ushr 31 != 0L) {
        // Some bits greater than 2^31 that might cause overflow
        // Check the result using the divide operator
        // and check for the special case of Long.MIN_VALUE * -1
        if (y != 0L && r / y != x || x == Long.MIN_VALUE && y == -1L) {
            throw ArithmeticException("long overflow")
        }
    }
    return r
}

/**
 * Returns the (modular in case of overflow) integer power.
 *
 * @param base The power base
 * @param exp  The power exponent
 * @return base^^exp
 * @throws IllegalArgumentException If the exponent is negative or if both base and exponent are equal to zero
 */
internal fun integerPow(base: Int, exp: Int): Int {
    var base = base
    var exp = exp
    require(exp >= 0) { "exponent must be non-negative" }
    if (base == 0) {
        return if (exp == 0) throw IllegalArgumentException("0^^0 is undefined") else 0
    }

    // exponentiation by squaring
    var result = 1
    while (exp != 0) {
        if (exp and 1 == 1) {
            result *= base
        }
        exp = exp shr 1
        base *= base
    }
    return result
}

/**
 * Returns the (modular in case of overflow) long integer power.
 *
 * @param base The power base
 * @param exp  The power exponent
 * @return base^^exp
 * @throws IllegalArgumentException If the exponent is negative or if both base and exponent are equal to zero
 */
internal fun integerPow(base: Long, exp: Long): Long {
    var base = base
    var exp = exp
    require(exp >= 0) { "exponent must be non-negative" }
    if (base == 0L) {
        return if (exp == 0L) throw IllegalArgumentException("0^^0 is undefined") else 0
    }

    // exponentiation by squaring
    var result: Long = 1
    while (exp != 0L) {
        if (exp and 1L == 1L) {
            result *= base
        }
        exp = exp shr 1
        base *= base
    }
    return result
}
