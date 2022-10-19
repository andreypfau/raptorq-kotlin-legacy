package com.github.andreypfau.raptorq.octet

internal fun fusedAddAssignMulScalarBinary(
    octets: ByteArray,
    other: BinaryOctetVec,
    scalar: Octet
) = fusedAddAssignMulScalarBinaryCommon(octets, other, scalar)

internal fun mulAssignScalar(
    octets: ByteArray,
    scalar: Octet
) = mulAssignScalarCommon(octets, scalar)

internal fun fusedAddAssignMulScalar(
    octets: ByteArray,
    other: ByteArray,
    scalar: Octet
) = fusedAddAssignMulScalarCommon(octets, other, scalar)

internal fun addAssign(
    octets: ByteArray,
    other: ByteArray,
): ByteArray = addAssignCommon(octets, other)
