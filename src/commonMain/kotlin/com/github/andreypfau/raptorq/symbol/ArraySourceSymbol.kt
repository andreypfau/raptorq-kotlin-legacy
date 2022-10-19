package com.github.andreypfau.raptorq.symbol

import io.ktor.utils.io.bits.*
import kotlin.math.min

class ArraySourceSymbol(
    val srcDataArray: ByteArray,
    val symbolOffset: Int,
    override val codeSize: Int,
    val transportSize: Int
) : SourceSymbol {
    constructor(
        srcDataArray: ByteArray,
        symbolOffset: Int,
        symbolSize: Int
    ) : this(
        srcDataArray,
        symbolOffset,
        symbolSize,
        min(symbolSize, srcDataArray.size - symbolOffset)
    )

    fun <R> transportBuf(memory: (Memory) -> R) = srcDataArray.useMemory(symbolOffset, transportSize, memory)

    override fun <R> transportData(memory: (Memory) -> R): R {
        return withMemory(transportSize) { dst ->
            transportBuf { src ->
                src.copyTo(dst, 0, src.size32, 0)
                memory(dst)
            }
        }
    }

    override fun <R> codeData(function: (Memory) -> R): R {
        return withMemory(codeSize) { dst ->
            dst.storeByteArray(0, srcDataArray, symbolOffset, transportSize)
            dst.fill(transportSize, codeSize - transportSize, 0)
            function(dst)
        }
    }
}
