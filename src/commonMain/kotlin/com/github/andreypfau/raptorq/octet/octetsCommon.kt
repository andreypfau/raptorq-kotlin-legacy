@file:Suppress("OPT_IN_USAGE")

package com.github.andreypfau.raptorq.octet

import kotlin.experimental.xor

internal inline fun fusedAddAssignMulScalarBinaryCommon(
    octets: ByteArray,
    octetsOffset: Int,
    octetsLength: Int,
    other: BinaryOctetVec,
    scalar: Octet
) {
    require(octetsLength == other.length)
    val octetVec = other.toOctetVec()
    if (scalar == Octet.ONE) {
        addAssign(octets, octetsOffset, octetsLength, octetVec, 0, octetVec.size)
    } else {
        fusedAddAssignMulScalar(octets, octetsOffset, octetsLength, octetVec, 0, octetVec.size, scalar)
    }
}

internal inline fun mulAssignScalarCommon(
    octets: ByteArray,
    octetsOffset: Int,
    octetsLength: Int,
    scalar: Octet
) {
    val scalarIndex = scalar.value.toInt()
    for (i in octetsOffset until octetsOffset + octetsLength) {
        val octetIndex = octets[i].toUByte().toInt()
        octets[i] = OCTET_MUL[scalarIndex][octetIndex]
    }
}

internal inline fun fusedAddAssignMulScalarCommon(
    octets: ByteArray,
    octetsOffset: Int,
    octetsLength: Int,
    other: ByteArray,
    otherOffset: Int,
    otherLength: Int,
    scalar: Octet
) {
    require(octetsLength == otherLength)
    val scalarIndex = scalar.value.toUInt().toInt()
    repeat(octetsLength) { i ->
        val octetIndex = octetsOffset + i
        val otherIndex = otherOffset + i
        octets[octetIndex] = (octets[octetIndex] xor OCTET_MUL[scalarIndex][other[otherIndex].toUByte().toInt()])
    }
}

internal inline fun addAssignCommon(
    octets: ByteArray,
    octetsOffset: Int,
    octetsLength: Int,
    other: ByteArray,
    otherOffset: Int,
    otherLength: Int
): ByteArray {
    require(octetsLength == otherLength) { "expected: octetLength == otherLength, actual: $octetsLength, $otherLength" }
    repeat(octetsLength) { i ->
        val octetIndex = octetsOffset + i
        val otherIndex = otherOffset + i
        octets[octetIndex] = octets[octetIndex] xor other[otherIndex]
    }
    return octets
}
