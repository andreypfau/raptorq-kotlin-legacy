package com.github.andreypfau.raptorq.matrix

import kotlin.random.Random
import kotlin.test.assertEquals

fun Random.randDenseAndSparse(size: Int): Pair<DenseBinaryMatrix, SparseBinaryMatrix> {
    val dense = DenseBinaryMatrix(size, size)
    val sparse = SparseBinaryMatrix(size, size, 1)
    // Generate 50% filled random matrices
    for (index in 0 until (size * size / 2)) {
        val i = nextInt(0, size)
        val j = nextInt(0, size)
        val value = nextBoolean()
        dense[i, j] = value
        sparse[i, j] = value
    }

    return dense to sparse
}

fun assertMatricesEquals(matrix1: BinaryMatrix, matrix2: BinaryMatrix) {
    assertEquals(matrix1.height, matrix2.height)
    assertEquals(matrix1.width, matrix2.width)
    for (i in 0 until matrix1.height) {
        for (j in 0 until matrix1.width) {
            assertEquals(matrix1[i, j], matrix2[i, j], "Matrices are not equal at ($i, $j)")
        }
    }
}

fun assertMatricesEquals(matrix1: DenseOctetMatrix, matrix2: DenseOctetMatrix) {
    assertEquals(matrix1.height, matrix2.height)
    assertEquals(matrix1.width, matrix2.width)
    for (i in 0 until matrix1.height) {
        for (j in 0 until matrix1.width) {
            assertEquals(matrix1[i, j], matrix2[i, j], "Matrices are not equal at ($i, $j)")
        }
    }
}
