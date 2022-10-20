package com.github.andreypfau.raptorq.octet

internal fun fusedAddAssignMulScalarBinary(
    octets: ByteArray,
    other: BinaryOctetVec,
    scalar: Octet
) = fusedAddAssignMulScalarBinary(octets, 0, octets.size, other, scalar)

internal fun fusedAddAssignMulScalarBinary(
    octets: ByteArray,
    octetsOffset: Int,
    octetsLen: Int,
    other: BinaryOctetVec,
    scalar: Octet
) = fusedAddAssignMulScalarBinaryCommon(octets, octetsOffset, octetsLen, other, scalar)

internal fun mulAssignScalar(
    octets: ByteArray,
    scalar: Octet
) = mulAssignScalar(octets, 0, octets.size, scalar)

internal fun mulAssignScalar(
    octets: ByteArray,
    octetsOffset: Int,
    octetsLen: Int,
    scalar: Octet
) = mulAssignScalarCommon(octets, octetsOffset, octetsLen, scalar)

internal fun fusedAddAssignMulScalar(
    octets: ByteArray,
    other: ByteArray,
    scalar: Octet
) = fusedAddAssignMulScalar(octets, 0, octets.size, other, 0, other.size, scalar)

internal fun fusedAddAssignMulScalar(
    octets: ByteArray,
    octetsOffset: Int,
    octetsLen: Int,
    other: ByteArray,
    otherOffset: Int,
    otherLen: Int,
    scalar: Octet
) = fusedAddAssignMulScalarCommon(octets, octetsOffset, octetsLen, other, otherOffset, otherLen, scalar)

internal fun addAssign(
    octets: ByteArray,
    other: ByteArray
) = addAssign(octets, 0, octets.size, other, 0, other.size)

internal fun addAssign(
    octets: ByteArray,
    octetsOffset: Int,
    octetsLen: Int,
    other: ByteArray,
    otherOffset: Int,
    otherLen: Int
): ByteArray = addAssignCommon(octets, octetsOffset, octetsLen, other, otherOffset, otherLen)
