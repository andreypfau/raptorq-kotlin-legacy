@file:Suppress("OPT_IN_USAGE")

package com.github.andreypfau.raptorq.utils

internal fun addAssignBinary(dest: ULongArray, src: ULongArray) {
    for (i in dest.indices) {
        // Addition over GF(2) is defined as XOR
        dest[i] = dest[i] xor src[i]
    }
}
