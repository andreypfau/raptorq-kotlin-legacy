package com.github.andreypfau.raptorq.packet

import com.github.andreypfau.raptorq.parameters.FECParameters
import io.ktor.utils.io.bits.*

abstract class AbstractEncodingPacket(
    val sourceBlockNumber: Int,
    val encodingSymbolId: Int,
    val symbols: Memory,
    val numSymbols: Int
) : EncodingPacket {
    val symbolsSize: Int get() = symbols.size32
    val fecPayloadId: Int get() = FECParameters.getFecPayloadId(sourceBlockNumber, encodingSymbolId)

    fun toByteArray(): ByteArray {
        val byteArray = ByteArray(Int.SIZE_BYTES * 2 + symbolsSize)
        toByteArray(byteArray)
        return byteArray
    }

    fun toByteArray(output: ByteArray, offset: Int = 0): Int {
        output.useMemory(offset, Int.SIZE_BYTES * 2 + symbolsSize) {
            it.storeIntAt(0, sourceBlockNumber)
            it.storeIntAt(Int.SIZE_BYTES, sourceBlockNumber)
            val symbolsOffset = Int.SIZE_BYTES * 2
            symbols.copyTo(it, symbolsOffset, symbolsSize, symbolsOffset)
        }
        return Int.SIZE_BYTES * 2 + symbolsSize
    }
}

class SourcePacket(
    sourceBlockNumber: Int,
    encodingSymbolId: Int,
    symbols: Memory,
    numSymbols: Int
) : AbstractEncodingPacket(sourceBlockNumber, encodingSymbolId, symbols, numSymbols)

class RepairPacket(
    sourceBlockNumber: Int,
    encodingSymbolId: Int,
    symbols: Memory,
    numSymbols: Int
) : AbstractEncodingPacket(sourceBlockNumber, encodingSymbolId, symbols, numSymbols)
