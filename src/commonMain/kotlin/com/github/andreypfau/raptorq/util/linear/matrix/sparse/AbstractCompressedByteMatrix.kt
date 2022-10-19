package com.github.andreypfau.raptorq.util.linear.matrix.sparse

import com.github.andreypfau.raptorq.util.linear.factory.Factory
import com.github.andreypfau.raptorq.util.linear.matrix.AbstractByteMatrix

abstract class AbstractCompressedByteMatrix(
    factory: Factory,
    rows: Int,
    columns: Int
) : AbstractByteMatrix(factory, rows, columns), SparseByteMatrix {

}
