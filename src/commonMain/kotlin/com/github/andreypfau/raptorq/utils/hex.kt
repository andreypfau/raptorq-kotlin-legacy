package com.github.andreypfau.raptorq.utils

internal fun hex(string: String): ByteArray {
    val result = ByteArray(string.length / 2)
    for (i in result.indices) {
        val first = string[i * 2].code.toByte()
        val second = string[i * 2 + 1].code.toByte()
        result[i] = (first * 16 + second).toByte()
    }
    return result
}
