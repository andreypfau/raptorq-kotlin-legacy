package com.github.andreypfau.raptorq.data

fun interface SourceBlockSupplier<SB : Any> {
    fun get(off: Int, sbn: Int): SB
}
