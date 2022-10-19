@file:Suppress("OPT_IN_USAGE")

package com.github.andreypfau.raptorq.octet

import kotlin.experimental.xor

internal inline fun fusedAddAssignMulScalarBinaryCommon(
    octets: ByteArray,
    other: BinaryOctetVec,
    scalar: Octet
) {
    require(octets.size == other.length)
    if (scalar == Octet.ONE) {
        addAssign(octets, other.toOctetVec())
    } else {
        fusedAddAssignMulScalar(octets, other.toOctetVec(), scalar)
    }
}

internal inline fun mulAssignScalarCommon(
    octets: ByteArray,
    scalar: Octet
) {
    val scalarIndex = scalar.value.toInt()
    for (i in octets.indices) {
        val octetIndex = octets[i].toUByte().toInt()
        octets[i] = OCTET_MUL[scalarIndex][octetIndex].toByte()
    }
}

internal inline fun fusedAddAssignMulScalarCommon(
    octets: ByteArray,
    other: ByteArray,
    scalar: Octet
) {
    require(octets.size == other.size)
    val scalarIndex = scalar.value.toUInt().toInt()
    for (i in octets.indices) {
        octets[i] = (octets[i] xor OCTET_MUL[scalarIndex][other[i].toUByte().toInt()].toByte())
    }
}

internal inline fun addAssignCommon(
    octets: ByteArray,
    other: ByteArray,
): ByteArray {
    require(octets.size == other.size)
    octets.indices.forEach { index ->
        octets[index] = octets[index] xor other[index]
    }
    return octets
}
