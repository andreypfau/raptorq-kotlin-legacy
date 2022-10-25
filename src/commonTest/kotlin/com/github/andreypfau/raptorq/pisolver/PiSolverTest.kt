package com.github.andreypfau.raptorq.pisolver

import com.github.andreypfau.raptorq.core.*
import com.github.andreypfau.raptorq.matrix.DenseBinaryMatrix
import com.github.andreypfau.raptorq.symbol.Symbol
import kotlin.test.Test
import kotlin.test.assertTrue

class PiSolverTest {
    @Test
    fun operationsPerSymbol() {
        for ((elements, expectedMulOps, expectedAddOps) in listOf(
            OperationsPerSymbol(10, 35.0, 50.0),
            OperationsPerSymbol(100, 16.0, 35.0)
        )) {
            val numSymbols = extendedSourceBlockSymbols(elements)
            val indences = IntArray(numSymbols) { it }
            val (a, hdpc) = generateConstraintMatrix(numSymbols, indences, DenseBinaryMatrix)
            val symbols = Array(a.width) { Symbol.zero(1) }
            val decoder = IntermediateSymbolDecoder.create(a, hdpc, symbols, numSymbols, debug = true)
            decoder.invoke()
            assertTrue(
                decoder.symbolMulOps.toDouble() / numSymbols <= expectedMulOps,
                "mul ops per symbol = ${decoder.symbolMulOps.toDouble() / numSymbols}"
            )
            assertTrue(
                decoder.symbolAddOps.toDouble() / numSymbols <= expectedAddOps,
                "add ops per symbol = ${decoder.symbolAddOps.toDouble() / numSymbols}"
            )
        }
    }

    @Test
    fun checkErrata3() {
        for (i in 0..MAX_SOURCE_SYMBOLS_PER_BLOCK) {
            assertTrue(extendedSourceBlockSymbols(i) + numLdpcSymbols(i) >= numLtSymbols(i))
        }
    }
}

data class OperationsPerSymbol(
    val elemnts: Int,
    val expectedMulOps: Double,
    val expectedAddOps: Double
)
