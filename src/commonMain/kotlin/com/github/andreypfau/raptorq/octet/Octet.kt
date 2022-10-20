@file:Suppress("OPT_IN_USAGE")

package com.github.andreypfau.raptorq.octet

import kotlin.jvm.JvmInline

@JvmInline
value class Octet(
    val value: UByte
) {
    operator fun plus(other: Octet): Octet = (value xor other.value).asOctet()

    operator fun minus(other: Octet): Octet = (value xor other.value).asOctet()

    operator fun times(other: Octet): Octet {
        if (this != ZERO && other != ZERO) {
            val logU = OCTET_LOG[this.value.toInt()].toUByte()
            val logV = OCTET_LOG[other.value.toInt()].toUByte()
            return alpha(logU + logV)
        }
        return ZERO
    }

    operator fun div(other: Octet): Octet {
        if (this != ZERO) {
            if (other == ZERO) {
                throw ArithmeticException("Division by zero")
            }
            val logU = OCTET_LOG[this.value.toInt()].toUByte()
            val logV = OCTET_LOG[other.value.toInt()].toUByte()
            return alpha(255u + logU - logV)
        }
        return ZERO
    }

    fun fma(a: Octet, b: Octet): Octet {
        if (a != ZERO && b != ZERO) {
            val logU = OCTET_LOG[a.value.toInt()].toUByte()
            val logV = OCTET_LOG[b.value.toInt()].toUByte()
            return Octet(value xor OCTET_EXP[(logU + logV).toInt()].toUByte())
        }
        return this
    }

    fun toInt(): Int = value.toInt()
    fun toByte(): Byte = value.toByte()

    override fun toString(): String = "Octet { value: $value }"

    companion object {
        val ZERO = Octet(0u)
        val ONE = Octet(1u)

        fun alpha(i: Int): Octet = Octet(OCTET_EXP[i].toUByte())
    }
}

inline fun Octet.Companion.alpha(i: UInt): Octet = alpha(i.toInt())

inline fun UByte.asOctet(): Octet = Octet(this)
inline fun Byte.asOctet(): Octet = Octet(toUByte())
inline fun Int.asOctet(): Octet = toUByte().asOctet()
