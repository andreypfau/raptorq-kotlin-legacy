package com.github.andreypfau.raptorq.utils

fun ByteArray.getLong(index: Int): Long {
    var result = 0L
    for (i in 0 until 8) {
        result = result shl 8
        result = result or (this[index + i].toLong() and 0xFFL)
    }
    return result
}

fun ByteArray.getULong(index: Int) = getLong(index).toULong()
