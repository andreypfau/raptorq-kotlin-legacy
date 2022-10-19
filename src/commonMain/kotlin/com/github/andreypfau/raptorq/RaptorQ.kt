package com.github.andreypfau.raptorq

import com.github.andreypfau.raptorq.encoder.array.ArrayDataEncoder
import com.github.andreypfau.raptorq.parameters.FECParameters
import kotlin.jvm.JvmStatic

object RaptorQ {
    @JvmStatic
    fun encoder(data: ByteArray, fecParameters: FECParameters): ArrayDataEncoder =
        encoder(data, 0, fecParameters)

    @JvmStatic
    fun encoder(data: ByteArray, offset: Int, fecParameters: FECParameters): ArrayDataEncoder =
        ArrayDataEncoder(data, offset, fecParameters)
}
