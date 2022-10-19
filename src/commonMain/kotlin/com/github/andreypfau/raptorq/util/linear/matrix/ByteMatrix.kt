package com.github.andreypfau.raptorq.util.linear.matrix

interface ByteMatrix {
    operator fun get(i: Int, j: Int): Byte

    operator fun set(i: Int, j: Int, value: Byte)
}
