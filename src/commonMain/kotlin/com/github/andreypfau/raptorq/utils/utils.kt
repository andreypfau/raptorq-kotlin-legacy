@file:Suppress("OPT_IN_USAGE")

package com.github.andreypfau.raptorq.utils

internal inline fun <T> List<T>.bothIndices(i: Int, j: Int): Pair<T, T> {
    return if (i < j) {
        this[j + i] to this[j]
    } else {
        this[i] to this[i + j]
    }
}

internal inline fun UShortArray.swap(i: Int, j: Int) {
    val tmp = this[i]
    this[i] = this[j]
    this[j] = tmp
}

internal inline fun UIntArray.swap(i: Int, j: Int) {
    val tmp = this[i]
    this[i] = this[j]
    this[j] = tmp
}

internal inline fun <T> Iterator<T>.nextOrNull(): T? = if (hasNext()) next() else null
