package com.github.andreypfau.raptorq.encoder

import com.github.andreypfau.raptorq.packet.EncodingPacket

interface SourceBlockEncoder {
    val numberOfSourceSymbols: Int

    fun <R> encodingPacket(esi: Int, packet: (EncodingPacket) -> R): R
    fun sourceIterator(): Iterator<EncodingPacket>
    fun repairIterator(numRepairPackets: Int): Iterator<EncodingPacket>
}
