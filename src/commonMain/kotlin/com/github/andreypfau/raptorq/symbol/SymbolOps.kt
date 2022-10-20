package com.github.andreypfau.raptorq.symbol

import com.github.andreypfau.raptorq.octet.Octet

sealed class SymbolOps {
    abstract operator fun invoke(symbols: Array<Symbol>)

    class AddAssign(
        val dest: Int,
        val src: Int
    ) : SymbolOps() {
        override fun invoke(symbols: Array<Symbol>) {
            val dest = symbols[dest]
            val src = symbols[src]
            dest += src
        }
    }

    class MulAssign(
        val dest: Int,
        val scalar: Octet
    ) : SymbolOps() {
        override fun invoke(symbols: Array<Symbol>) {
            val dest = symbols[dest]
            dest *= scalar
        }
    }

    class FMA(
        val dest: Int,
        val src: Int,
        val scalar: Octet
    ) : SymbolOps() {
        override fun invoke(symbols: Array<Symbol>) {
            val dest = symbols[dest]
            val src = symbols[src]
            dest.fusedAddAssignMulScalar(src, scalar)
        }
    }

    class Reorder(
        val order: IntArray
    ) {
        fun invoke(symbols: Array<Symbol>) {
            val newSymbols = Array(symbols.size) { Symbol.zero(symbols[0].value.size) }
            for (i in symbols.indices) {
                newSymbols[i] = symbols[order[i]]
            }
            for (i in symbols.indices) {
                symbols[i] = newSymbols[i]
            }
        }
    }
}
