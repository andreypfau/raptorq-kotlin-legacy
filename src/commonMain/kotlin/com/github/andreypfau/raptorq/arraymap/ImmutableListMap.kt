@file:Suppress("OPT_IN_USAGE")

package com.github.andreypfau.raptorq.arraymap

class ImmutableListMap(
    val offsets: UIntArray,
    val values: UIntArray
) {
    operator fun get(i: Int): UIntArray {
        val start = offsets[i].toInt()
        val end = if (i == offsets.size - 1) {
            values.size
        } else {
            offsets[i + 1].toInt()
        }
        return values.copyOfRange(start, end)
    }
}
