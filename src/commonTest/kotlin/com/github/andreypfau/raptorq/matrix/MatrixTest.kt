package com.github.andreypfau.raptorq.matrix

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class MatrixTest {
    @Test
    fun rowIter() {
        val (dense, sparse) = Random.randDenseAndSparse(8)
        for (row in 0 until dense.height) {
            val startCol = Random.nextInt(0, dense.width - 2)
            val endCol = Random.nextInt(startCol + 1, dense.width)
            val denseIter = dense.rowIterator(row, startCol, endCol)
            val sparseIter = sparse.rowIterator(row, startCol, endCol)
            for (col in startCol until endCol) {
                assertEquals(dense[row, col], sparse[row, col])
                val nextDense = denseIter.next()
                assertEquals(dense[row, col], nextDense.second)
                // Sparse iter is not required to return zeros
                if (sparse[row, col]) {
                    val nextSparse = sparseIter.next()
                    assertEquals(sparse[row, col], nextSparse.second)
                }
            }
            assertFalse(denseIter.hasNext())
            assertFalse(sparseIter.hasNext())
        }
    }

    fun matrix(size: Int): Pair<DenseBinaryMatrix, SparseBinaryMatrix> {
        val dense = DenseBinaryMatrix(size, size)
        val sparse = SparseBinaryMatrix(size, size, 1)
        dense[0, 0] = true
        dense[3, 0] = true
        sparse[0, 0] = true
        sparse[3, 0] = true
        return dense to sparse
    }

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
}
