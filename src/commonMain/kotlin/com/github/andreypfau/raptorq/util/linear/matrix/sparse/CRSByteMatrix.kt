package com.github.andreypfau.raptorq.util.linear.matrix.sparse

import com.github.andreypfau.raptorq.util.linear.factory.CRSFactory

class CRSByteMatrix(
    rows: Int = 0,
    columns: Int = 0
) : AbstractCompressedByteMatrix(CRSFactory, rows, columns) {

}
