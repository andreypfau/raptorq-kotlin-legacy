package com.github.andreypfau.raptorq.data

fun interface SourceSymbolSupplier<SS : Any> {
    fun get(off: Int, esi: Int, T: Int): SS
}
