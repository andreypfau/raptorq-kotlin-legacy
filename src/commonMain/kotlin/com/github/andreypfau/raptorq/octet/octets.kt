package com.github.andreypfau.raptorq.octet

fun addOctets(
    octets: ByteArray,
    other: ByteArray,
    output: ByteArray = octets
): ByteArray = addOctetsCommon(octets, other, output)
