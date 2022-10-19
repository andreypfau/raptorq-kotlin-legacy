package com.github.andreypfau.raptorq.encoder.iterator

import com.github.andreypfau.raptorq.encoder.SourceBlockEncoder
import com.github.andreypfau.raptorq.parameters.ParameterChecker

class EncodingPacketIteratorBuilder(
    var encoder: SourceBlockEncoder,
    startingESI: Int = 0,
    endingESI: Int = ParameterChecker.maxEncodingSymbolID(),
) {
    private var startingESI_ = startingESI
        set(value) {
            require(!ParameterChecker.isEncodingSymbolIDOutOfBounds(value))
            field = value
        }
    private var endingESI_ = endingESI
        set(value) {
            require(!ParameterChecker.isEncodingSymbolIDOutOfBounds(value))
            field = value
        }

    var startingESI: Int
        get() = startingESI_
        set(value) {
            startingESI_ = value
            if (endingESI_ < value) {
                endingESI_ = value
            }
        }

    var endingESI: Int
        get() = endingESI_
        set(value) {
            endingESI_ = value
            if (startingESI_ > value) {
                startingESI_ = value
            }
        }

    fun startAtInitialSourceSymbol() = apply {
        startingESI = 0
    }

    fun startAtInitalRepairSymbol() = apply {
        startingESI = encoder.numberOfSourceSymbols
    }

    fun endAtFinalSourceSymbol() = apply {
        endingESI = encoder.numberOfSourceSymbols - 1
    }

    fun build() = EncodingPacketIterator(encoder, startingESI, endingESI)
}
