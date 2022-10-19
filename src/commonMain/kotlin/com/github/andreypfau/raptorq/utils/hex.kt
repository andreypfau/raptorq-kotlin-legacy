package com.github.andreypfau.raptorq.utils

internal fun hex(string: String): ByteArray {
    val result = ByteArray(string.length / 2)
    for (i in result.indices) {
        val index = i * 2
        val first = string[index].digitToInt(16) shl 4
        val second = string[index + 1].digitToInt(16)
        result[i] = (first or second).toByte()
    }
    return result
}

internal fun ByteArray.toHex(): String {
    val result = StringBuilder()
    for (i in this.indices) {
        val first = ((this[i].toInt() and 0xFF) / 16).digitToChar(16)
        val second = ((this[i].toInt() and 0xFF) % 16).digitToChar(16)
        result.append(first)
        result.append(second)
    }
    return result.toString()
}
