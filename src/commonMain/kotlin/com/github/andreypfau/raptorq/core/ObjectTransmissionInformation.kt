package com.github.andreypfau.raptorq.core

import kotlin.math.ceil
import kotlin.math.floor

// As defined in section 3.3.2 and 3.3.3
data class ObjectTransmissionInformation(
    val transferLength: ULong,
    val symbolSize: UShort,
    val numSourceBlocks: UByte,
    val numSubBlocks: UShort,
    val symbolAlignment: UByte
) {
    init {
        require(transferLength <= 942574504275u)
        require(symbolSize in 16u..65535u)
        require(numSourceBlocks in 1u..255u)
        require(numSubBlocks in 1u..65535u)
        require(symbolAlignment in 1u..255u)
    }

    companion object {
        fun generateEncodingParameters(
            transferLength: ULong,
            maxPacketSize: UShort,
            decoderMemoryRequirement: ULong = 10uL * 1024uL * 1024uL
        ): ObjectTransmissionInformation {
            val alignment = 8.toUShort()
            require(maxPacketSize >= alignment)
            val symbolSize = (maxPacketSize - (maxPacketSize % alignment)).toUShort()
            val subSymbolSize = 8u

            val kt = ceil(transferLength.toDouble() / symbolSize.toDouble())
            val nMax = floor(symbolSize.toDouble() / (subSymbolSize * alignment).toDouble()).toInt()

            fun kl(n: Int): Int {
                SYSTEMATIC_INDICES_AND_PARAMETERS.reversed().forEach { (kPrime, _, _, _, _) ->
                    val x = ceil(symbolSize.toDouble() / (alignment * n.toUInt()).toDouble())
                    if (kPrime <= (decoderMemoryRequirement.toDouble() / (alignment.toDouble() * x))) {
                        return kPrime
                    }
                }
                error("No k' found")
            }

            val numSourceBlocks = ceil(kt / kl(nMax).toDouble()).toInt()

            var n = 1
            for (i in 1..nMax) {
                n = i
                if (ceil(kt / numSourceBlocks.toDouble()).toInt() <= kl(n)) {
                    break
                }
            }

            return ObjectTransmissionInformation(
                transferLength,
                symbolSize,
                numSourceBlocks.toUByte(),
                n.toUShort(),
                alignment.toUByte()
            )
        }
    }
}
