package com.github.andreypfau.raptorq.matrix

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class MatrixTest {
    @Test
    fun rowIter() = repeat(100) {
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

    @Test
    fun swapRows() = repeat(100) {
        // randDenseAndSparse() uses set(), so just check that it works
        val (dense, sparse) = Random.randDenseAndSparse(8)
        dense.swapRows(0, 4)
        dense.swapRows(1, 6)
        dense.swapRows(1, 7)
        sparse.swapRows(0, 4)
        sparse.swapRows(1, 6)
        sparse.swapRows(1, 7)
        assertMatricesEquals(dense, sparse)
    }

    @Test
    fun swapColumns() = repeat(100) {
        // randDenseAndSparse() uses set(), so just check that it works
        val (dense, sparse) = Random.randDenseAndSparse(8)
        dense.swapColumns(0, 4, 0)
        dense.swapColumns(1, 6, 0)
        dense.swapColumns(1, 1, 0)
        sparse.swapColumns(0, 4, 0)
        sparse.swapColumns(1, 6, 0)
        sparse.swapColumns(1, 1, 0)
        assertMatricesEquals(dense, sparse)
    }

    @Test
    fun countOnes() = repeat(100) {
        // randDenseAndSparse() uses set(), so just check that it works
        val (dense, sparse) = Random.randDenseAndSparse(8)
        assertEquals(dense.countOnes(0, 0, 5), sparse.countOnes(0, 0, 5))
        assertEquals(dense.countOnes(2, 2, 6), sparse.countOnes(2, 2, 6))
        assertEquals(dense.countOnes(3, 1, 2), sparse.countOnes(3, 1, 2))
    }

    @Test
    fun fmaRows() = repeat(100) {
        // randDenseAndSparse() uses set(), so just check that it works
        val (dense, sparse) = Random.randDenseAndSparse(8)
        dense.addAssignRows(0, 1, 0)
        dense.addAssignRows(0, 2, 0)
        dense.addAssignRows(2, 1, 0)
        sparse.addAssignRows(0, 1, 0)
        sparse.addAssignRows(0, 2, 0)
        sparse.addAssignRows(2, 1, 0)
        assertMatricesEquals(dense, sparse)
    }

    @Test
    fun resize() = repeat(100) {
        val (dense, sparse) = Random.randDenseAndSparse(8)
        dense.disableColumnAccessAcceleration()
        sparse.disableColumnAccessAcceleration()
        dense.resize(5, 5)
        sparse.resize(5, 5)
        assertMatricesEquals(dense, sparse)
    }

    @Test
    fun hintColumnDenseAndFrozen() = repeat(100) {
        val (dense, sparse) = Random.randDenseAndSparse(8)
        sparse.enableColumnAccessAcceleration()
        sparse.hintColumnDenseAndFrozen(6)
        sparse.hintColumnDenseAndFrozen(5)
        assertMatricesEquals(dense, sparse)
    }

    @Test
    fun denseStorageMath() {
        val size = 128
        val (dense, sparse) = Random.randDenseAndSparse(size)
        sparse.enableColumnAccessAcceleration()
        for (i in size - 2 downTo 0) {
            sparse.hintColumnDenseAndFrozen(i)
            assertMatricesEquals(dense, sparse)
        }
        assertMatricesEquals(dense, sparse)
        sparse.disableColumnAccessAcceleration()
        repeat(1000) {
            val i = Random.nextInt(0, size)
            var j = Random.nextInt(0, size)
            while (i == j) {
                j = Random.nextInt(0, size)
            }
            dense.addAssignRows(i, j, 0)
            sparse.addAssignRows(i, j, 0)
        }
        assertMatricesEquals(dense, sparse)
    }
}
