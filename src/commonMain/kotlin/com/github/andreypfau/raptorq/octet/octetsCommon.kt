package com.github.andreypfau.raptorq.octet

import kotlin.experimental.xor

internal inline fun addOctetsCommon(
    octets: ByteArray,
    other: ByteArray,
    output: ByteArray = octets,
): ByteArray {
    require(octets.size == other.size)
    output.indices.forEach { index ->
        output[index] = octets[index] xor other[index]
    }
    return output
}
