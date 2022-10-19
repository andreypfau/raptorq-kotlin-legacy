package com.github.andreypfau.raptorq.util.linear.matrix

import com.github.andreypfau.raptorq.util.linear.factory.Factory

abstract class AbstractByteMatrix(
    val factory: Factory,
    val rows: Int,
    val columns: Int
) : ByteMatrix {

}
