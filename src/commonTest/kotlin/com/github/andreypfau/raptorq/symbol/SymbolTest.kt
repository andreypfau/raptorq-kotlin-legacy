package com.github.andreypfau.raptorq.symbol

import kotlin.experimental.xor
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertContentEquals

class SymbolTest {
    @Test
    fun addAssign() {
        val symbolSize = 41
        val data1 = Random.nextBytes(symbolSize)
        val data2 = Random.nextBytes(symbolSize)
        val result = ByteArray(symbolSize) {
            data1[it] xor data2[it]
        }
        val symbol1 = Symbol(data1)
        val symbol2 = Symbol(data2)

        symbol1 += symbol2
        assertContentEquals(result, symbol1.value)
    }
}
