package com.github.andreypfau.raptorq.symbol

import com.github.andreypfau.raptorq.octet.Octet
import com.github.andreypfau.raptorq.utils.bothIndices

sealed interface SymbolOps {

    operator fun invoke(symbols: Array<Symbol>)

    data class AddAssign(
        val dest: Int,
        val src: Int
    ) : SymbolOps {
        override fun invoke(symbols: Array<Symbol>) {
            val (dest, tmp) = symbols.bothIndices(dest, src)
            dest += tmp
        }
    }

    data class MulAssign(
        val dest: Int,
        val scalar: Octet
    ) : SymbolOps {
        override fun invoke(symbols: Array<Symbol>) {
            TODO()
//            symbols[dest] *= scalar
        }
    }

    data class Fma(
        val dest: Int,
        val src: Int,
        val scalar: Octet
    ) : SymbolOps {
        override fun invoke(symbols: Array<Symbol>) {
            TODO()
//            val (dest, tmp) = symbols.bothIndices(dest, src)
//            dest += tmp * scalar
        }
    }

    data class Reorder(
        val order: List<Int>
    ) : SymbolOps {
        override fun invoke(symbols: Array<Symbol>) {
            val tmp = symbols.copyOf()
            order.forEachIndexed { index, i ->
                symbols[index] = tmp[i]
            }
        }
    }
}
