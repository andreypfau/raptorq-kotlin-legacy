package com.github.andreypfau.raptorq.core

import kotlin.test.Test
import kotlin.test.assertTrue

class SystematicTest {
    @Test
    fun checkMaxWidthOptimization() {
        assertTrue(numIntermediateSymbols(MAX_SOURCE_SYMBOLS_PER_BLOCK) < 65536)
    }
}
