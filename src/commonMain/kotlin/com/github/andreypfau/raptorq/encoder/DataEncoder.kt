package com.github.andreypfau.raptorq.encoder

import com.github.andreypfau.raptorq.parameters.FECParameters

interface DataEncoder : Iterable<SourceBlockEncoder> {
    val fecParameters: FECParameters

    val dataSize: Long
    val symbolSize: Int
    val numberOfSourceBlocks: Int

    fun sourceBlockEncoder(sbn: Int): SourceBlockEncoder
}
