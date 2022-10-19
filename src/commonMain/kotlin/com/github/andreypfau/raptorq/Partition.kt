package com.github.andreypfau.raptorq

import com.github.andreypfau.raptorq.data.SourceBlockSupplier
import com.github.andreypfau.raptorq.data.SourceSymbolSupplier
import com.github.andreypfau.raptorq.parameters.FECParameters
import com.github.andreypfau.raptorq.util.math.ceilDiv
import kotlin.jvm.JvmInline
import kotlin.jvm.JvmStatic

@JvmInline
value class Partition(
    val array: IntArray
) {
    constructor(I: Int, J: Int) : this(
        intArrayOf(
            ceilDiv(I, J),
            I / J,
            I - (I / J) * J,
            J - (I - (I / J) * J)
        )
    )

    inline val IL: Int get() = array[0]
    inline val IS: Int get() = array[1]
    inline val JL: Int get() = array[2]
    inline val JS: Int get() = array[3]

    operator fun component1() = array[0]
    operator fun component2() = array[1]
    operator fun component3() = array[2]
    operator fun component4() = array[3]

    companion object {
        @JvmStatic
        fun <SB : Any> partitionSourceData(
            fecParameters: FECParameters,
            startOffset: Int = 0,
            supplier: SourceBlockSupplier<SB>
        ): List<SB> {
            val Kt = fecParameters.totalSymbols
            val Z = fecParameters.numberOfSourceBlocks

            // (KL, KS, ZL, ZS) = Partition[Kt, Z]
            val (KL, KS, ZL, _) = Partition(Kt, Z)
            val srcBlocks = ArrayList<SB>(Z)

            val T = fecParameters.symbolSize
            var sbn = 0
            var off = startOffset
            for (i in 0 until ZL) {
                val sb = supplier.get(off, sbn)
                srcBlocks.add(sb)
                sbn++
                off += KL * T
            }
            for (i in sbn until Z) {
                val sb = supplier.get(off, sbn)
                srcBlocks.add(sb)
                sbn++
                off += KS * T
            }
            return srcBlocks
        }

        @JvmStatic
        fun <SS : Any> partitionSourceBlock(
            sbn: Int,
            fecParameters: FECParameters,
            startOffset: Int,
            supplier: SourceSymbolSupplier<SS>
        ): List<SS> {
            // number of source symbols
            val K = fecParameters.getK(sbn)

            val srcSymbols = ArrayList<SS>(K)

            val T = fecParameters.symbolSize
            var off = startOffset
            for (esi in 0 until K) {
                val ss = supplier.get(off, esi, T)
                srcSymbols.add(ss)
                off += T
            }

            return srcSymbols
        }
    }
}
