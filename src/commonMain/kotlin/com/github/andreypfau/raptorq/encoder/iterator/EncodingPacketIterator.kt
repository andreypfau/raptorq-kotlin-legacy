package com.github.andreypfau.raptorq.encoder.iterator

import com.github.andreypfau.raptorq.encoder.SourceBlockEncoder
import com.github.andreypfau.raptorq.packet.EncodingPacket

class EncodingPacketIterator(
    val encoder: SourceBlockEncoder,
    startingESI: Int,
    endingESI: Int
) : Iterator<EncodingPacket> {
    private val fence = endingESI + 1
    private var nextESI = startingESI

    override fun hasNext(): Boolean = nextESI < fence

    override fun next(): EncodingPacket = encoder.encodingPacket(nextESI++)
}
