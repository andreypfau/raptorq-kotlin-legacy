package com.github.andreypfau.raptorq.symbol

import com.github.andreypfau.raptorq.octet.Octet
import com.github.andreypfau.raptorq.octet.addAssign
import com.github.andreypfau.raptorq.octet.fusedAddAssignMulScalar
import com.github.andreypfau.raptorq.octet.mulAssignScalar
import kotlin.jvm.JvmInline

@JvmInline
value class Symbol(
    val value: ByteArray
) {
    operator fun plusAssign(other: Symbol) {
        addAssign(value, other.value)
    }

    operator fun timesAssign(scalar: Octet) {
        mulAssignScalar(value, scalar)
    }

    fun fusedAddAssignMulScalar(other: Symbol, scalar: Octet) {
        fusedAddAssignMulScalar(value, other.value, scalar)
    }

    companion object {
        fun zero(size: Int) = Symbol(ByteArray(size))
    }
}
