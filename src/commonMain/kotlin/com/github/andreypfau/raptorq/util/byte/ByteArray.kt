package com.github.andreypfau.raptorq.util.byte

fun ByteArray.setIntAt(index: Int, value: Int) {
    this[index] = (value ushr 24).toByte()
    this[index + 1] = (value ushr 16).toByte()
    this[index + 2] = (value ushr 8).toByte()
    this[index + 3] = value.toByte()
}

fun ByteArray.getIntAt(index: Int): Int =
    this[index].toInt() and 0xFF shl 24 or
        (this[index + 1].toInt() and 0xFF shl 16) or
        (this[index + 2].toInt() and 0xFF shl 8) or
        (this[index + 3].toInt() and 0xFF)
