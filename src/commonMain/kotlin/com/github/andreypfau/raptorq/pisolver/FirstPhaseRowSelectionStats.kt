@file:Suppress("OPT_IN_USAGE")

package com.github.andreypfau.raptorq.pisolver

import com.github.andreypfau.raptorq.arraymap.U16ArrayMap
import com.github.andreypfau.raptorq.arraymap.U32VecMap
import com.github.andreypfau.raptorq.arraymap.UndirectedGraph
import com.github.andreypfau.raptorq.graph.ConnectedComponentGraph
import com.github.andreypfau.raptorq.matrix.BinaryMatrix

internal class FirstPhaseRowSelectionStats private constructor(
    private var originalDegree: U16ArrayMap,
    private val onesPerRow: U16ArrayMap,
    private val onesHistogram: U32VecMap,
    private var startCol: Int,
    private var endCol: Int,
    private var startRow: Int,
    private val rowsWithSingleOne: MutableList<Int>,
    // Mapping from columns (graph nodes) to their connected component id for the r = 2 substep
    private val colGraph: ConnectedComponentGraph
) {
    constructor(matrix: BinaryMatrix, endCol: Int, endRow: Int) : this(
        originalDegree = U16ArrayMap(0, 0),
        onesPerRow = U16ArrayMap(0, matrix.height),
        onesHistogram = U32VecMap(0),
        startCol = 0,
        endCol = endCol,
        startRow = 0,
        rowsWithSingleOne = ArrayList(),
        colGraph = ConnectedComponentGraph.create(endCol)
    ) {
        for (row in 0 until matrix.height) {
            val ones = matrix.countOnes(row, 0, endCol)
            onesPerRow[row] = ones.toUShort()
            onesHistogram.increment(ones)
            if (ones == 1) {
                rowsWithSingleOne.add(row)
            }
        }
        originalDegree = onesPerRow.copy()
        rebuildConnectedComponents(0, endRow, matrix)
    }


    fun firstPhaseSelection(
        startRow: Int,
        endRow: Int,
        matrix: BinaryMatrix
    ): Pair<Int?, Int?> {
        var r = -1
        for (i in 1..(endCol - startCol)) {
            if (onesHistogram[i] > 0u) {
                r = i
                break
            }
        }
        return when (r) {
            -1 -> return null to null
            2 -> {
                // Paragraph starting "If r = 2 and there is no row with exactly 2 ones in V" can
                // be ignored due to Errata 8.

                // See paragraph starting "If r = 2 and there is a row with exactly 2 ones in V..."
                val row = firstPhaseGraphSubstep(startRow, endRow, matrix)
                row to r
            }

            else -> {
                val row = firstPhaseOriginalDegreeSubstep(startRow, endRow, r)
                row to r
            }
        }
    }

    fun swapRows(i: Int, j: Int) {
        onesPerRow.swap(i, j)
        originalDegree.swap(i, j)
        val iterator = rowsWithSingleOne.listIterator()
        while (iterator.hasNext()) {
            val row = iterator.next()
            if (row == i) {
                iterator.set(j)
            } else if (row == j) {
                iterator.set(i)
            }
        }
    }

    fun swapColumns(i: Int, j: Int) {
        colGraph.swap(i, j)
    }

    // Recompute all stored statistics for the given row
    fun recomputeRow(row: Int, matrix: BinaryMatrix) {
        val ones = matrix.countOnes(row, startCol, endCol)
        rowsWithSingleOne.indexOfFirst { it == row }.let { index ->
            if (index != -1) {
                rowsWithSingleOne.removeAt(index)
            }
        }
        if (ones == 1) {
            rowsWithSingleOne.add(row)
        }
        onesHistogram.decrement(onesPerRow[row].toInt())
        onesHistogram.increment(ones)
        if (onesPerRow[row] == 2.toUShort()) {
            removeGraphEdge(row, matrix)
        }
        onesPerRow[row] = ones.toUShort()
        if (ones == 2) {
            addGraphEdge(row, matrix, startCol, endCol)
        }
    }

    fun resize(
        startRow: Int,
        endRow: Int,
        startCol: Int,
        endCol: Int,
        onesInStartCol: Iterable<Int>,
        matrix: BinaryMatrix
    ) {
        // Only shrinking is supported
        require(endCol <= this.endCol)
        require(this.startRow == startRow - 1)
        require(this.startCol == startCol - 1)

        // Remove this separately, since it's not part of ones_in_start_col
        if (matrix[this.startRow, this.startCol]) {
            val row = this.startRow
            onesPerRow.decrement(row)
            val ones = onesPerRow[row].toInt()
            when (ones) {
                0 -> {
                    rowsWithSingleOne.indexOfFirst { it == row }.let { index ->
                        if (index != -1) {
                            rowsWithSingleOne.removeAt(index)
                        }
                    }
                }

                1 -> {
                    removeGraphEdge(row, matrix)
                }
            }
            onesHistogram.decrement(ones + 1)
            onesHistogram.increment(ones)
        }

        val possibleNewGraphEdges = ArrayList<Int>()
        for (row in onesInStartCol) {
            onesPerRow.decrement(row)
            val ones = onesPerRow[row].toInt()
            when (ones) {
                0 -> rowsWithSingleOne.indexOfFirst { it == row }.let { index ->
                    if (index != -1) {
                        rowsWithSingleOne.removeAt(index)
                    }
                }

                1 -> {
                    rowsWithSingleOne.add(row)
                    removeGraphEdge(row, matrix)
                }

                2 -> possibleNewGraphEdges.add(row)
            }
            onesHistogram.decrement(ones + 1)
            onesHistogram.increment(ones)
        }

        colGraph.removeNode(startCol - 1)

        for (col in endCol until this.endCol) {
            for (row in matrix.onesInColumn(col, this.startRow, endRow)) {
                onesPerRow.decrement(row)
                val ones = onesPerRow[row].toInt()
                when (ones) {
                    0 -> rowsWithSingleOne.indexOfFirst { it == row }.let { index ->
                        if (index != -1) {
                            rowsWithSingleOne.removeAt(index)
                        }
                    }

                    1 -> {
                        rowsWithSingleOne.add(row)
                        removeGraphEdge(row, matrix)
                    }

                    2 -> possibleNewGraphEdges.add(row)
                }
                onesHistogram.decrement(ones + 1)
                onesHistogram.increment(ones)
            }
            colGraph.removeNode(col)
        }

        for (row in possibleNewGraphEdges) {
            if (onesPerRow[row] == 2.toUShort()) {
                addGraphEdge(row, matrix, startCol, endCol)
            }
        }

        this.startCol = startCol
        this.endCol = endCol
        this.startRow = startRow
    }

    private fun addGraphEdge(
        row: Int,
        matrix: BinaryMatrix,
        startCol: Int,
        endCol: Int
    ) {
        val ones = IntArray(2)
        var found = 0
        for ((col, value) in matrix.rowIterator(row, startCol, endCol)) {
            if (value) {
                ones[found++] = col
                if (found == 2) {
                    break
                }
            }
        }
        require(found == 2)
        colGraph.addEdge(ones[0], ones[1])
    }

    private fun removeGraphEdge(row: Int, matrix: BinaryMatrix) {
        // No-op. Graph edges are only removed when eliminating an entire connected component.
        // The effected nodes (cols) will be swapped to the beginning or end of V.
        // Therefore there is no need to update the connected component graph
    }

    private fun firstPhaseGraphSubstepBuildAdjacency(
        startRow: Int,
        endRow: Int,
        matrix: BinaryMatrix
    ): UndirectedGraph {
        val graph = UndirectedGraph(
            startCol.toUShort(),
            endCol.toUShort(),
            endCol - startCol
        )
        for (row in startRow until endRow) {
            if (onesPerRow[row] != 2.toUShort()) {
                continue
            }
            val ones = IntArray(2)
            var found = 0
            for ((col, value) in matrix.rowIterator(row, startCol, endCol)) {
                // "The following graph defined by the structure of V is used in determining which
                // row of A is chosen. The columns that intersect V are the nodes in the graph,
                // and the rows that have exactly 2 nonzero entries in V and are not HDPC rows
                // are the edges of the graph that connect the two columns (nodes) in the positions
                // of the two ones."
                // This part of the matrix is over GF(2), so "nonzero entries" is equivalent to "ones"
                if (value) {
                    ones[found++] = col
                }
                if (found == 2) {
                    break
                }
            }
            check(found == 2)
            graph.addEdge(ones[0].toUShort(), ones[1].toUShort())
        }
        graph.build()
        return graph
    }

    private fun rebuildConnectedComponents(
        startRow: Int,
        endRow: Int,
        matrix: BinaryMatrix
    ) {
        colGraph.reset()
        val graph = firstPhaseGraphSubstepBuildAdjacency(startRow, endRow, matrix)
        val nodeQueue = ArrayDeque<UShort>(10)
        for (key in graph.nodes()) {
            val connectedComponentId = colGraph.createConnectedComponent()
            nodeQueue.clear()
            nodeQueue.add(key)
            while (!nodeQueue.isEmpty()) {
                val node = nodeQueue.removeLast()
                if (colGraph.contains(node.toInt())) {
                    continue
                }
                colGraph.addNode(node.toInt(), connectedComponentId)
                for (nextNode in graph.getAdjacentNodes(node)) {
                    nodeQueue.add(nextNode)
                }
            }
        }
    }

    private fun firstPhaseGraphSubstep(
        startRow: Int,
        endRow: Int,
        matrix: BinaryMatrix
    ): Int {
        // Find a node (col) in the largest connected component
        val node = colGraph.getNodeInLargestConnectedComponent(this.startCol, this.endCol)
        // Find a row with two ones in the given column
        for (row in matrix.onesInColumn(node, startRow, endRow)) {
            if (onesPerRow[row] == 2.toUShort()) {
                return row
            }
        }
        return -1
    }

    private fun firstPhaseOriginalDegreeSubstep(
        startRow: Int,
        endRow: Int,
        r: Int
    ): Int {
        // There's no need for special handling of HDPC rows, since Errata 2 guarantees we won't
        // select any, and they're excluded in the first_phase solver
        var chosen = -1
        var chosenOriginalDegree = UShort.MAX_VALUE
        // Fast path for r=1, since this is super common
        if (r == 1) {
            require(rowsWithSingleOne.size != 0)
            for (row in rowsWithSingleOne) {
                val rowOriginalDegree = originalDegree[row]
                if (rowOriginalDegree < chosenOriginalDegree) {
                    chosen = row
                    chosenOriginalDegree = rowOriginalDegree
                }
            }
        } else {
            for (row in startRow until endRow) {
                val ones = onesPerRow[row].toInt()
                val rowOriginalDegree = originalDegree[row]
                if (ones == r && rowOriginalDegree < chosenOriginalDegree) {
                    chosen = row
                    chosenOriginalDegree = rowOriginalDegree
                }
            }
        }
        check(chosen != -1)
        return chosen
    }
}
