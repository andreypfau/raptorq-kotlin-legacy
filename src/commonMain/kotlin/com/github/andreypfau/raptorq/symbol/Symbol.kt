package com.github.andreypfau.raptorq.symbol

import com.github.andreypfau.raptorq.octet.addAssign
import kotlin.jvm.JvmInline

@JvmInline
value class Symbol(
    val value: ByteArray
) {
    operator fun plusAssign(other: Symbol) {
        addAssign(value, other.value)
    }

    companion object {
        fun zero(size: Int) = Symbol(ByteArray(size))
    }
}
