package com.github.andreypfau.raptorq.arraymap

class ImmutableListMap(
    val offsets: IntArray,
    val values: IntArray
) {
    operator fun get(i: Int): List<Int> {
        val start = offsets[i]
        val end = if (i == offsets.size - 1) {
            values.size
        } else {
            offsets[i + 1]
        }
        return values.slice(start until end)
    }
}
