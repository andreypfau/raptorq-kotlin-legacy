package com.github.andreypfau.raptorq.symbol

import kotlin.test.Test
import kotlin.test.assertEquals

class SymbolOpsTest {
    @Test
    fun testReorder() {
        val rows = 10
        val symbolSize = 10
        val data = Array(rows) { i ->
            val symbolData = ByteArray(symbolSize) {
                i.toByte()
            }
            Symbol(symbolData)
        }

        assertEquals(data[0].value[0], 0)
        assertEquals(data[1].value[0], 1)
        assertEquals(data[2].value[0], 2)
        assertEquals(data[9].value[0], 9)

        SymbolOps.Reorder(intArrayOf(9, 7, 5, 3, 1, 8, 0, 6, 2, 4)).invoke(
            data
        )

        assertEquals(data[0].value[0], 9)
        assertEquals(data[1].value[0], 7)
        assertEquals(data[2].value[0], 5)
        assertEquals(data[3].value[0], 3)
        assertEquals(data[4].value[0], 1)
        assertEquals(data[5].value[0], 8)
        assertEquals(data[6].value[0], 0)
        assertEquals(data[7].value[0], 6)
        assertEquals(data[8].value[0], 2)
        assertEquals(data[9].value[0], 4)
    }
}
