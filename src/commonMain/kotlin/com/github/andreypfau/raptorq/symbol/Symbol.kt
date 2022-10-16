package com.github.andreypfau.raptorq.symbol

import com.github.andreypfau.raptorq.octet.addOctets
import kotlin.jvm.JvmInline

@JvmInline
value class Symbol(
    val value: ByteArray
) {
    operator fun plusAssign(other: Symbol) {
        addOctets(value, other.value, value)
    }

    companion object {
        fun zero(size: Int) = Symbol(ByteArray(size))
    }
}
