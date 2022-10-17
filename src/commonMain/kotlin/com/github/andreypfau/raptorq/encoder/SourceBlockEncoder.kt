@file:Suppress("OPT_IN_USAGE")

package com.github.andreypfau.raptorq.encoder

import com.github.andreypfau.raptorq.core.*
import com.github.andreypfau.raptorq.symbol.Symbol

class SourceBlockEncoder(
    val sourceBlockId: UByte,
    val sourceSymbols: Array<Symbol>,
    val intermediateSymbols: Array<Symbol>
) {
    companion object {
        fun createSymbols(
            config: ObjectTransmissionInformation,
            data: ByteArray
        ): Array<Symbol> {
            require(data.size.toUShort() % config.symbolSize == 0u)
            if (config.numSubBlocks > 1u) {
                val symbols = List(data.size / config.symbolSize.toInt()) {
                    ArrayList<ByteArray>()
                }
                val (tl, ts, nl, ns) = RaptorQ.partition(
                    config.symbolSize / config.symbolAlignment,
                    config.numSubBlocks.toUInt(),
                )
                // Divide the block into sub-blocks and then concatenate the sub-symbols into symbols
                // See second to last paragraph in section 4.4.1.2.
                var offset = 0
                for (subBlock in 0 until (nl + ns).toInt()) {
                    val bytes = if (subBlock < nl.toInt()) {
                        tl.toInt() * config.symbolAlignment.toInt()
                    } else {
                        ts.toInt() * config.symbolAlignment.toInt()
                    }
                    for (symbol in symbols) {
                        symbol += data.copyOfRange(offset, offset + bytes)
                        offset += bytes
                    }
                }
                val flattenSymbols = symbols.flatten()
                return Array(flattenSymbols.size) {
                    Symbol(flattenSymbols[it])
                }
            } else {
                var offset = 0
                val symbolSize = config.symbolSize.toInt()
                return Array(data.size / symbolSize) {
                    val symbol = data.copyOfRange(offset, offset + symbolSize)
                    offset += symbolSize
                    Symbol(symbol)
                }
            }
        }

        fun createIntermediateSymbols(
            sourceBlock: Array<Symbol>,
            symbolSize: Int,
            sparseThreshold: Int
        ) {
            val extendedSourceSymbols = extendedSourceBlockSymbols(sourceBlock.size)
            val d = createD(sourceBlock, symbolSize, extendedSourceSymbols)
            val indices = 0 until extendedSourceSymbols
            if (extendedSourceSymbols >= sparseThreshold) {

            }
        }

        fun createD(
            sourceBlock: Array<Symbol>,
            symbolSize: Int,
            extendedSourceSymbols: Int
        ): Array<Symbol> {
            val l = numIntermediateSymbols(sourceBlock.size)
            val s = numLdpcSymbols(sourceBlock.size)
            val h = numHdpcSymbols(sourceBlock.size)
            val d = ArrayList<Symbol>(l)
            for (i in 0 until (s + h)) {
                d.add(Symbol(ByteArray(symbolSize)))
            }
            for (i in sourceBlock) {
                d.add(Symbol(i.value.copyOf()))
            }
            // Extend the source block with padding. See section 5.3.2
            for (i in 0 until (extendedSourceSymbols - sourceBlock.size)) {
                d.add(Symbol(ByteArray(symbolSize)))
            }
            check(d.size == l)
            return d.toTypedArray()
        }
    }
}
