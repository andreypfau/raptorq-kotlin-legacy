@file:Suppress("OPT_IN_USAGE")

package com.github.andreypfau.raptorq.utils

fun bothRanges(i: Int, j: Int, len: Int): Pair<IntRange, IntRange> {
    return if (i < j) {
        (j + i until j + i + len) to (j until j + len)
    } else {
        (i until i + len) to (i + j until i + j + len)
    }
}

fun ULongArray.both(i: Int, j: Int, len: Int): Pair<ULongArray, ULongArray> {
    val (first, second) = bothRanges(i, j, len)
    return this.sliceArray(first) to this.sliceArray(second)
}
