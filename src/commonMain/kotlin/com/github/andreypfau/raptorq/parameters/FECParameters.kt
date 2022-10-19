package com.github.andreypfau.raptorq.parameters

import com.github.andreypfau.raptorq.Partition
import com.github.andreypfau.raptorq.util.math.ceilDiv

class FECParameters(
    val F: Long,
    val T: Int,
    val Z: Int,
    val N: Int,
    val Al: Int
) {
    val dataSize: Long get() = F
    val symbolSize: Int get() = T
    val numberOfSourceBlocks: Int get() = Z
    val interleaverSize: Int get() = N
    val symbolAlignment: Int get() = Al
    val totalSymbols: Int get() = getTotalSymbols(dataSize, symbolSize)

    fun getK(sbn: Int): Int {
        val Kt = totalSymbols
        val Z = numberOfSourceBlocks

        val (KL, KS, ZL, _) = Partition(Kt, Z)
        return if (sbn < ZL) {
            KL
        } else {
            KS
        }
    }

    companion object {
        private const val SOURCE_BLOCK_NUMBER_SHIFT = ESI_num_bytes * Byte.SIZE_BYTES

        fun getTotalSymbols(F: Long, T: Int): Int = ceilDiv(F, T.toLong()).toInt()

        fun getFecPayloadId(sbn: Int, esi: Int): Int = sbn shl SOURCE_BLOCK_NUMBER_SHIFT or esi
    }
}
