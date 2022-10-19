package com.github.andreypfau.raptorq.encoder.array

import com.github.andreypfau.raptorq.Partition
import com.github.andreypfau.raptorq.encoder.DataEncoder
import com.github.andreypfau.raptorq.encoder.SourceBlockEncoder
import com.github.andreypfau.raptorq.parameters.FECParameters

/**
 * A RaptorQ encoder for an array data object.
 */
class ArrayDataEncoder(
    private val dataArray: ByteArray,
    private val dataOffset: Int,
    override val fecParameters: FECParameters
) : DataEncoder {
    init {
        require(dataArray.size in dataOffset..fecParameters.dataSize) { throw IndexOutOfBoundsException() }
    }

    val srcBlockEncoders = Partition.partitionSourceData(fecParameters, dataOffset) { off, sbn ->
        ArraySourceBlockEncoder(this, dataArray, off, fecParameters, sbn)
    }

    override val dataSize: Long get() = fecParameters.dataSize
    override val symbolSize: Int get() = fecParameters.symbolSize
    override val numberOfSourceBlocks: Int get() = fecParameters.numberOfSourceBlocks

    override fun sourceBlockEncoder(sbn: Int): ArraySourceBlockEncoder = srcBlockEncoders[sbn]

    override fun iterator(): Iterator<SourceBlockEncoder> = srcBlockEncoders.iterator()
}
